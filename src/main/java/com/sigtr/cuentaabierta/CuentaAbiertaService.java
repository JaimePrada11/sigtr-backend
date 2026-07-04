package com.sigtr.cuentaabierta;

import com.sigtr.caja.CajaSesion;
import com.sigtr.caja.CajaSesionRepository;
import com.sigtr.caja.MovimientoCaja;
import com.sigtr.caja.MovimientoCajaRepository;
import com.sigtr.cliente.Cliente;
import com.sigtr.cliente.ClienteRepository;
import com.sigtr.common.TenantContext;
import com.sigtr.inventario.InventarioService;
import com.sigtr.inventario.MovimientoInventario;
import com.sigtr.producto.Producto;
import com.sigtr.producto.ProductoRepository;
import com.sigtr.venta.Venta;
import com.sigtr.venta.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cuentas abiertas: mesas de consumo grupal (ej. cerveza entre varios).
 * Cada participante consume distinto; al cerrar, lo no pagado se fia
 * automaticamente al cliente asociado a cada participante (si existe).
 */
@Service
@RequiredArgsConstructor
public class CuentaAbiertaService {

    private final CuentaAbiertaRepository cuentaAbiertaRepository;
    private final ParticipanteCuentaRepository participanteRepository;
    private final ConsumoCuentaRepository consumoRepository;
    private final PagoCuentaRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final VentaRepository ventaRepository;
    private final InventarioService inventarioService;

    // Fase 1/2: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public CuentaAbierta abrir(String clientUuid, String nombre) {
        Long tenantId = TenantContext.get();

        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay una sesion de caja abierta"));

