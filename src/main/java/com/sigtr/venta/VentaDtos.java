package com.sigtr.venta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class VentaDtos {

    /** Venta normal (contado o fiado), con carrito completo. */
    public record VentaRequest(
            @NotBlank String clientUuid,
            Long clienteId, // nullable si es CONTADO a mostrador
            @NotNull Venta.FormaPago formaPago,
            @NotEmpty List<ItemVenta> items
    ) {
    }

    public record ItemVenta(
            @NotNull Long productoId,
            @NotNull BigDecimal cantidad
    ) {
    }

    /**
     * Request de la Libreta Digital: "APUNTAR FIADO".
     * Siempre es un cliente existente + lista de productos, siempre FIADO.
     * Es intencionalmente mas simple que VentaRequest: sin necesidad de
     * armar un "carrito" en el sentido tradicional.
     */
    public record LibretaFiadoRequest(
            @NotBlank String clientUuid,
            @NotNull Long clienteId,
            @NotEmpty List<ItemVenta> items
    ) {
    }

    public record VentaResponse(
            Long id,
            Long clienteId,
            String formaPago,
            BigDecimal total,
            String estado
    ) {
        public static VentaResponse from(Venta v) {
            return new VentaResponse(
                    v.getId(), v.getClienteId(), v.getFormaPago().name(),
                    v.getTotal(), v.getEstado().name()
            );
        }
    }

    public record AbonoRequest(
            @NotBlank String clientUuid,
            @NotNull Long clienteId,
            @NotNull BigDecimal monto
    ) {
    }
}
