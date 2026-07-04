package com.sigtr.prestamo;

import com.sigtr.agenda.Recordatorio;
import com.sigtr.agenda.RecordatorioService;
import com.sigtr.caja.CajaSesion;
import com.sigtr.caja.CajaSesionRepository;
import com.sigtr.caja.MovimientoCaja;
import com.sigtr.caja.MovimientoCajaRepository;
import com.sigtr.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final AbonoPrestamoRepository abonoPrestamoRepository;
    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final RecordatorioService recordatorioService;

    // Fase 1-4: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public Prestamo crear(PrestamoDtos.CrearPrestamoRequest req) {
        var existente = prestamoRepository.findByClientUuid(req.clientUuid());
        if (existente.isPresent()) {
            return existente.get();
        }

        Long tenantId = TenantContext.get();
        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay una sesion de caja abierta"));

        Prestamo prestamo = new Prestamo();
        prestamo.setTenantId(tenantId);
        prestamo.setClientUuid(req.clientUuid());
        prestamo.setTipo(req.tipo());
        prestamo.setNombrePersona(req.nombrePersona());
        prestamo.setClienteId(req.clienteId());
        prestamo.setMonto(req.monto());
        prestamo.setSaldoPendiente(req.monto());
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaVencimiento(req.fechaVencimiento());
        prestamo.setEstado(Prestamo.EstadoPrestamo.ACTIVO);
        prestamo.setUsuarioId(usuarioActualId());
        prestamo.setCajaSesionId(cajaAbierta.getId());
        prestamoRepository.save(prestamo);

        // OTORGADO: la plata sale de caja. RECIBIDO: la plata entra a caja.
        MovimientoCaja.TipoMovimiento tipoMovimiento = req.tipo() == Prestamo.TipoPrestamo.OTORGADO
                ? MovimientoCaja.TipoMovimiento.GASTO
                : MovimientoCaja.TipoMovimiento.INGRESO;
        registrarMovimiento(tenantId, cajaAbierta.getId(), req.clientUuid(), tipoMovimiento,
                req.monto(), "Prestamo " + req.tipo() + " a/de " + req.nombrePersona(),
                "PRESTAMO", prestamo.getId());

        if (req.fechaVencimiento() != null) {
            recordatorioService.generarAutomatico(Recordatorio.TipoRecordatorio.PRESTAMO,
                    "PRESTAMO", prestamo.getId(),
                    "Vencimiento de prestamo: " + req.nombrePersona(),
                    "Prestamo " + req.tipo() + " por " + req.monto(),
                    req.fechaVencimiento());
        }

        return prestamo;
    }

    @Transactional
    public AbonoPrestamo registrarAbono(Long prestamoId, String clientUuid, BigDecimal monto) {
        Long tenantId = TenantContext.get();
        Prestamo prestamo = prestamoRepository.findByIdAndTenantId(prestamoId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Prestamo no encontrado: " + prestamoId));

        if (monto.compareTo(prestamo.getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException("El abono no puede ser mayor al saldo pendiente");
        }

        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay una sesion de caja abierta"));

        AbonoPrestamo abono = new AbonoPrestamo();
        abono.setTenantId(tenantId);
        abono.setClientUuid(clientUuid);
        abono.setPrestamoId(prestamoId);
        abono.setMonto(monto);
        abono.setUsuarioId(usuarioActualId());
        abono.setCajaSesionId(cajaAbierta.getId());
        abonoPrestamoRepository.save(abono);

        prestamo.setSaldoPendiente(prestamo.getSaldoPendiente().subtract(monto));
        if (prestamo.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            prestamo.setEstado(Prestamo.EstadoPrestamo.PAGADO);
        }
        prestamoRepository.save(prestamo);

        // Abono a OTORGADO: nos pagan, entra plata. Abono a RECIBIDO: pagamos, sale plata.
        MovimientoCaja.TipoMovimiento tipoMovimiento = prestamo.getTipo() == Prestamo.TipoPrestamo.OTORGADO
                ? MovimientoCaja.TipoMovimiento.INGRESO
                : MovimientoCaja.TipoMovimiento.GASTO;
        registrarMovimiento(tenantId, cajaAbierta.getId(), clientUuid, tipoMovimiento, monto,
                "Abono a prestamo #" + prestamoId, "ABONO_PRESTAMO", prestamoId);

        return abono;
    }

    public List<Prestamo> listar(Prestamo.EstadoPrestamo estado) {
        Long tenantId = TenantContext.get();
        return estado != null
                ? prestamoRepository.findByTenantIdAndEstado(tenantId, estado)
                : prestamoRepository.findByTenantId(tenantId);
    }

    private void registrarMovimiento(Long tenantId, Long cajaSesionId, String clientUuid,
                                      MovimientoCaja.TipoMovimiento tipo, BigDecimal monto,
                                      String descripcion, String referenciaTipo, Long referenciaId) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(tenantId);
        movimiento.setClientUuid(clientUuid + "-mov-" + referenciaTipo);
        movimiento.setCajaSesionId(cajaSesionId);
        movimiento.setTipo(tipo);
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setReferenciaTipo(referenciaTipo);
        movimiento.setReferenciaId(referenciaId);
        movimientoCajaRepository.save(movimiento);
    }
}
