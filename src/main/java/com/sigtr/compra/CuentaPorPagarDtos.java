package com.sigtr.compra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuentaPorPagarDtos {

    public record AbonoRequest(@NotBlank String clientUuid, @NotNull BigDecimal monto) {
    }

    public record CxpResponse(Long id, Long proveedorId, Long compraId, BigDecimal montoTotal,
                               BigDecimal saldoPendiente, LocalDate fechaVencimiento, String estado) {
        public static CxpResponse from(CuentaPorPagar c) {
            return new CxpResponse(c.getId(), c.getProveedorId(), c.getCompraId(), c.getMontoTotal(),
                    c.getSaldoPendiente(), c.getFechaVencimiento(), c.getEstado().name());
        }
    }
}
