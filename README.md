# SIGTR Backend — Fase 1, 2, 3 y 4

Backend de la Fase 1 de SIGTR: Libreta Digital, Ventas simples, Productos básicos, Clientes, Caja.
Ver `SIGTR-Fase1-Diseno.md` (en la carpeta de entrega) para el diseño completo.

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 14+

> Nota: este proyecto no fue compilado ni ejecutado en el entorno donde se generó (sin acceso
> a Maven Central desde aquí). Antes de darlo por bueno, corre `mvn clean install` localmente
> y revisa que no haya errores de compilación.
>
> **Con 4 fases ya montadas sobre esta base sin haberla compilado ni una vez, este es un buen
> momento para parar y correr el build antes de seguir agregando código.** Cuanto más se
> acumula sin verificar, más cuesta encontrar el origen de un eventual error.

## 1. Crear la base de datos

```sql
CREATE DATABASE sigtr;
CREATE USER sigtr WITH PASSWORD 'sigtr';
GRANT ALL PRIVILEGES ON DATABASE sigtr TO sigtr;
```

Ajusta usuario/password en `src/main/resources/application.yml` si usas otros.

## 2. Crear el primer usuario (dueño)

El script `V1__init_schema.sql` trae un `INSERT` con un placeholder
`{{REEMPLAZAR_CON_HASH_BCRYPT_REAL}}` que **debes reemplazar antes de migrar**. Genera el hash así:

```java
// clase de un solo uso, o un test rapido
String hash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        .encode("tu-password-elegida");
System.out.println(hash);
```

Reemplaza el placeholder en `V1__init_schema.sql` con ese hash antes de correr la app por
primera vez (Flyway solo corre cada migración una vez, así que hazlo antes del primer arranque).

## 3. Levantar la aplicación

```bash
mvn spring-boot:run
```

Flyway aplica automáticamente `V1__init_schema.sql` al arrancar.

## 4. Probar el flujo completo (con curl o Postman)

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dueno@sigtr.local","password":"tu-password-elegida"}'
# -> guarda el "token" de la respuesta

TOKEN="pega-aqui-el-token"

# 2. Abrir caja
curl -X POST http://localhost:8080/api/caja/apertura \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"montoApertura": 100000}'

# 3. Crear un producto
curl -X POST http://localhost:8080/api/productos \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"clientUuid":"'$(uuidgen)'","nombre":"Poker","categoria":"CERVEZA","precioVenta":3500,"stockActual":100}'
# -> guarda el "id" del producto

# 4. Crear un cliente
curl -X POST http://localhost:8080/api/clientes \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"clientUuid":"'$(uuidgen)'","nombre":"Jairo","limiteCredito":50000}'
# -> guarda el "id" del cliente

# 5. Apuntar un fiado (Libreta Digital)
curl -X POST http://localhost:8080/api/ventas/libreta \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
        "clientUuid":"'$(uuidgen)'",
        "clienteId": 1,
        "items": [{"productoId": 1, "cantidad": 2}]
      }'

