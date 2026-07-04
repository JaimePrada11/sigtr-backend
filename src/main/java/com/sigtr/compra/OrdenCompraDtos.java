package com.sigtr.compra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class OrdenCompraDtos {

    public record ItemOrden(@NotNull Long productoId, @NotNull BigDecimal cantidad,
                             @NotNull BigDecimal costoUnitarioEstimado) {
    }

    public record CrearOrdenRequest(
            @NotBlank String clientUuid,
            @NotNull Long proveedorId,
            @NotEmpty List<ItemOrden> items,
            String observaciones
    ) {
    }

    public record DetalleResponse(Long productoId, BigDecimal cantidadPedida,
                                   BigDecimal cantidadRecibida, BigDecimal costoUnitarioEstimado) {
        public static DetalleResponse from(DetalleOrdenCompra d) {
            return new DetalleResponse(d.getProductoId(), d.getCantidadPedida(),
                    d.getCantidadRecibida(), d.getCostoUnitarioEstimado());
        }
    }

    public record OrdenResponse(Long id, Long proveedorId, String estado,
                                 List<DetalleResponse> detalles) {
        public static OrdenResponse from(OrdenCompra o) {
            return new OrdenResponse(o.getId(), o.getProveedorId(), o.getEstado().name(),
                    o.getDetalles().stream().map(DetalleResponse::from).toList());
        }
    }
}
