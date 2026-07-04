package com.sigtr.prestamo;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "abonos_prestamo")
public class AbonoPrestamo extends TenantSyncEntity {

    @Column(name = "prestamo_id", nullable = false)
    private Long prestamoId;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;
}
