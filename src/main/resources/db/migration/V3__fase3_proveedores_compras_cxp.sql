-- SIGTR - Fase 3 - Proveedores, Ordenes de Compra, Compras, Cuentas por Pagar

CREATE TABLE proveedores (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    telefono        VARCHAR(30),
    email           VARCHAR(150),
    contacto        VARCHAR(150),
    direccion       VARCHAR(255),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_proveedores_tenant ON proveedores(tenant_id);

CREATE TABLE ordenes_compra (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    proveedor_id        BIGINT NOT NULL REFERENCES proveedores(id),
    usuario_id          BIGINT NOT NULL REFERENCES usuarios(id),
    estado              VARCHAR(15) NOT NULL DEFAULT 'BORRADOR'
                            CHECK (estado IN ('BORRADOR', 'PENDIENTE', 'PARCIAL', 'RECIBIDA', 'CANCELADA')),
    fecha_aprobacion    TIMESTAMPTZ,
    observaciones       VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_oc_tenant_proveedor ON ordenes_compra(tenant_id, proveedor_id);

CREATE TABLE detalle_orden_compra (
    id                          BIGSERIAL PRIMARY KEY,
    tenant_id                   BIGINT NOT NULL,
    orden_compra_id             BIGINT NOT NULL REFERENCES ordenes_compra(id) ON DELETE CASCADE,
    producto_id                 BIGINT NOT NULL REFERENCES productos(id),
    cantidad_pedida             NUMERIC(14,3) NOT NULL,
    cantidad_recibida           NUMERIC(14,3) NOT NULL DEFAULT 0,
    costo_unitario_estimado     NUMERIC(14,2) NOT NULL
);
CREATE INDEX idx_detalle_oc ON detalle_orden_compra(orden_compra_id);

CREATE TABLE compras (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    proveedor_id    BIGINT NOT NULL REFERENCES proveedores(id),
    orden_compra_id BIGINT REFERENCES ordenes_compra(id),
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    forma_pago      VARCHAR(10) NOT NULL CHECK (forma_pago IN ('CONTADO', 'CREDITO')),
    total           NUMERIC(14,2) NOT NULL DEFAULT 0,
    estado          VARCHAR(15) NOT NULL DEFAULT 'REGISTRADA' CHECK (estado IN ('REGISTRADA', 'ANULADA')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_compras_tenant_proveedor ON compras(tenant_id, proveedor_id);

CREATE TABLE detalle_compra (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    compra_id           BIGINT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    producto_id         BIGINT NOT NULL REFERENCES productos(id),
    cantidad            NUMERIC(14,3) NOT NULL,
    costo_unitario      NUMERIC(14,2) NOT NULL,
    subtotal            NUMERIC(14,2) NOT NULL,
    numero_lote         VARCHAR(50),
    fecha_vencimiento   DATE
);
CREATE INDEX idx_detalle_compra ON detalle_compra(compra_id);

CREATE TABLE cuentas_por_pagar (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(40) NOT NULL UNIQUE,
    proveedor_id        BIGINT NOT NULL REFERENCES proveedores(id),
    compra_id           BIGINT NOT NULL REFERENCES compras(id),
    monto_total         NUMERIC(14,2) NOT NULL,
    saldo_pendiente     NUMERIC(14,2) NOT NULL,
    fecha_vencimiento   DATE,
    estado              VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE'
                            CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cxp_tenant_proveedor ON cuentas_por_pagar(tenant_id, proveedor_id);
CREATE INDEX idx_cxp_estado ON cuentas_por_pagar(tenant_id, estado);

CREATE TABLE abonos_cuenta_por_pagar (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    client_uuid             VARCHAR(36) NOT NULL UNIQUE,
    cuenta_por_pagar_id     BIGINT NOT NULL REFERENCES cuentas_por_pagar(id),
    usuario_id              BIGINT NOT NULL REFERENCES usuarios(id),
    caja_sesion_id          BIGINT NOT NULL REFERENCES caja_sesiones(id),
    monto                   NUMERIC(14,2) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_abonoscxp_cxp ON abonos_cuenta_por_pagar(cuenta_por_pagar_id);
