package com.sigtr.producto;

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
@Table(name = "historial_precios")
public class HistorialPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "vigente_desde", nullable = false)
    private Instant vigenteDesde;

    @Column(name = "vigente_hasta")
    private Instant vigenteHasta; // null = precio actual

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
}
