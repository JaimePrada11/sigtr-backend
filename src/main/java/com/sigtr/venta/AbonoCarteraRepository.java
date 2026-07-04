package com.sigtr.venta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AbonoCarteraRepository extends JpaRepository<AbonoCartera, Long> {
    Optional<AbonoCartera> findByClientUuid(String clientUuid);

    List<AbonoCartera> findByTenantIdAndClienteIdOrderByCreatedAtAsc(Long tenantId, Long clienteId);

    List<AbonoCartera> findByTenantIdAndCreatedAtBetween(Long tenantId, Instant desde, Instant hasta);
}
