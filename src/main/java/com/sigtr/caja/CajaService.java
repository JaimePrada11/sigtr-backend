package com.sigtr.caja;

import com.sigtr.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CajaService {

    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;

    @Transactional
    public CajaSesion abrir(BigDecimal montoApertura) {
        Long tenantId = TenantContext.get();

        cajaSesionRepository.findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .ifPresent(s -> {
                    throw new IllegalStateException("Ya existe una sesion de caja abierta (id=" + s.getId() + ")");
                });

        CajaSesion sesion = new CajaSesion();
        sesion.setTenantId(tenantId);
        sesion.setUsuarioId(usuarioActualId());
        sesion.setFechaApertura(Instant.now());
        sesion.setMontoApertura(montoApertura);
        sesion.setEstado(CajaSesion.EstadoCaja.ABIERTA);
        return cajaSesionRepository.save(sesion);
    }

    @Transactional
    public CajaSesion cerrar(Long sesionId, BigDecimal montoCierreReal) {
        Long tenantId = TenantContext.get();
        CajaSesion sesion = cajaSesionRepository.findById(sesionId)
                .filter(s -> s.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EntityNotFoundException("Sesion de caja no encontrada: " + sesionId));

        BigDecimal totalMovimientos = movimientoCajaRepository
                .findByCajaSesionIdOrderByCreatedAtAsc(sesionId).stream()
                .map(m -> signo(m.getTipo()).multiply(m.getMonto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal esperado = sesion.getMontoApertura().add(totalMovimientos);

        sesion.setFechaCierre(Instant.now());
        sesion.setMontoCierreEsperado(esperado);
        sesion.setMontoCierreReal(montoCierreReal);
        sesion.setDiferencia(montoCierreReal.subtract(esperado));
        sesion.setEstado(CajaSesion.EstadoCaja.CERRADA);

        return cajaSesionRepository.save(sesion);
    }

    public CajaSesion actual() {
        return cajaSesionRepository
                .findByTenantIdAndEstado(TenantContext.get(), CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new EntityNotFoundException("No hay sesion de caja abierta"));
    }

    @Transactional
    public MovimientoCaja registrarMovimiento(String clientUuid, MovimientoCaja.TipoMovimiento tipo,
                                               BigDecimal monto, String descripcion) {
        CajaSesion sesion = actual();

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(TenantContext.get());
        movimiento.setClientUuid(clientUuid);
        movimiento.setCajaSesionId(sesion.getId());
        movimiento.setTipo(tipo);
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        return movimientoCajaRepository.save(movimiento);
    }

    private BigDecimal signo(MovimientoCaja.TipoMovimiento tipo) {
        return switch (tipo) {
            case VENTA, ABONO, INGRESO -> BigDecimal.ONE;
            case RETIRO, GASTO -> BigDecimal.valueOf(-1);
        };
    }

    // Fase 1: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }
}
