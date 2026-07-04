package com.sigtr.venta;

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
@Table(name = "ventas")
public class Venta extends TenantSyncEntity {

    // null = venta de mostrador a alguien no registrado (siempre CONTADO en ese caso)
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "caja_sesion_id", nullable = false)
    private Long cajaSesionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", nullable = false)
    private FormaPago formaPago;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public enum FormaPago {
        CONTADO, FIADO
    }

    public enum EstadoVenta {
        COMPLETADA, ANULADA
    }
}
