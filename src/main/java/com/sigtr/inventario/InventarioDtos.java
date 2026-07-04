package com.sigtr.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventarioDtos {

    public record EntradaRequest(
            @NotBlank String clientUuid,
            @NotNull Long productoId,
            @NotNull BigDecimal cantidad,
            @NotNull BigDecimal costoUnitario,
            String numeroLote,
            LocalDate fechaVencimiento
    ) {
    }

    public record SalidaRequest(
            @NotNull Long productoId,
            @NotNull BigDecimal cantidad,
            String motivo
    ) {
    }

    public record AjusteRequest(
            @NotNull Long productoId,
            @NotNull BigDecimal delta, // positivo o negativo
            @NotBlank String motivo
    ) {
    }

    public record MovimientoResponse(
            Long id, Long productoId, Long loteId, String tipo, BigDecimal cantidad,
            BigDecimal costoUnitario, String motivo, java.time.Instant createdAt
    ) {
        public static MovimientoResponse from(MovimientoInventario m) {
            return new MovimientoResponse(m.getId(), m.getProductoId(), m.getLoteId(),
                    m.getTipo().name(), m.getCantidad(), m.getCostoUnitario(), m.getMotivo(), m.getCreatedAt());
        }
    }

    public record LoteResponse(
            Long id, Long productoId, String numeroLote, LocalDate fechaIngreso,
            LocalDate fechaVencimiento, BigDecimal costoUnitario,
            BigDecimal cantidadInicial, BigDecimal cantidadActual
    ) {
        public static LoteResponse from(Lote l) {
            return new LoteResponse(l.getId(), l.getProductoId(), l.getNumeroLote(),
                    l.getFechaIngreso(), l.getFechaVencimiento(), l.getCostoUnitario(),
                    l.getCantidadInicial(), l.getCantidadActual());
        }
    }
}
