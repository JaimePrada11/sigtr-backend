package com.sigtr.caja;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "caja_sesiones")
public class CajaSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_apertura", nullable = false)
    private Instant fechaApertura;

    @Column(name = "monto_apertura", nullable = false)
    private BigDecimal montoApertura;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @Column(name = "monto_cierre_esperado")
    private BigDecimal montoCierreEsperado;

    @Column(name = "monto_cierre_real")
    private BigDecimal montoCierreReal;

    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCaja estado = EstadoCaja.ABIERTA;

    public enum EstadoCaja {
        ABIERTA, CERRADA
    }
}
