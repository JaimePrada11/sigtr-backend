-- SIGTR - Fase 4 - Prestamos, Agenda/Recordatorios, Metas

CREATE TABLE prestamos (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    tipo                VARCHAR(10) NOT NULL CHECK (tipo IN ('OTORGADO', 'RECIBIDO')),
    nombre_persona      VARCHAR(150) NOT NULL,
    cliente_id          BIGINT REFERENCES clientes(id),
    monto               NUMERIC(14,2) NOT NULL,
    saldo_pendiente     NUMERIC(14,2) NOT NULL,
    fecha_prestamo      DATE NOT NULL,
    fecha_vencimiento   DATE,
    estado              VARCHAR(10) NOT NULL DEFAULT 'ACTIVO'
                            CHECK (estado IN ('ACTIVO', 'PAGADO', 'INCUMPLIDO')),
    usuario_id          BIGINT NOT NULL REFERENCES usuarios(id),
    caja_sesion_id      BIGINT NOT NULL REFERENCES caja_sesiones(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_prestamos_tenant_estado ON prestamos(tenant_id, estado);

CREATE TABLE abonos_prestamo (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    prestamo_id         BIGINT NOT NULL REFERENCES prestamos(id),
    monto               NUMERIC(14,2) NOT NULL,
    usuario_id          BIGINT NOT NULL REFERENCES usuarios(id),
    caja_sesion_id      BIGINT NOT NULL REFERENCES caja_sesiones(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_abonosprestamo_prestamo ON abonos_prestamo(prestamo_id);

CREATE TABLE recordatorios (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL,
    client_uuid             VARCHAR(36) NOT NULL UNIQUE,
    tipo                    VARCHAR(25) NOT NULL CHECK (tipo IN
                                ('PAGO_CLIENTE', 'PAGO_PROVEEDOR', 'VENCIMIENTO_PRODUCTO', 'PRESTAMO', 'TAREA')),
    referencia_tipo         VARCHAR(30),
    referencia_id           BIGINT,
    titulo                  VARCHAR(200) NOT NULL,
    descripcion             VARCHAR(500),
    fecha_recordatorio      DATE NOT NULL,
    estado                  VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
                                CHECK (estado IN ('PENDIENTE', 'COMPLETADO', 'DESCARTADO')),
    usuario_id              BIGINT NOT NULL REFERENCES usuarios(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recordatorios_tenant_estado ON recordatorios(tenant_id, estado);
CREATE INDEX idx_recordatorios_fecha ON recordatorios(fecha_recordatorio);

CREATE TABLE metas (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    client_uuid         VARCHAR(36) NOT NULL UNIQUE,
    tipo                VARCHAR(20) NOT NULL CHECK (tipo IN ('VENTAS', 'COBRO_CARTERA', 'REDUCCION_MERMAS')),
    descripcion         VARCHAR(255),
    valor_objetivo      NUMERIC(14,2) NOT NULL,
    periodo_inicio      DATE NOT NULL,
    periodo_fin         DATE NOT NULL,
    estado              VARCHAR(15) NOT NULL DEFAULT 'ACTIVA'
                            CHECK (estado IN ('ACTIVA', 'CUMPLIDA', 'VENCIDA', 'CANCELADA')),
    usuario_id          BIGINT NOT NULL REFERENCES usuarios(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_metas_tenant ON metas(tenant_id);
