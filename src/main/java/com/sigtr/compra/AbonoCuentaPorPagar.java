package com.sigtr.compra;

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
@Table(name = "abonos_cuenta_por_pagar")
public class AbonoCuentaPorPagar extends TenantSyncEntity {

    @Column(name = "cuenta_por_pagar_id", nullable = false)
    private Long cuentaPorPagarId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;

    @Column(nullable = false)
    private BigDecimal monto;
}
