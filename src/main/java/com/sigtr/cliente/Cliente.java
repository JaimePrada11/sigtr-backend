package com.sigtr.cliente;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente extends TenantSyncEntity {

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(name = "limite_credito", nullable = false)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    // Se actualiza en la misma transaccion que ventas fiadas y abonos.
    // Nunca se calcula sumando movimientos en tiempo real.
    @Column(name = "saldo_actual", nullable = false)
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
