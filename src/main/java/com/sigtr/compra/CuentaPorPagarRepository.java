package com.sigtr.compra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaPorPagarRepository extends JpaRepository<CuentaPorPagar, Long> {
    Optional<CuentaPorPagar> findByIdAndTenantId(Long id, Long tenantId);
    List<CuentaPorPagar> findByTenantIdAndProveedorId(Long tenantId, Long proveedorId);
    List<CuentaPorPagar> findByTenantIdAndProveedorIdAndEstadoNot(
            Long tenantId, Long proveedorId, CuentaPorPagar.EstadoCuentaPorPagar estado);
    List<CuentaPorPagar> findByTenantIdAndEstado(Long tenantId, CuentaPorPagar.EstadoCuentaPorPagar estado);
    List<CuentaPorPagar> findByTenantIdAndEstadoNot(Long tenantId, CuentaPorPagar.EstadoCuentaPorPagar estado);
}
