package com.sigtr.prestamo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PrestamoDtos {

    public record CrearPrestamoRequest(
            @NotBlank String clientUuid,
            @NotNull Prestamo.TipoPrestamo tipo,
            @NotBlank String nombrePersona,
            Long clienteId,
            @NotNull BigDecimal monto,
            LocalDate fechaVencimiento
    ) {
    }

    public record AbonoRequest(@NotBlank String clientUuid, @NotNull BigDecimal monto) {
    }

    public record PrestamoResponse(
            Long id, String tipo, String nombrePersona, Long clienteId, BigDecimal monto,
            BigDecimal saldoPendiente, LocalDate fechaPrestamo, LocalDate fechaVencimiento, String estado
    ) {
        public static PrestamoResponse from(Prestamo p) {
            return new PrestamoResponse(p.getId(), p.getTipo().name(), p.getNombrePersona(),
                    p.getClienteId(), p.getMonto(), p.getSaldoPendiente(), p.getFechaPrestamo(),
                    p.getFechaVencimiento(), p.getEstado().name());
        }
    }
}
