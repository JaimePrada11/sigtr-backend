-- SIGTR - Fase 1 (MVP) - Esquema inicial
-- Todo tenant_id queda listo para multi-tenant aunque hoy solo exista tenant_id = 1

CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    nombre          VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             VARCHAR(20) NOT NULL CHECK (rol IN ('DUENO', 'CAJERO')),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE clientes (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    telefono        VARCHAR(30),
    limite_credito  NUMERIC(14,2) NOT NULL DEFAULT 0,
    saldo_actual    NUMERIC(14,2) NOT NULL DEFAULT 0,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);
CREATE INDEX idx_clientes_tenant ON clientes(tenant_id);

CREATE TABLE productos (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    categoria       VARCHAR(20) NOT NULL,
    precio_venta    NUMERIC(14,2) NOT NULL,
    costo           NUMERIC(14,2) NOT NULL DEFAULT 0,
    unidad_medida   VARCHAR(10) NOT NULL DEFAULT 'UNIDAD',
    stock_actual    NUMERIC(14,3) NOT NULL DEFAULT 0,
    stock_minimo    NUMERIC(14,3) NOT NULL DEFAULT 0,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);
CREATE INDEX idx_productos_tenant ON productos(tenant_id);

CREATE TABLE caja_sesiones (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    usuario_id              BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_apertura          TIMESTAMPTZ NOT NULL,
    monto_apertura          NUMERIC(14,2) NOT NULL,
    fecha_cierre            TIMESTAMPTZ,
    monto_cierre_esperado   NUMERIC(14,2),
    monto_cierre_real       NUMERIC(14,2),
    diferencia              NUMERIC(14,2),
    estado                  VARCHAR(10) NOT NULL CHECK (estado IN ('ABIERTA', 'CERRADA'))
);
CREATE INDEX idx_caja_tenant_estado ON caja_sesiones(tenant_id, estado);

CREATE TABLE ventas (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    cliente_id      BIGINT REFERENCES clientes(id),
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    caja_sesion_id  BIGINT NOT NULL REFERENCES caja_sesiones(id),
    forma_pago      VARCHAR(10) NOT NULL CHECK (forma_pago IN ('CONTADO', 'FIADO')),
    total           NUMERIC(14,2) NOT NULL DEFAULT 0,
    estado          VARCHAR(15) NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ventas_tenant ON ventas(tenant_id);
CREATE INDEX idx_ventas_cliente ON ventas(cliente_id);

CREATE TABLE detalle_ventas (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    venta_id        BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id     BIGINT NOT NULL REFERENCES productos(id),
    cantidad        NUMERIC(14,3) NOT NULL,
    precio_unitario NUMERIC(14,2) NOT NULL,
    subtotal        NUMERIC(14,2) NOT NULL
);
CREATE INDEX idx_detalle_venta ON detalle_ventas(venta_id);

CREATE TABLE abonos_cartera (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    client_uuid     VARCHAR(36) NOT NULL UNIQUE,
    cliente_id      BIGINT NOT NULL REFERENCES clientes(id),
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id),
    caja_sesion_id  BIGINT NOT NULL REFERENCES caja_sesiones(id),
    monto           NUMERIC(14,2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_abonos_cliente ON abonos_cartera(cliente_id);

CREATE TABLE movimientos_caja (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(60) NOT NULL UNIQUE,
    caja_sesion_id      BIGINT NOT NULL REFERENCES caja_sesiones(id),
    tipo                VARCHAR(10) NOT NULL CHECK (tipo IN ('VENTA', 'ABONO', 'RETIRO', 'GASTO', 'INGRESO')),
    monto               NUMERIC(14,2) NOT NULL,
    descripcion         VARCHAR(255),
    referencia_tipo     VARCHAR(30),
    referencia_id       BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_movcaja_sesion ON movimientos_caja(caja_sesion_id);

-- Seed inicial: un solo tenant, un usuario dueno.
-- IMPORTANTE: el password_hash de abajo es un PLACEHOLDER, no corresponde a
-- ninguna contrasena real. Genera el hash real con BCryptPasswordEncoder
-- (ver README, seccion "Crear el primer usuario") y reemplaza este INSERT
-- antes de correr la migracion.
INSERT INTO usuarios (tenant_id, nombre, email, password_hash, rol)
VALUES (1, 'Dueno', 'dueno@sigtr.local', '$2a$10$cbyQwtJk6iJEMkRNIXheNu1a4/bmzlka.EQSwv5W.SuJbXMy7ROXO', 'DUENO');
