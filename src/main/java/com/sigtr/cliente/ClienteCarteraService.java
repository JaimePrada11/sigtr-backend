package com.sigtr.cliente;

import com.sigtr.common.TenantContext;
import com.sigtr.venta.AbonoCartera;
import com.sigtr.venta.AbonoCarteraRepository;
import com.sigtr.venta.Venta;
import com.sigtr.venta.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Cartera avanzada (Fase 3): estado de cuenta real (antes era un stub vacio
 * en Fase 1) y alertas de limite de credito / mora.
 */
@Service
@RequiredArgsConstructor
public class ClienteCarteraService {

    // Umbral simple para "cliente de alto riesgo". Ajustable segun se valide con uso real.
    private static final long DIAS_MORA_ALTO_RIESGO = 30;

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final AbonoCarteraRepository abonoCarteraRepository;

    public ClienteDtos.EstadoCuentaResponse estadoCuenta(Long clienteId) {
        Cliente cliente = obtener(clienteId);
        Long tenantId = TenantContext.get();

        List<Venta> fiados = ventaRepository.findByTenantIdAndClienteIdAndFormaPagoOrderByCreatedAtAsc(
                tenantId, clienteId, Venta.FormaPago.FIADO);
        List<AbonoCartera> abonos = abonoCarteraRepository.findByTenantIdAndClienteIdOrderByCreatedAtAsc(
                tenantId, clienteId);

        List<ClienteDtos.MovimientoCuenta> movimientos = new java.util.ArrayList<>();
        fiados.forEach(v -> movimientos.add(
                new ClienteDtos.MovimientoCuenta("VENTA_FIADA", v.getTotal(), v.getCreatedAt())));
        abonos.forEach(a -> movimientos.add(
                new ClienteDtos.MovimientoCuenta("ABONO", a.getMonto(), a.getCreatedAt())));
        movimientos.sort(Comparator.comparing(ClienteDtos.MovimientoCuenta::fecha));

        return new ClienteDtos.EstadoCuentaResponse(
                cliente.getId(), cliente.getNombre(), cliente.getSaldoActual(),
                cliente.getLimiteCredito(), movimientos);
    }

    public ClienteDtos.AlertasResponse alertas(Long clienteId) {
        Cliente cliente = obtener(clienteId);
        Long tenantId = TenantContext.get();

        boolean limiteExcedido = cliente.getLimiteCredito().compareTo(BigDecimal.ZERO) > 0
                && cliente.getSaldoActual().compareTo(cliente.getLimiteCredito()) > 0;

        long diasMora = 0;
        if (cliente.getSaldoActual().compareTo(BigDecimal.ZERO) > 0) {
            List<AbonoCartera> abonos = abonoCarteraRepository
                    .findByTenantIdAndClienteIdOrderByCreatedAtAsc(tenantId, clienteId);
            List<Venta> fiados = ventaRepository.findByTenantIdAndClienteIdAndFormaPagoOrderByCreatedAtAsc(
                    tenantId, clienteId, Venta.FormaPago.FIADO);

            // Proxy simple: dias desde el ultimo abono, o desde el fiado mas antiguo
            // si nunca ha abonado. No es una conciliacion contable exacta linea por
            // linea, pero es suficiente para una alerta operativa.
            Instant referencia = !abonos.isEmpty()
                    ? abonos.get(abonos.size() - 1).getCreatedAt()
                    : fiados.isEmpty() ? null : fiados.get(0).getCreatedAt();

            if (referencia != null) {
                diasMora = ChronoUnit.DAYS.between(referencia, Instant.now());
            }
        }

        boolean altoRiesgo = limiteExcedido || diasMora >= DIAS_MORA_ALTO_RIESGO;

        return new ClienteDtos.AlertasResponse(clienteId, limiteExcedido, diasMora, altoRiesgo);
    }

    private Cliente obtener(Long clienteId) {
        return clienteRepository.findByIdAndTenantId(clienteId, TenantContext.get())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clienteId));
    }
}
