package com.sigtr.caja;

import com.sigtr.cliente.ClienteRepository;
import com.sigtr.common.TenantContext;
import com.sigtr.compra.CuentaPorPagar;
import com.sigtr.compra.CuentaPorPagarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;

/**
 * Fotografia de ingresos/egresos reales de caja en un rango, mas una
 * proyeccion simple con lo que falta por cobrar (cartera de clientes) y por
 * pagar (cuentas por pagar a proveedores).
 */
@Service
@RequiredArgsConstructor
public class FlujoCajaService {

    private static final Set<MovimientoCaja.TipoMovimiento> TIPOS_INGRESO =
            EnumSet.of(MovimientoCaja.TipoMovimiento.VENTA, MovimientoCaja.TipoMovimiento.ABONO,
                    MovimientoCaja.TipoMovimiento.INGRESO);
    private static final Set<MovimientoCaja.TipoMovimiento> TIPOS_EGRESO =
            EnumSet.of(MovimientoCaja.TipoMovimiento.RETIRO, MovimientoCaja.TipoMovimiento.GASTO);

    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ClienteRepository clienteRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;

    public FlujoCajaDtos.FlujoCajaResponse calcular(LocalDate desde, LocalDate hasta) {
        Long tenantId = TenantContext.get();
        Instant desdeInstant = desde.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant hastaInstant = hasta.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        var movimientos = movimientoCajaRepository.findByTenantIdAndCreatedAtBetween(
                tenantId, desdeInstant, hastaInstant);

        BigDecimal ingresos = movimientos.stream()
                .filter(m -> TIPOS_INGRESO.contains(m.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal egresos = movimientos.stream()
                .filter(m -> TIPOS_EGRESO.contains(m.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal carteraPendiente = clienteRepository.findByTenantIdAndActivoTrue(tenantId).stream()
                .map(c -> c.getSaldoActual())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cuentasPorPagarPendientes = cuentaPorPagarRepository
                .findByTenantIdAndEstadoNot(tenantId, CuentaPorPagar.EstadoCuentaPorPagar.PAGADA).stream()
                .map(c -> c.getSaldoPendiente())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FlujoCajaDtos.FlujoCajaResponse(desde, hasta, ingresos, egresos,
                ingresos.subtract(egresos), carteraPendiente, cuentasPorPagarPendientes);
    }
}