# 6. Ver el estado de cuenta del cliente
curl http://localhost:8080/api/clientes/1/cuenta -H "Authorization: Bearer $TOKEN"
```

## Fase 3 — qué se agregó

- **Límite de crédito: bloqueo duro, no solo alerta.** `VentaService.registrarVenta` ahora
  rechaza una venta fiada si `saldoActual + total > limiteCredito` (salvo que `limiteCredito`
  sea 0, que se interpreta como "sin límite definido"). Está dentro de la misma transacción
  que el descuento de inventario, así que si se rechaza, no queda nada a medias.
- **Estado de cuenta real**: `GET /api/clientes/{id}/cuenta` ya no es un stub — junta ventas
  fiadas y abonos en orden cronológico.
- **Alertas de cartera**: `GET /api/clientes/{id}/alertas` devuelve límite excedido, días en
  mora (proxy simple: días desde el último abono, o desde el fiado más antiguo si nunca ha
  abonado) y si el cliente es de alto riesgo.
- **Proveedores, Órdenes de Compra, Compras y Cuentas por Pagar**: una compra recepcionada
  genera automáticamente entradas de inventario reales (vía `InventarioService`, con lote y
  costo), actualiza el estado de la orden de compra si viene de una, y si es a crédito genera
  la cuenta por pagar correspondiente. Los abonos a proveedor generan un egreso de caja
  (tipo `GASTO` — el modelo de `MovimientoCaja` no distingue "gasto operativo" de "pago a
  proveedor"; si eso importa para tus reportes, vale la pena separarlo en una fase futura).

## Fase 4 — qué se agregó

- **Préstamos** (`OTORGADO`/`RECIBIDO`), separados del fiado porque son efectivo puro, no
  mercancía. No tocan `InventarioService` en absoluto.
- **Recordatorios generados automáticamente**, no solo capturados a mano:
  `CompraService` genera uno al crear una cuenta por pagar con fecha de vencimiento,
  `InventarioService` genera uno 7 días antes de que venza un lote, y `PrestamoService`
  genera uno si el préstamo tiene fecha de vencimiento. También se pueden crear tareas
  manuales sueltas.
- **Metas** con progreso **calculado**, no reportado a mano: `GET /api/metas/{id}/progreso`
  agrega datos reales de ventas, abonos o mermas del período — nunca puede desincronizarse
  de la realidad porque no se guarda un número, se recalcula cada vez que se consulta.
- **Flujo de caja** (`GET /api/flujo-caja?desde=&hasta=`): ingresos/egresos reales de ese
  rango más una proyección simple (cuánto falta por cobrar de clientes, cuánto falta por
  pagar a proveedores).

## Qué falta antes de producción (aunque ya funcione)

- **Roles reales**: hoy el filtro JWT arma la autoridad `ROLE_DUENO`/`ROLE_CAJERO` pero
  ningún endpoint todavía la usa con `@PreAuthorize`. Falta decidir qué puede hacer un cajero
  vs un dueño (ej. cerrar caja, editar precios).
- **`/api/sync/batch`**: el endpoint de sincronización offline por lotes descrito en el diseño
  no está implementado aún — hoy cada endpoint ya es idempotente por `clientUuid` individualmente,
  que es la base necesaria, pero falta el endpoint que reciba varios registros en un solo POST.
- **Tests**: no hay tests todavía. Antes de tocar la lógica de `VentaService`, `CajaService`
  o `InventarioService` (donde vive el dinero y el stock), vale la pena escribir tests de
  saldo, cuadre de caja y consumo FIFO de lotes.
- **CORS**: si el frontend corre en otro puerto/dominio, falta configurar CORS en `SecurityConfig`.
- **Reportes de vencimientos próximos**: los lotes ya guardan `fecha_vencimiento`, pero
  todavía no hay un endpoint que liste "productos por vencer en los próximos N días" — es
  una consulta simple sobre `lotes` que vale la pena agregar pronto, antes de la Fase 5
  (Agenda y Recordatorios) que la va a necesitar.

## Estructura del proyecto

```
com.sigtr
├── agenda          # Fase 4: recordatorios manuales y automaticos
├── auth            # Usuario, login, JWT
├── caja            # Sesiones de caja, movimientos, flujo de caja
├── cliente         # Clientes, estado de cuenta real, alertas de cartera
├── common          # TenantSyncEntity, TenantContext (base multi-tenant + offline)
├── compra          # Fase 3: proveedores, órdenes de compra, compras, cuentas por pagar
├── config          # Seguridad, auditoría JPA, manejo de errores
├── cuentaabierta   # Fase 2: mesas de consumo grupal, división y fiado automático
├── inventario      # Fase 2: fuente de verdad del stock, lotes FIFO
├── meta            # Fase 4: metas con progreso calculado contra datos reales
├── prestamo        # Fase 4: prestamos otorgados y recibidos (efectivo, no mercancia)
├── producto        # Catálogo de productos + historial de precios
├── proveedor       # Fase 3: catálogo de proveedores
└── venta           # Ventas, detalle de venta, Libreta Digital, abonos
```

## Fase 2 — qué se agregó

- **Inventario (`InventarioService`)** es ahora la única fuente de verdad para cambios de
  stock. `VentaService` ya no toca `producto.stock_actual` directamente — se lo delega.
- **Lotes con consumo FIFO**: si un producto tiene lotes, las salidas (venta, merma,
  consumo propio, vencimiento) descuentan primero del lote que vence antes. Si el producto
  no tiene lotes (ej. productos dados de alta en Fase 1 sin este flujo), sigue funcionando
  igual que antes, sin lote asociado.
- **Historial de precios**: cada cambio de `precioVenta` en `PUT /api/productos/{id}` cierra
  el registro vigente y abre uno nuevo automáticamente. Consultable en
  `GET /api/productos/{id}/historial-precios`.
- **Cuentas abiertas**: `POST /api/cuentas-abiertas` para abrir, agregar participantes,
  registrar consumos (el stock sale del inventario en ese momento, no al cerrar),
  `GET .../division` para ver cuánto debe cada quien, y `POST .../cerrar` que fía
  automáticamente cualquier saldo pendiente al cliente asociado a cada participante.
  **Importante**: si un participante tiene saldo pendiente y no tiene cliente vinculado,
  el cierre se bloquea — hay que cobrar o vincular un cliente primero. Es una decisión de
  diseño deliberada para no perder plata "fiada a nadie".

### Nomenclatura de endpoints de inventario

El diseño original hablaba de un endpoint genérico "salidas". En el código quedó dividido
en rutas explícitas para que el motivo de cada movimiento sea inequívoco:
`POST /api/inventario/mermas`, `/consumo-propio`, `/vencimientos`, `/ajustes`, `/entradas`.
