package com.sigtr.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    Optional<OrdenCompra> findByIdAndTenantId(Long id, Long tenantId);
}
