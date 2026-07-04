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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "ordenes_compra")
public class OrdenCompra extends TenantSyncEntity {

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrdenCompra estado = EstadoOrdenCompra.BORRADOR;

    @Column(name = "fecha_aprobacion")
    private Instant fechaAprobacion;

    private String observaciones;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompra> detalles = new ArrayList<>();

    public enum EstadoOrdenCompra {
        BORRADOR, PENDIENTE, PARCIAL, RECIBIDA, CANCELADA
    }
}
