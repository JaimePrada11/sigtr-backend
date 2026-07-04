package com.sigtr.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByTenantIdAndActivoTrue(Long tenantId);

    Optional<Cliente> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Cliente> findByClientUuid(String clientUuid);

    List<Cliente> findByTenantIdAndNombreContainingIgnoreCase(Long tenantId, String nombre);
}
