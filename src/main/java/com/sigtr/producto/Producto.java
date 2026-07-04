package com.sigtr.producto;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "productos")
public class Producto extends TenantSyncEntity {

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProducto categoria;

    @Column(name = "precio_venta", nullable = false)
    private BigDecimal precioVenta;

    @Column(nullable = false)
    private BigDecimal costo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    @Column(name = "stock_actual", nullable = false)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", nullable = false)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum CategoriaProducto {
        CERVEZA, ABARROTES, VERDURA, FRUTA, ASEO, BEBIDA, RETORNABLE, OTRO
    }

    public enum UnidadMedida {
        UNIDAD, KG, LIBRA, CAJA
    }
}
