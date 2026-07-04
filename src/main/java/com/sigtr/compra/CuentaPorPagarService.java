package com.sigtr.compra;

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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaPorPagarService {

    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final AbonoCuentaPorPagarRepository abonoRepository;
    private final CajaSesionRepository cajaSesionRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;

    // Fase 1/2/3: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    public List<CuentaPorPagar> listarPorProveedor(Long proveedorId) {
        return cuentaPorPagarRepository.findByTenantIdAndProveedorId(TenantContext.get(), proveedorId);
    }

    public List<CuentaPorPagar> listarPendientes() {
        return cuentaPorPagarRepository.findByTenantIdAndEstado(
                TenantContext.get(), CuentaPorPagar.EstadoCuentaPorPagar.PENDIENTE);
    }

    @Transactional
    public AbonoCuentaPorPagar registrarAbono(Long cuentaPorPagarId, String clientUuid, BigDecimal monto) {
        Long tenantId = TenantContext.get();
        CuentaPorPagar cxp = cuentaPorPagarRepository.findByIdAndTenantId(cuentaPorPagarId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta por pagar no encontrada: " + cuentaPorPagarId));

        if (monto.compareTo(cxp.getSaldoPendiente()) > 0) {
            throw new IllegalArgumentException("El abono no puede ser mayor al saldo pendiente");
        }

        CajaSesion cajaAbierta = cajaSesionRepository
                .findByTenantIdAndEstado(tenantId, CajaSesion.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay una sesion de caja abierta"));

        AbonoCuentaPorPagar abono = new AbonoCuentaPorPagar();
        abono.setTenantId(tenantId);
        abono.setClientUuid(clientUuid);
        abono.setCuentaPorPagarId(cuentaPorPagarId);
        abono.setUsuarioId(usuarioActualId());
        abono.setCajaSesionId(cajaAbierta.getId());
        abono.setMonto(monto);
        abonoRepository.save(abono);

        cxp.setSaldoPendiente(cxp.getSaldoPendiente().subtract(monto));
        if (cxp.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            cxp.setEstado(CuentaPorPagar.EstadoCuentaPorPagar.PAGADA);
        }
        cuentaPorPagarRepository.save(cxp);

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setTenantId(tenantId);
        movimiento.setClientUuid(clientUuid + "-mov");
        movimiento.setCajaSesionId(cajaAbierta.getId());
        movimiento.setTipo(MovimientoCaja.TipoMovimiento.GASTO);
        movimiento.setMonto(monto);
        movimiento.setDescripcion("Abono a proveedor, cuenta por pagar #" + cuentaPorPagarId);
        movimiento.setReferenciaTipo("ABONO_CUENTA_POR_PAGAR");
        movimiento.setReferenciaId(cxp.getId());
        movimientoCajaRepository.save(movimiento);

        return abono;
    }
}
