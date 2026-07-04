package com.sigtr.prestamo;

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
@Table(name = "prestamos")
public class Prestamo extends TenantSyncEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPrestamo tipo;

    @Column(name = "nombre_persona", nullable = false)
    private String nombrePersona;

    @Column(name = "cliente_id")
    private Long clienteId; // nullable -- no requiere ser cliente registrado

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(name = "saldo_pendiente", nullable = false)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;

    public enum TipoPrestamo {
        OTORGADO, RECIBIDO
    }

    public enum EstadoPrestamo {
        ACTIVO, PAGADO, INCUMPLIDO
    }
}
