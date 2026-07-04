package com.sigtr.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Superclase para toda entidad de negocio "sincronizable" desde el frontend offline.
 *
 * - tenantId: preparado para multi-tenant aunque hoy siempre sea el mismo valor.
 * - clientUuid: generado en el dispositivo ANTES de tener conexion. Es la clave
 *   de idempotencia que usa /api/sync/batch para no duplicar registros si la
 *   app reintenta el envio.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class TenantSyncEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @Column(name = "client_uuid", nullable = false, unique = true, updatable = false, length = 36)
    private String clientUuid;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
