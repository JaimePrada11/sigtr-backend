package com.sigtr.cuentaabierta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CuentaAbiertaDtos {

    public record AbrirRequest(@NotBlank String clientUuid, @NotBlank String nombre) {
    }

    public record ParticipanteRequest(@NotBlank String nombre, Long clienteId) {
    }

    public record ConsumoRequest(
            @NotNull Long participanteId,
            @NotNull Long productoId,
            @NotNull BigDecimal cantidad
    ) {
    }

    public record PagoRequest(
            @NotBlank String clientUuid,
            Long participanteId, // null = pago grupal
            @NotNull BigDecimal monto,
            @NotNull PagoCuenta.FormaPago formaPago
    ) {
    }

    public record ParticipanteSaldo(
            Long participanteId,
            String nombre,
            Long clienteId,
            BigDecimal consumido,
            BigDecimal pagado,
            BigDecimal saldoPendiente
    ) {
    }

    public record DivisionResponse(
            Long cuentaAbiertaId,
            BigDecimal totalConsumido,
            BigDecimal totalPagado,
            BigDecimal totalPendiente,
            List<ParticipanteSaldo> porParticipante
    ) {
    }

    public record CuentaResponse(Long id, String nombre, String estado) {
        static CuentaResponse from(CuentaAbierta c) {
            return new CuentaResponse(c.getId(), c.getNombre(), c.getEstado().name());
        }
    }
}
