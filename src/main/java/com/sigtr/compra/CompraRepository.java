package com.sigtr.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    Optional<Compra> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Compra> findByClientUuid(String clientUuid);
    List<Compra> findByTenantIdAndProveedorIdOrderByCreatedAtDesc(Long tenantId, Long proveedorId);
}
