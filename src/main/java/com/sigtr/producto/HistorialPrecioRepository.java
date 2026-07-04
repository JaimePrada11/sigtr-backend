package com.sigtr.producto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistorialPrecioRepository extends JpaRepository<HistorialPrecio, Long> {
    List<HistorialPrecio> findByTenantIdAndProductoIdOrderByVigenteDesdeDesc(Long tenantId, Long productoId);

    Optional<HistorialPrecio> findByTenantIdAndProductoIdAndVigenteHastaIsNull(Long tenantId, Long productoId);
}
