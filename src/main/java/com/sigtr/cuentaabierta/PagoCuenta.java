package com.sigtr.cuentaabierta;

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
@Table(name = "pagos_cuenta")
public class PagoCuenta extends TenantSyncEntity {

    @Column(name = "cuenta_abierta_id", nullable = false)
    private Long cuentaAbiertaId;

    // null = pago grupal (cubre a varios participantes, se aplica en orden de registro)
    @Column(name = "participante_id")
    private Long participanteId;

    @Column(nullable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", nullable = false)
    private FormaPago formaPago;

    public enum FormaPago {
        CONTADO, FIADO
    }
}
