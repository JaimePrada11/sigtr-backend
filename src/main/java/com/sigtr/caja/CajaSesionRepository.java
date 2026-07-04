package com.sigtr.caja;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CajaSesionRepository extends JpaRepository<CajaSesion, Long> {
    Optional<CajaSesion> findByTenantIdAndEstado(Long tenantId, CajaSesion.EstadoCaja estado);
}
