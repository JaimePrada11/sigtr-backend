package com.sigtr.venta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByClientUuid(String clientUuid);

    List<Venta> findByTenantIdAndClienteIdAndFormaPagoOrderByCreatedAtAsc(
            Long tenantId, Long clienteId, Venta.FormaPago formaPago);

    List<Venta> findByTenantIdAndCreatedAtBetween(Long tenantId, Instant desde, Instant hasta);
}
