package com.sigtr.compra;

import com.sigtr.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;

    // Fase 1/2/3: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public OrdenCompra crear(OrdenCompraDtos.CrearOrdenRequest req) {
        Long tenantId = TenantContext.get();

        OrdenCompra orden = new OrdenCompra();
        orden.setTenantId(tenantId);
        orden.setClientUuid(req.clientUuid());
        orden.setProveedorId(req.proveedorId());
        orden.setUsuarioId(usuarioActualId());
        orden.setObservaciones(req.observaciones());
        orden.setEstado(OrdenCompra.EstadoOrdenCompra.BORRADOR);

        for (OrdenCompraDtos.ItemOrden item : req.items()) {
            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setTenantId(tenantId);
            detalle.setOrdenCompra(orden);
            detalle.setProductoId(item.productoId());
            detalle.setCantidadPedida(item.cantidad());
            detalle.setCostoUnitarioEstimado(item.costoUnitarioEstimado());
            orden.getDetalles().add(detalle);
        }

        return ordenCompraRepository.save(orden);
    }

    @Transactional
    public OrdenCompra enviar(Long id) {
        OrdenCompra orden = obtener(id);
        if (orden.getEstado() != OrdenCompra.EstadoOrdenCompra.BORRADOR) {
            throw new IllegalStateException("Solo una orden en BORRADOR puede enviarse");
        }
        orden.setEstado(OrdenCompra.EstadoOrdenCompra.PENDIENTE);
        return ordenCompraRepository.save(orden);
    }

    @Transactional
    public OrdenCompra cancelar(Long id) {
        OrdenCompra orden = obtener(id);
        if (orden.getEstado() == OrdenCompra.EstadoOrdenCompra.RECIBIDA) {
            throw new IllegalStateException("No se puede cancelar una orden ya recibida");
        }
        orden.setEstado(OrdenCompra.EstadoOrdenCompra.CANCELADA);
        return ordenCompraRepository.save(orden);
    }

    public OrdenCompra obtener(Long id) {
        return ordenCompraRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new EntityNotFoundException("Orden de compra no encontrada: " + id));
    }
}
