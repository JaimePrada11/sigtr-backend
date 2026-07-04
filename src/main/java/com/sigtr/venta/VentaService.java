package com.sigtr.venta;

import com.sigtr.caja.CajaSesion;
import com.sigtr.caja.CajaSesionRepository;
import com.sigtr.caja.MovimientoCaja;
import com.sigtr.caja.MovimientoCajaRepository;
import com.sigtr.cliente.Cliente;
import com.sigtr.cliente.ClienteRepository;
import com.sigtr.common.TenantContext;
import com.sigtr.inventario.InventarioService;
import com.sigtr.inventario.MovimientoInventario;
import com.sigtr.producto.Producto;
import com.sigtr.producto.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final AbonoCarteraRepository abonoCarteraRepository;
    private final InventarioService inventarioService;

    /**
     * Registra una venta (contado o fiado) con su detalle, descuenta stock,
     * actualiza saldo del cliente si aplica y genera el movimiento de caja.
     * Idempotente por clientUuid: si ya existe, se retorna sin duplicar
     * (clave para cuando el frontend reenvia desde la cola offline).
     */
    @Transactional
    public Venta registrarVenta(String clientUuid, Long clienteId, Venta.FormaPago formaPago,
                                 Iterable<VentaDtos.ItemVenta> items) {

        var existente = ventaRepository.findByClientUuid(clientUuid);
        if (existente.isPresent()) {
            return existente.get();
        }

        Long tenantId = TenantContext.get();
        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay una sesion de caja abierta"));

        if (formaPago == Venta.FormaPago.FIADO && clienteId == null) {
            throw new IllegalArgumentException("Una venta fiada requiere un cliente");
        }

        Venta venta = new Venta();
        venta.setTenantId(tenantId);
        venta.setClientUuid(clientUuid);
        venta.setClienteId(clienteId);
        venta.setUsuarioId(usuarioActualId());
        venta.setCajaSesionId(cajaAbierta.getId());
        venta.setFormaPago(formaPago);

        BigDecimal total = BigDecimal.ZERO;

        for (VentaDtos.ItemVenta item : items) {
            Producto producto = productoRepository.findByIdAndTenantId(item.productoId(), tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + item.productoId()));

            if (producto.getStockActual().compareTo(item.cantidad()) < 0) {
                throw new IllegalStateException("Stock insuficiente para " + producto.getNombre());
            }

            BigDecimal subtotal = producto.getPrecioVenta().multiply(item.cantidad());

            DetalleVenta detalle = new DetalleVenta();
            detalle.setTenantId(tenantId);
            detalle.setVenta(venta);
            detalle.setProductoId(producto.getId());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);
            venta.getDetalles().add(detalle);

            total = total.add(subtotal);
        }

        venta.setTotal(total);
        ventaRepository.save(venta);

        // El descuento de stock pasa por InventarioService (fuente de verdad,
        // consume lotes FIFO si existen) una vez la venta ya tiene id, para
        // que el movimiento de inventario quede referenciado a esta venta.
        for (VentaDtos.ItemVenta item : items) {
            inventarioService.registrarSalida(item.productoId(), item.cantidad(),
                    MovimientoInventario.TipoMovimientoInventario.VENTA, null,
                    usuarioActualId(), "VENTA", venta.getId());
        }

        if (formaPago == Venta.FormaPago.FIADO) {
            Cliente cliente = clienteRepository.findByIdAndTenantId(clienteId, tenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));

            BigDecimal limite = cliente.getLimiteCredito();
            BigDecimal saldoProyectado = cliente.getSaldoActual().add(total);
            if (limite.compareTo(BigDecimal.ZERO) > 0 && saldoProyectado.compareTo(limite) > 0) {
                throw new IllegalStateException(
                        "Venta rechazada: " + cliente.getNombre() + " superaria su limite de credito ("
                        + saldoProyectado + " > " + limite + ")");
            }

            cliente.setSaldoActual(saldoProyectado);
            clienteRepository.save(cliente);
        }

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(tenantId);
        movimiento.setClientUuid(clientUuid + "-mov"); // uuid derivado, unico por venta
        movimiento.setCajaSesionId(cajaAbierta.getId());
        movimiento.setTipo(MovimientoCaja.TipoMovimiento.VENTA);
        movimiento.setMonto(total);
        movimiento.setDescripcion("Venta " + formaPago);
        movimiento.setReferenciaTipo("VENTA");
        movimiento.setReferenciaId(venta.getId());
        movimientoCajaRepository.save(movimiento);

        return venta;
    }

    /**
     * Registra un abono a la cartera de un cliente y actualiza su saldo.
     * Idempotente por clientUuid.
     */
    @Transactional
    public AbonoCartera registrarAbono(String clientUuid, Long clienteId, BigDecimal monto) {
        var existente = abonoCarteraRepository.findByClientUuid(clientUuid);
        if (existente.isPresent()) {
            return existente.get();
        }

        Long tenantId = TenantContext.get();
        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay una sesion de caja abierta"));

        Cliente cliente = clienteRepository.findByIdAndTenantId(clienteId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));

        cliente.setSaldoActual(cliente.getSaldoActual().subtract(monto));
        clienteRepository.save(cliente);

        AbonoCartera abono = new AbonoCartera();
        abono.setTenantId(tenantId);
        abono.setClientUuid(clientUuid);
        abono.setClienteId(clienteId);
        abono.setUsuarioId(usuarioActualId());
        abono.setCajaSesionId(cajaAbierta.getId());
        abono.setMonto(monto);
        abonoCarteraRepository.save(abono);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(tenantId);
        movimiento.setClientUuid(clientUuid + "-mov");
        movimiento.setCajaSesionId(cajaAbierta.getId());
        movimiento.setTipo(MovimientoCaja.TipoMovimiento.ABONO);
        movimiento.setMonto(monto);
        movimiento.setDescripcion("Abono cartera cliente " + clienteId);
        movimiento.setReferenciaTipo("ABONO_CARTERA");
        movimiento.setReferenciaId(abono.getId());
        movimientoCajaRepository.save(movimiento);

        return abono;
    }

    // Fase 1: simplificado. Cuando se agregue Spring Security con JWT completo,
    // este metodo debe leer el usuario autenticado del SecurityContext.
    private Long usuarioActualId() {
        return 1L;
    }
}
