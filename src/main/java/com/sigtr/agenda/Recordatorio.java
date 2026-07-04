package com.sigtr.agenda;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "recordatorios")
public class Recordatorio extends TenantSyncEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecordatorio tipo;

    // nullable: un recordatorio manual (TAREA) no referencia otra entidad
    @Column(name = "referencia_tipo")
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    @Column(name = "fecha_recordatorio", nullable = false)
    private LocalDate fechaRecordatorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRecordatorio estado = EstadoRecordatorio.PENDIENTE;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    public enum TipoRecordatorio {
        PAGO_CLIENTE, PAGO_PROVEEDOR, VENCIMIENTO_PRODUCTO, PRESTAMO, TAREA
    }

    public enum EstadoRecordatorio {
        PENDIENTE, COMPLETADO, DESCARTADO
    }
}
