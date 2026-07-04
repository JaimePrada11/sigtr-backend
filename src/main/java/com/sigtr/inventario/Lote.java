package com.sigtr.inventario;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "lotes")
public class Lote extends TenantSyncEntity {

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "numero_lote")
    private String numeroLote;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "costo_unitario", nullable = false)
    private BigDecimal costoUnitario;

    @Column(name = "cantidad_inicial", nullable = false)
    private BigDecimal cantidadInicial;

    // Se descuenta por FIFO a medida que se consume (venta, merma, etc.)
    @Column(name = "cantidad_actual", nullable = false)
    private BigDecimal cantidadActual;
}
