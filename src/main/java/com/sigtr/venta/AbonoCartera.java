package com.sigtr.venta;

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
@Table(name = "abonos_cartera")
public class AbonoCartera extends TenantSyncEntity {

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;

    @Column(nullable = false)
    private BigDecimal monto;
}
