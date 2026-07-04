package com.sigtr.common;

/**
 * Fase 1: solo existe un tenant, pero toda la app ya lee el tenant activo
 * desde aqui en vez de asumir un valor fijo en el codigo de negocio.
 * El JwtAuthFilter es quien llena este valor a partir del claim del token.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    // Fase 1: tenant unico por defecto si aun no hay autenticacion (ej. seed inicial)
    public static final Long DEFAULT_TENANT_ID = 1L;

    private TenantContext() {
    }

    public static void set(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long get() {
        Long tenantId = CURRENT_TENANT.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
