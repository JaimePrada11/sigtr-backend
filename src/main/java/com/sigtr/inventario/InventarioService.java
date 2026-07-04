package com.sigtr.inventario;

import com.sigtr.agenda.Recordatorio;
import com.sigtr.agenda.RecordatorioService;
import com.sigtr.common.TenantContext;
import com.sigtr.producto.Producto;
import com.sigtr.producto.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fuente de verdad de todo cambio de stock. producto.stock_actual es un
 * campo derivado que esta clase mantiene consistente en cada operacion.
 *
 * Si el producto tiene lotes registrados, las salidas se consumen por FIFO
 * (el que vence antes, primero). Si no tiene lotes (ej. abarrotes sueltos
 * dados de alta directo con stockActual en Fase 1), la salida afecta el
 * stock del producto directamente, sin lote asociado.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final int DIAS_AVISO_VENCIMIENTO = 7;

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final RecordatorioService recordatorioService;

    @Transactional
    public Lote registrarEntrada(String clientUuid, Long productoId, BigDecimal cantidad,
                                  BigDecimal costoUnitario, String numeroLote,
                                  LocalDate fechaVencimiento, Long usuarioId) {
        Long tenantId = TenantContext.get();
        Producto producto = obtenerProducto(productoId, tenantId);

        Lote lote = new Lote();
        lote.setTenantId(tenantId);
        lote.setClientUuid(clientUuid);
        lote.setProductoId(productoId);
        lote.setNumeroLote(numeroLote);
        lote.setFechaIngreso(LocalDate.now());
        lote.setFechaVencimiento(fechaVencimiento);
        lote.setCostoUnitario(costoUnitario);
        lote.setCantidadInicial(cantidad);
        lote.setCantidadActual(cantidad);
        loteRepository.save(lote);

        registrarMovimiento(tenantId, productoId, lote.getId(),
                MovimientoInventario.TipoMovimientoInventario.COMPRA, cantidad,
                costoUnitario, null, usuarioId, "COMPRA", lote.getId());

        producto.setStockActual(producto.getStockActual().add(cantidad));
        productoRepository.save(producto);

        if (fechaVencimiento != null) {
            LocalDate fechaAviso = fechaVencimiento.minusDays(DIAS_AVISO_VENCIMIENTO);
            recordatorioService.generarAutomatico(Recordatorio.TipoRecordatorio.VENCIMIENTO_PRODUCTO,
                    "LOTE", lote.getId(),
                    "Producto por vencer: " + producto.getNombre(),
                    "Lote " + (numeroLote != null ? numeroLote : "#" + lote.getId())
                            + " vence el " + fechaVencimiento,
                    fechaAviso);
        }

        return lote;
    }

    /**
     * Salida generica de inventario (venta, merma, consumo propio, vencimiento).
     * Consume lotes por FIFO si existen; si no, descuenta directo del producto.
     */
    @Transactional
    public void registrarSalida(Long productoId, BigDecimal cantidad,
                                 MovimientoInventario.TipoMovimientoInventario tipo,
                                 String motivo, Long usuarioId,
                                 String referenciaTipo, Long referenciaId) {
        Long tenantId = TenantContext.get();
        Producto producto = obtenerProducto(productoId, tenantId);

        if (producto.getStockActual().compareTo(cantidad) < 0) {
            throw new IllegalStateException("Stock insuficiente para " + producto.getNombre());
        }

        List<Lote> disponibles = loteRepository.findDisponiblesFifo(tenantId, productoId);
        BigDecimal restante = cantidad;

        if (!disponibles.isEmpty()) {
            for (Lote lote : disponibles) {
                if (restante.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal tomar = lote.getCantidadActual().min(restante);
                lote.setCantidadActual(lote.getCantidadActual().subtract(tomar));
                loteRepository.save(lote);

                registrarMovimiento(tenantId, productoId, lote.getId(), tipo, tomar,
                        lote.getCostoUnitario(), motivo, usuarioId, referenciaTipo, referenciaId);

                restante = restante.subtract(tomar);
            }
            // Si los lotes no alcanzan a cubrir toda la cantidad (inconsistencia de
            // datos), el remanente se descuenta sin lote asociado para no bloquear
            // la operacion -- pero queda registrado igual como movimiento.
            if (restante.compareTo(BigDecimal.ZERO) > 0) {
                registrarMovimiento(tenantId, productoId, null, tipo, restante,
                        null, motivo, usuarioId, referenciaTipo, referenciaId);
            }
        } else {
            registrarMovimiento(tenantId, productoId, null, tipo, cantidad,
                    null, motivo, usuarioId, referenciaTipo, referenciaId);
        }

        producto.setStockActual(producto.getStockActual().subtract(cantidad));
        productoRepository.save(producto);
    }

    /** Ajuste manual +/- (ej. conteo fisico que no cuadra). No interactua con lotes. */
    @Transactional
    public void registrarAjuste(Long productoId, BigDecimal delta, String motivo, Long usuarioId) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Un ajuste de inventario requiere motivo");
        }

        Long tenantId = TenantContext.get();
        Producto producto = obtenerProducto(productoId, tenantId);

        registrarMovimiento(tenantId, productoId, null,
                MovimientoInventario.TipoMovimientoInventario.AJUSTE, delta.abs(),
                null, motivo, usuarioId, "AJUSTE_MANUAL", null);

        producto.setStockActual(producto.getStockActual().add(delta));
        productoRepository.save(producto);
    }

    public List<MovimientoInventario> listarMovimientos(Long productoId) {
        return movimientoRepository.findByTenantIdAndProductoIdOrderByCreatedAtDesc(TenantContext.get(), productoId);
    }

    public List<Lote> listarLotes(Long productoId) {
        return loteRepository.findByTenantIdAndProductoIdOrderByFechaVencimientoAscCreatedAtAsc(
                TenantContext.get(), productoId);
    }

    private Producto obtenerProducto(Long productoId, Long tenantId) {
        return productoRepository.findByIdAndTenantId(productoId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));
    }

    private void registrarMovimiento(Long tenantId, Long productoId, Long loteId,
                                      MovimientoInventario.TipoMovimientoInventario tipo,
                                      BigDecimal cantidad, BigDecimal costoUnitario, String motivo,
                                      Long usuarioId, String referenciaTipo, Long referenciaId) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setTenantId(tenantId);
        mov.setClientUuid(java.util.UUID.randomUUID().toString());
        mov.setProductoId(productoId);
        mov.setLoteId(loteId);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setCostoUnitario(costoUnitario);
        mov.setMotivo(motivo);
        mov.setUsuarioId(usuarioId);
        mov.setReferenciaTipo(referenciaTipo);
        mov.setReferenciaId(referenciaId);
        movimientoRepository.save(mov);
    }
}
