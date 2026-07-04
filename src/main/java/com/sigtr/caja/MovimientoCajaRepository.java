package com.sigtr.caja;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findByCajaSesionIdOrderByCreatedAtAsc(Long cajaSesionId);

    List<MovimientoCaja> findByTenantIdAndCreatedAtBetween(Long tenantId, Instant desde, Instant hasta);
}
