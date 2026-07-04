package com.sigtr.cuentaabierta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaAbiertaRepository extends JpaRepository<CuentaAbierta, Long> {
    Optional<CuentaAbierta> findByIdAndTenantId(Long id, Long tenantId);
}
