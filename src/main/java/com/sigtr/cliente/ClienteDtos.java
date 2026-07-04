package com.sigtr.cliente;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;

public class ClienteDtos {

    public record ClienteRequest(
            @NotBlank String clientUuid,
            @NotBlank String nombre,
            String telefono,
            BigDecimal limiteCredito
    ) {
    }

    public record ClienteResponse(
            Long id,
            String nombre,
            String telefono,
            BigDecimal limiteCredito,
            BigDecimal saldoActual,
            boolean activo
    ) {
        public static ClienteResponse from(Cliente c) {
            return new ClienteResponse(
                    c.getId(), c.getNombre(), c.getTelefono(),
                    c.getLimiteCredito(), c.getSaldoActual(), c.isActivo()
            );
        }
    }

    public record EstadoCuentaResponse(
            Long clienteId,
            String nombre,
            BigDecimal saldoActual,
            BigDecimal limiteCredito,
            java.util.List<MovimientoCuenta> movimientos
    ) {
    }

    public record MovimientoCuenta(
            String tipo, // VENTA_FIADA, ABONO
            BigDecimal monto,
            Instant fecha
    ) {
    }

    public record AlertasResponse(
            Long clienteId,
            boolean limiteCreditoExcedido,
            long diasMora,
            boolean clienteAltoRiesgo
    ) {
    }
}
