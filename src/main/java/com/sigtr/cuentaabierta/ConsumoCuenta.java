package com.sigtr.cuentaabierta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "consumos_cuenta")
public class ConsumoCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "cuenta_abierta_id", nullable = false)
    private Long cuentaAbiertaId;

    @Column(name = "participante_id", nullable = false)
    private Long participanteId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private BigDecimal cantidad;

    // Copiado del producto al momento del consumo (mismo patron que detalle_ventas).
    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
