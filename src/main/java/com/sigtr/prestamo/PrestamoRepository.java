package com.sigtr.prestamo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    Optional<Prestamo> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Prestamo> findByClientUuid(String clientUuid);
    List<Prestamo> findByTenantIdAndEstado(Long tenantId, Prestamo.EstadoPrestamo estado);
    List<Prestamo> findByTenantId(Long tenantId);
}
