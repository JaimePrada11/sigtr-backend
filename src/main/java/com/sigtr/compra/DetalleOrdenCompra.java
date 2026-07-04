package com.sigtr.compra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "detalle_orden_compra")
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_pedida", nullable = false)
    private BigDecimal cantidadPedida;

    @Column(name = "cantidad_recibida", nullable = false)
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Column(name = "costo_unitario_estimado", nullable = false)
    private BigDecimal costoUnitarioEstimado;
}
