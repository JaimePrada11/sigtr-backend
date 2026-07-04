package com.sigtr.inventario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByTenantIdAndProductoIdOrderByCreatedAtDesc(Long tenantId, Long productoId);

    List<MovimientoInventario> findByTenantIdAndTipoAndCreatedAtBetween(
            Long tenantId, MovimientoInventario.TipoMovimientoInventario tipo, Instant desde, Instant hasta);
}
