package com.sigtr.caja;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja extends TenantSyncEntity {

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private BigDecimal monto;

    private String descripcion;

    @Column(name = "referencia_tipo")
    private String referenciaTipo; // "VENTA", "ABONO_CARTERA", etc.

    @Column(name = "referencia_id")
    private Long referenciaId;

    public enum TipoMovimiento {
        VENTA, ABONO, RETIRO, GASTO, INGRESO
    }
}
