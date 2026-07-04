package com.sigtr.cuentaabierta;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "cuentas_abiertas")
public class CuentaAbierta extends TenantSyncEntity {

    @Column(nullable = false)
    private String nombre; // ej. "Mesa 3"

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado = EstadoCuenta.ABIERTA;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    public enum EstadoCuenta {
        ABIERTA, CERRADA
    }
}
