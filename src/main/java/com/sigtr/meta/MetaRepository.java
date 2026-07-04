package com.sigtr.meta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaRepository extends JpaRepository<Meta, Long> {
    Optional<Meta> findByIdAndTenantId(Long id, Long tenantId);

    List<Meta> findByTenantId(Long tenantId);
}