        CuentaAbierta cuenta = new CuentaAbierta();
        cuenta.setTenantId(tenantId);
        cuenta.setClientUuid(clientUuid);
        cuenta.setNombre(nombre);
        cuenta.setUsuarioId(usuarioActualId());
        cuenta.setCajaSesionId(cajaAbierta.getId());
        cuenta.setEstado(CuentaAbierta.EstadoCuenta.ABIERTA);
        return cuentaAbiertaRepository.save(cuenta);
    }

    @Transactional
    public ParticipanteCuenta agregarParticipante(Long cuentaId, String nombre, Long clienteId) {
        CuentaAbierta cuenta = obtenerAbierta(cuentaId);

        ParticipanteCuenta participante = new ParticipanteCuenta();
        participante.setTenantId(cuenta.getTenantId());
        participante.setCuentaAbiertaId(cuentaId);
        participante.setNombre(nombre);
        participante.setClienteId(clienteId);
        return participanteRepository.save(participante);
    }

    @Transactional
    public ConsumoCuenta registrarConsumo(Long cuentaId, Long participanteId, Long productoId, BigDecimal cantidad) {
        CuentaAbierta cuenta = obtenerAbierta(cuentaId);
        Long tenantId = cuenta.getTenantId();

        participanteRepository.findByIdAndCuentaAbiertaId(participanteId, cuentaId)
                .orElseThrow(() -> new EntityNotFoundException("Participante no encontrado: " + participanteId));

        Producto producto = productoRepository.findByIdAndTenantId(productoId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));

        BigDecimal subtotal = producto.getPrecioVenta().multiply(cantidad);

        ConsumoCuenta consumo = new ConsumoCuenta();
        consumo.setTenantId(tenantId);
        consumo.setCuentaAbiertaId(cuentaId);
        consumo.setParticipanteId(participanteId);
        consumo.setProductoId(productoId);
        consumo.setCantidad(cantidad);
        consumo.setPrecioUnitario(producto.getPrecioVenta());
        consumo.setSubtotal(subtotal);
        consumo.setCreatedAt(Instant.now());
        consumoRepository.save(consumo);

        // El producto sale del inventario en el momento del consumo, no al cerrar la cuenta.
        inventarioService.registrarSalida(productoId, cantidad,
                MovimientoInventario.TipoMovimientoInventario.VENTA, null,
                usuarioActualId(), "CUENTA_ABIERTA", cuentaId);

        return consumo;
    }

    /** Division por consumo real: cada quien paga lo que consumio, no partes iguales. */
    public CuentaAbiertaDtos.DivisionResponse calcularDivision(Long cuentaId) {
        CuentaAbierta cuenta = obtenerAbierta(cuentaId);

        List<ParticipanteCuenta> participantes = participanteRepository.findByCuentaAbiertaId(cuentaId);
        List<ConsumoCuenta> consumos = consumoRepository.findByCuentaAbiertaId(cuentaId);
        List<PagoCuenta> pagos = pagoRepository.findByCuentaAbiertaId(cuentaId);

        Map<Long, BigDecimal> consumoPorParticipante = consumos.stream()
                .collect(Collectors.groupingBy(ConsumoCuenta::getParticipanteId,
                        Collectors.reducing(BigDecimal.ZERO, ConsumoCuenta::getSubtotal, BigDecimal::add)));

        Map<Long, BigDecimal> pagoIndividualPorParticipante = pagos.stream()
                .filter(p -> p.getParticipanteId() != null)
                .collect(Collectors.groupingBy(PagoCuenta::getParticipanteId,
                        Collectors.reducing(BigDecimal.ZERO, PagoCuenta::getMonto, BigDecimal::add)));

        BigDecimal totalPagosGrupales = pagos.stream()
                .filter(p -> p.getParticipanteId() == null)
                .map(PagoCuenta::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalConsumido = consumoPorParticipante.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Saldo de cada participante antes de aplicar pagos grupales.
        BigDecimal saldoPrevioTotal = BigDecimal.ZERO;
        java.util.Map<Long, BigDecimal> saldoPrevio = new java.util.LinkedHashMap<>();
        for (ParticipanteCuenta p : participantes) {
            BigDecimal consumido = consumoPorParticipante.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal pagadoIndividual = pagoIndividualPorParticipante.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal saldo = consumido.subtract(pagadoIndividual).max(BigDecimal.ZERO);
            saldoPrevio.put(p.getId(), saldo);
            saldoPrevioTotal = saldoPrevioTotal.add(saldo);
        }

        // El pago grupal se distribuye proporcional al saldo pendiente de cada uno.
        BigDecimal factorCubierto = saldoPrevioTotal.compareTo(BigDecimal.ZERO) > 0
                ? totalPagosGrupales.divide(saldoPrevioTotal, 6, RoundingMode.HALF_UP).min(BigDecimal.ONE)
                : BigDecimal.ZERO;

        List<CuentaAbiertaDtos.ParticipanteSaldo> detalle = participantes.stream().map(p -> {
            BigDecimal consumido = consumoPorParticipante.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal pagadoIndividual = pagoIndividualPorParticipante.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal saldoAntesGrupal = saldoPrevio.get(p.getId());
            BigDecimal cubiertoPorGrupal = saldoAntesGrupal.multiply(factorCubierto);
            BigDecimal saldoFinal = saldoAntesGrupal.subtract(cubiertoPorGrupal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pagadoTotal = pagadoIndividual.add(cubiertoPorGrupal).setScale(2, RoundingMode.HALF_UP);

            return new CuentaAbiertaDtos.ParticipanteSaldo(
                    p.getId(), p.getNombre(), p.getClienteId(), consumido, pagadoTotal, saldoFinal);
        }).toList();

        BigDecimal totalPagado = pagoIndividualPorParticipante.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(totalPagosGrupales);
        BigDecimal totalPendiente = totalConsumido.subtract(totalPagado).max(BigDecimal.ZERO);

        return new CuentaAbiertaDtos.DivisionResponse(cuentaId, totalConsumido, totalPagado, totalPendiente, detalle);
    }

    @Transactional
    public PagoCuenta registrarPago(Long cuentaId, String clientUuid, Long participanteId,
                                     BigDecimal monto, PagoCuenta.FormaPago formaPago) {
        CuentaAbierta cuenta = obtenerAbierta(cuentaId);

        PagoCuenta pago = new PagoCuenta();
        pago.setTenantId(cuenta.getTenantId());
        pago.setClientUuid(clientUuid);
        pago.setCuentaAbiertaId(cuentaId);
        pago.setParticipanteId(participanteId);
        pago.setMonto(monto);
        pago.setFormaPago(formaPago);
        pagoRepository.save(pago);

        if (formaPago == PagoCuenta.FormaPago.CONTADO) {
            MovimientoCaja movimiento = new MovimientoCaja();
            movimiento.setTenantId(cuenta.getTenantId());
            movimiento.setClientUuid(clientUuid + "-mov");
            movimiento.setCajaSesionId(cuenta.getCajaSesionId());
            movimiento.setTipo(MovimientoCaja.TipoMovimiento.VENTA);
            movimiento.setMonto(monto);
            movimiento.setDescripcion("Pago cuenta abierta: " + cuenta.getNombre());
            movimiento.setReferenciaTipo("PAGO_CUENTA_ABIERTA");
            movimiento.setReferenciaId(pago.getId());
            movimientoCajaRepository.save(movimiento);
        }
        // formaPago = FIADO se registra como pago aqui para que la division cuadre,
        // pero el saldo real del cliente se ajusta en cerrar() via el fiado automatico
        // de lo que quede pendiente -- no antes, para no fiar dos veces lo mismo.

        return pago;
    }

    /**
     * Cierra la cuenta. Todo saldo pendiente por participante se fia
     * automaticamente a su cliente asociado. Si un participante tiene saldo
     * pendiente sin cliente asociado, se bloquea el cierre: hay que cobrar
     * o vincular un cliente antes de cerrar.
     */
    @Transactional
    public CuentaAbierta cerrar(Long cuentaId) {
        CuentaAbierta cuenta = obtenerAbierta(cuentaId);
        CuentaAbiertaDtos.DivisionResponse division = calcularDivision(cuentaId);

        List<CuentaAbiertaDtos.ParticipanteSaldo> sinCliente = division.porParticipante().stream()
                .filter(p -> p.saldoPendiente().compareTo(BigDecimal.ZERO) > 0 && p.clienteId() == null)
                .toList();

        if (!sinCliente.isEmpty()) {
            String nombres = sinCliente.stream()
                    .map(CuentaAbiertaDtos.ParticipanteSaldo::nombre)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "No se puede cerrar: " + nombres + " tienen saldo pendiente sin cliente asociado. " +
                    "Cobra el saldo o vincula un cliente a ese participante antes de cerrar.");
        }

        for (CuentaAbiertaDtos.ParticipanteSaldo p : division.porParticipante()) {
            if (p.saldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
                fiarSaldoPendiente(cuenta, p.clienteId(), p.saldoPendiente());
            }
        }

        cuenta.setEstado(CuentaAbierta.EstadoCuenta.CERRADA);
        cuenta.setFechaCierre(Instant.now());
        return cuentaAbiertaRepository.save(cuenta);
    }

    /**
     * Genera el fiado automatico al cerrar. No pasa por VentaService.registrarVenta
     * porque el stock de estos productos ya salio del inventario al momento del
     * consumo (registrarConsumo) -- aqui solo se ajusta caja y cartera del cliente.
     */
    private void fiarSaldoPendiente(CuentaAbierta cuenta, Long clienteId, BigDecimal monto) {
        Cliente cliente = clienteRepository.findByIdAndTenantId(clienteId, cuenta.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));
        cliente.setSaldoActual(cliente.getSaldoActual().add(monto));
        clienteRepository.save(cliente);

        Venta venta = new Venta();
        venta.setTenantId(cuenta.getTenantId());
        venta.setClientUuid(UUID.randomUUID().toString());
        venta.setClienteId(clienteId);
        venta.setUsuarioId(usuarioActualId());
        venta.setCajaSesionId(cuenta.getCajaSesionId());
        venta.setFormaPago(Venta.FormaPago.FIADO);
        venta.setTotal(monto);
        venta.setEstado(Venta.EstadoVenta.COMPLETADA);
        ventaRepository.save(venta);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(cuenta.getTenantId());
        movimiento.setClientUuid(venta.getClientUuid() + "-mov");
        movimiento.setCajaSesionId(cuenta.getCajaSesionId());
        movimiento.setTipo(MovimientoCaja.TipoMovimiento.VENTA);
        movimiento.setMonto(monto);
        movimiento.setDescripcion("Fiado automatico al cerrar cuenta abierta: " + cuenta.getNombre());
        movimiento.setReferenciaTipo("CUENTA_ABIERTA_FIADO");
        movimiento.setReferenciaId(venta.getId());
        movimientoCajaRepository.save(movimiento);
    }

    private CuentaAbierta obtenerAbierta(Long cuentaId) {
        CuentaAbierta cuenta = cuentaAbiertaRepository.findByIdAndTenantId(cuentaId, TenantContext.get())
                .orElseThrow(() -> new EntityNotFoundException("Cuenta abierta no encontrada: " + cuentaId));
        if (cuenta.getEstado() == CuentaAbierta.EstadoCuenta.CERRADA) {
            throw new IllegalStateException("La cuenta ya esta cerrada");
        }
        return cuenta;
    }
}
