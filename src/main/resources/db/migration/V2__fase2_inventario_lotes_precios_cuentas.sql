-- SIGTR - Fase 2 - Inventario completo, Lotes, Historial de Precios, Cuentas Abiertas

CREATE TABLE lotes (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    producto_id         BIGINT NOT NULL REFERENCES productos(id),
    numero_lote         VARCHAR(50),
    fecha_ingreso       DATE NOT NULL,
    fecha_vencimiento   DATE,
    costo_unitario      NUMERIC(14,2) NOT NULL,
    cantidad_inicial    NUMERIC(14,3) NOT NULL,
    cantidad_actual     NUMERIC(14,3) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_lotes_producto ON lotes(tenant_id, producto_id);
CREATE INDEX idx_lotes_vencimiento ON lotes(fecha_vencimiento);

CREATE TABLE movimientos_inventario (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    producto_id         BIGINT NOT NULL REFERENCES productos(id),
    lote_id             BIGINT REFERENCES lotes(id),
    tipo                VARCHAR(20) NOT NULL CHECK (tipo IN
                            ('COMPRA', 'VENTA', 'MERMA', 'AJUSTE', 'CONSUMO_PROPIO', 'VENCIMIENTO')),
    cantidad            NUMERIC(14,3) NOT NULL,
    costo_unitario      NUMERIC(14,2),
    motivo              VARCHAR(255),
    usuario_id          BIGINT NOT NULL REFERENCES usuarios(id),
    referencia_tipo     VARCHAR(30),
    referencia_id       BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_movinv_producto ON movimientos_inventario(tenant_id, producto_id);

CREATE TABLE historial_precios (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL REFERENCES productos(id),
    precio          NUMERIC(14,2) NOT NULL,
    vigente_desde   TIMESTAMPTZ NOT NULL,
    vigente_hasta   TIMESTAMPTZ,
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id)
);
CREATE INDEX idx_histprecio_producto ON historial_precios(tenant_id, producto_id);

-- Backfill: cada producto ya existente en Fase 1 arranca con un registro de
-- historial "vigente desde ahora" para que la Fase 2 no rompa productos previos.
INSERT INTO historial_precios (tenant_id, producto_id, precio, vigente_desde, usuario_id)
SELECT tenant_id, id, precio_venta, now(), 1 FROM productos;

CREATE TABLE cuentas_abiertas (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL,
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    caja_sesion_id  BIGINT NOT NULL REFERENCES caja_sesiones(id),
    estado          VARCHAR(10) NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA', 'CERRADA')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_cierre    TIMESTAMPTZ
);
CREATE INDEX idx_cuentas_tenant_estado ON cuentas_abiertas(tenant_id, estado);

CREATE TABLE participantes_cuenta (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    cuenta_abierta_id   BIGINT NOT NULL REFERENCES cuentas_abiertas(id) ON DELETE CASCADE,
    nombre              VARCHAR(100) NOT NULL,
    cliente_id          BIGINT REFERENCES clientes(id)
);
CREATE INDEX idx_participantes_cuenta ON participantes_cuenta(cuenta_abierta_id);

CREATE TABLE consumos_cuenta (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    cuenta_abierta_id   BIGINT NOT NULL REFERENCES cuentas_abiertas(id) ON DELETE CASCADE,
    participante_id     BIGINT NOT NULL REFERENCES participantes_cuenta(id),
    producto_id         BIGINT NOT NULL REFERENCES productos(id),
    cantidad            NUMERIC(14,3) NOT NULL,
    precio_unitario     NUMERIC(14,2) NOT NULL,
    subtotal            NUMERIC(14,2) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_consumos_cuenta ON consumos_cuenta(cuenta_abierta_id);
CREATE INDEX idx_consumos_participante ON consumos_cuenta(participante_id);

CREATE TABLE pagos_cuenta (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    cuenta_abierta_id   BIGINT NOT NULL REFERENCES cuentas_abiertas(id) ON DELETE CASCADE,
    participante_id     BIGINT REFERENCES participantes_cuenta(id), -- null = pago grupal
    monto               NUMERIC(14,2) NOT NULL,
    forma_pago          VARCHAR(10) NOT NULL CHECK (forma_pago IN ('CONTADO', 'FIADO')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pagos_cuenta ON pagos_cuenta(cuenta_abierta_id);
