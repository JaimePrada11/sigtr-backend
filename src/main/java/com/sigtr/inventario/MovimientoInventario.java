package com.sigtr.inventario;

import com.sigtr.common.TenantSyncEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario extends TenantSyncEntity {

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "lote_id")
    private Long loteId; // nullable: producto sin manejo de lotes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimientoInventario tipo;

    // Siempre positiva; el signo (entrada/salida) lo determina el tipo.
    @Column(nullable = false)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario")
    private BigDecimal costoUnitario;

    private String motivo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "referencia_tipo")
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    public enum TipoMovimientoInventario {
        COMPRA, VENTA, MERMA, AJUSTE, CONSUMO_PROPIO, VENCIMIENTO
    }

    /** true si el tipo suma al stock, false si resta. */
    public static boolean esEntrada(TipoMovimientoInventario tipo) {
        return tipo == TipoMovimientoInventario.COMPRA;
        // AJUSTE puede ser positivo o negativo -- se maneja aparte en el servicio.
    }
}
