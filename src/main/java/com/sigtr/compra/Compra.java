package com.sigtr.compra;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "compras")
public class Compra extends TenantSyncEntity {

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    // nullable: una compra puede registrarse sin orden de compra previa
    @Column(name = "orden_compra_id")
    private Long ordenCompraId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", nullable = false)
    private FormaPagoCompra formaPago;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado = EstadoCompra.REGISTRADA;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();

    public enum FormaPagoCompra {
        CONTADO, CREDITO
    }

    public enum EstadoCompra {
        REGISTRADA, ANULADA
    }
}
