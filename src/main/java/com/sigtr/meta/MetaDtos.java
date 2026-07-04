package com.sigtr.meta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MetaDtos {

    public record CrearMetaRequest(
            @NotBlank String clientUuid,
            @NotNull Meta.TipoMeta tipo,
            String descripcion,
            @NotNull BigDecimal valorObjetivo,
            @NotNull LocalDate periodoInicio,
            @NotNull LocalDate periodoFin
    ) {
    }

    public record MetaResponse(Long id, String tipo, String descripcion, BigDecimal valorObjetivo,
                                LocalDate periodoInicio, LocalDate periodoFin, String estado) {
        public static MetaResponse from(Meta m) {
            return new MetaResponse(m.getId(), m.getTipo().name(), m.getDescripcion(),
                    m.getValorObjetivo(), m.getPeriodoInicio(), m.getPeriodoFin(), m.getEstado().name());
        }
    }

    public record ProgresoResponse(Long metaId, String tipo, BigDecimal valorObjetivo,
                                    BigDecimal valorActual, BigDecimal porcentaje, boolean vencida) {
    }
}
