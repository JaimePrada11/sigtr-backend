package com.sigtr.inventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByTenantIdAndProductoIdOrderByFechaVencimientoAscCreatedAtAsc(Long tenantId, Long productoId);

    // Lotes con existencia > 0, ordenados FIFO: primero el que vence antes
    // (o el mas antiguo si no tiene fecha de vencimiento).
    @Query("SELECT l FROM Lote l WHERE l.tenantId = :tenantId AND l.productoId = :productoId " +
           "AND l.cantidadActual > 0 " +
           "ORDER BY CASE WHEN l.fechaVencimiento IS NULL THEN 1 ELSE 0 END, l.fechaVencimiento ASC, l.createdAt ASC")
    List<Lote> findDisponiblesFifo(@Param("tenantId") Long tenantId, @Param("productoId") Long productoId);
}
