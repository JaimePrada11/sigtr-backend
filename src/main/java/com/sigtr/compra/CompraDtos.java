package com.sigtr.compra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CompraDtos {

    public record ItemCompra(
            @NotNull Long productoId,
            @NotNull BigDecimal cantidad,
            @NotNull BigDecimal costoUnitario,
            String numeroLote,
            LocalDate fechaVencimiento
    ) {
    }

    public record CrearCompraRequest(
            @NotBlank String clientUuid,
            @NotNull Long proveedorId,
            Long ordenCompraId, // nullable
            @NotNull Compra.FormaPagoCompra formaPago,
            LocalDate fechaVencimientoPago, // solo relevante si formaPago = CREDITO
            @NotEmpty List<ItemCompra> items
    ) {
    }

    public record CompraResponse(Long id, Long proveedorId, Long ordenCompraId,
                                  String formaPago, BigDecimal total, String estado) {
        public static CompraResponse from(Compra c) {
            return new CompraResponse(c.getId(), c.getProveedorId(), c.getOrdenCompraId(),
                    c.getFormaPago().name(), c.getTotal(), c.getEstado().name());
        }
    }
}
