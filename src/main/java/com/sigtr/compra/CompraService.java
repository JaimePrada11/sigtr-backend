package com.sigtr.compra;

import com.sigtr.agenda.Recordatorio;
import com.sigtr.agenda.RecordatorioService;
import com.sigtr.caja.CajaSesion;
import com.sigtr.caja.CajaSesionRepository;
import com.sigtr.caja.MovimientoCaja;
import com.sigtr.caja.MovimientoCajaRepository;
import com.sigtr.common.TenantContext;
import com.sigtr.inventario.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Registra una compra (con o sin orden de compra previa). Cada linea genera
 * una entrada real de inventario via InventarioService (lote + movimiento +
 * actualizacion de stock). Si la compra es a credito, genera la CuentaPorPagar
 * correspondiente; si es de contado, genera un egreso de caja inmediato.
 */
@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final InventarioService inventarioService;
    private final RecordatorioService recordatorioService;

    // Fase 1/2/3: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public Compra registrar(CompraDtos.CrearCompraRequest req) {
        var existente = compraRepository.findByClientUuid(req.clientUuid());
        if (existente.isPresent()) {
            return existente.get();
        }

        Long tenantId = TenantContext.get();

        Compra compra = new Compra();
        compra.setTenantId(tenantId);
        compra.setClientUuid(req.clientUuid());
        compra.setProveedorId(req.proveedorId());
        compra.setOrdenCompraId(req.ordenCompraId());
        compra.setUsuarioId(usuarioActualId());
        compra.setFormaPago(req.formaPago());

        BigDecimal total = BigDecimal.ZERO;
        for (CompraDtos.ItemCompra item : req.items()) {
            BigDecimal subtotal = item.costoUnitario().multiply(item.cantidad());

            DetalleCompra detalle = new DetalleCompra();
            detalle.setTenantId(tenantId);
            detalle.setCompra(compra);
            detalle.setProductoId(item.productoId());
            detalle.setCantidad(item.cantidad());
            detalle.setCostoUnitario(item.costoUnitario());
            detalle.setSubtotal(subtotal);
            detalle.setNumeroLote(item.numeroLote());
            detalle.setFechaVencimiento(item.fechaVencimiento());
            compra.getDetalles().add(detalle);

            total = total.add(subtotal);
        }

        compra.setTotal(total);
        compraRepository.save(compra);

        // Cada linea entra al inventario real (crea lote, movimiento COMPRA, stock).
        for (CompraDtos.ItemCompra item : req.items()) {
            inventarioService.registrarEntrada(UUID.randomUUID().toString(), item.productoId(),
                    item.cantidad(), item.costoUnitario(), item.numeroLote(),
                    item.fechaVencimiento(), usuarioActualId());
        }

        if (req.ordenCompraId() != null) {
            actualizarOrdenCompra(req.ordenCompraId(), req.items());
        }

        if (req.formaPago() == Compra.FormaPagoCompra.CREDITO) {
            CuentaPorPagar cxp = new CuentaPorPagar();
            cxp.setTenantId(tenantId);
            cxp.setClientUuid(req.clientUuid() + "-cxp");
            cxp.setProveedorId(req.proveedorId());
            cxp.setCompraId(compra.getId());
            cxp.setMontoTotal(total);
            cxp.setSaldoPendiente(total);
            cxp.setFechaVencimiento(req.fechaVencimientoPago());
            cxp.setEstado(CuentaPorPagar.EstadoCuentaPorPagar.PENDIENTE);
            cuentaPorPagarRepository.save(cxp);

            if (req.fechaVencimientoPago() != null) {
                recordatorioService.generarAutomatico(Recordatorio.TipoRecordatorio.PAGO_PROVEEDOR,
                        "CUENTA_POR_PAGAR", cxp.getId(),
                        "Pago a proveedor pendiente",
                        "Compra #" + compra.getId() + " por " + total,
                        req.fechaVencimientoPago());
            }
        } else {
            registrarEgresoCaja(tenantId, req.clientUuid(), total,
                    "Compra de contado a proveedor " + req.proveedorId());
        }

        return compra;
    }

    private void actualizarOrdenCompra(Long ordenCompraId, java.util.List<CompraDtos.ItemCompra> items) {
        OrdenCompra orden = ordenCompraRepository.findByIdAndTenantId(ordenCompraId, TenantContext.get())
                .orElseThrow(() -> new EntityNotFoundException("Orden de compra no encontrada: " + ordenCompraId));

        for (CompraDtos.ItemCompra item : items) {
            orden.getDetalles().stream()
                    .filter(d -> d.getProductoId().equals(item.productoId()))
                    .findFirst()
                    .ifPresent(d -> d.setCantidadRecibida(d.getCantidadRecibida().add(item.cantidad())));
        }

        boolean todoCompleto = orden.getDetalles().stream()
                .allMatch(d -> d.getCantidadRecibida().compareTo(d.getCantidadPedida()) >= 0);
        boolean algoRecibido = orden.getDetalles().stream()
                .anyMatch(d -> d.getCantidadRecibida().compareTo(BigDecimal.ZERO) > 0);

        orden.setEstado(todoCompleto
                ? OrdenCompra.EstadoOrdenCompra.RECIBIDA
                : algoRecibido ? OrdenCompra.EstadoOrdenCompra.PARCIAL : orden.getEstado());

        ordenCompraRepository.save(orden);
    }

    private void registrarEgresoCaja(Long tenantId, String clientUuid, BigDecimal monto, String descripcion) {
        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay una sesion de caja abierta -- no se puede pagar de contado sin caja abierta"));

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(tenantId);
        movimiento.setClientUuid(clientUuid + "-mov");
        movimiento.setCajaSesionId(cajaAbierta.getId());
        movimiento.setTipo(MovimientoCaja.TipoMovimiento.GASTO);
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setReferenciaTipo("COMPRA_CONTADO");
        movimientoCajaRepository.save(movimiento);
    }

    public Compra obtener(Long id) {
        return compraRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new EntityNotFoundException("Compra no encontrada: " + id));
    }
}
