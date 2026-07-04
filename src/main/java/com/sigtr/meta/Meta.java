package com.sigtr.meta;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "metas")
public class Meta extends TenantSyncEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMeta tipo;

    private String descripcion;

    @Column(name = "valor_objetivo", nullable = false)
    private BigDecimal valorObjetivo;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMeta estado = EstadoMeta.ACTIVA;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    public enum TipoMeta {
        VENTAS, COBRO_CARTERA, REDUCCION_MERMAS
    }

    public enum EstadoMeta {
        ACTIVA, CUMPLIDA, VENCIDA, CANCELADA
    }
}
