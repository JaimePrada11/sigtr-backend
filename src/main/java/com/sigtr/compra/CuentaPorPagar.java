package com.sigtr.compra;

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
@Table(name = "cuentas_por_pagar")
public class CuentaPorPagar extends TenantSyncEntity {

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    @Column(name = "compra_id", nullable = false)
    private Long compraId;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "saldo_pendiente", nullable = false)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuentaPorPagar estado = EstadoCuentaPorPagar.PENDIENTE;

    public enum EstadoCuentaPorPagar {
        PENDIENTE, PAGADA, VENCIDA
    }
}
