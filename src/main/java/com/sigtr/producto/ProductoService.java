package com.sigtr.producto;

import com.sigtr.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final HistorialPrecioRepository historialPrecioRepository;

    // Fase 1/2: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public Producto crear(ProductoController.ProductoRequest req) {
        Producto producto = productoRepository.findByClientUuid(req.clientUuid())
                .orElseGet(Producto::new);

        boolean esNuevo = producto.getId() == null;

        producto.setTenantId(TenantContext.get());
        producto.setClientUuid(req.clientUuid());
        producto.setNombre(req.nombre());
        producto.setCategoria(req.categoria());
        producto.setPrecioVenta(req.precioVenta());
        producto.setCosto(req.costo() != null ? req.costo() : BigDecimal.ZERO);
        producto.setUnidadMedida(req.unidadMedida() != null ? req.unidadMedida() : Producto.UnidadMedida.UNIDAD);
        producto.setStockActual(req.stockActual() != null ? req.stockActual() : BigDecimal.ZERO);
        producto.setStockMinimo(req.stockMinimo() != null ? req.stockMinimo() : BigDecimal.ZERO);

        productoRepository.save(producto);

        if (esNuevo) {
            abrirHistorialPrecio(producto);
        }

        return producto;
    }

    @Transactional
    public Producto actualizar(Long id, ProductoController.ProductoRequest req) {
        Long tenantId = TenantContext.get();
        Producto producto = productoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));

        boolean cambioPrecio = producto.getPrecioVenta().compareTo(req.precioVenta()) != 0;

        producto.setNombre(req.nombre());
        producto.setCategoria(req.categoria());
        if (req.costo() != null) producto.setCosto(req.costo());
        if (req.unidadMedida() != null) producto.setUnidadMedida(req.unidadMedida());
        if (req.stockMinimo() != null) producto.setStockMinimo(req.stockMinimo());

        if (cambioPrecio) {
            cerrarHistorialVigente(tenantId, producto.getId());
            producto.setPrecioVenta(req.precioVenta());
            productoRepository.save(producto);
            abrirHistorialPrecio(producto);
        } else {
            productoRepository.save(producto);
        }

        return producto;
    }

    public List<HistorialPrecio> historialPrecios(Long productoId) {
        return historialPrecioRepository.findByTenantIdAndProductoIdOrderByVigenteDesdeDesc(
                TenantContext.get(), productoId);
    }

    private void abrirHistorialPrecio(Producto producto) {
        HistorialPrecio historial = new HistorialPrecio();
        historial.setTenantId(producto.getTenantId());
        historial.setProductoId(producto.getId());
        historial.setPrecio(producto.getPrecioVenta());
        historial.setVigenteDesde(Instant.now());
        historial.setUsuarioId(usuarioActualId());
        historialPrecioRepository.save(historial);
    }

    private void cerrarHistorialVigente(Long tenantId, Long productoId) {
        historialPrecioRepository.findByTenantIdAndProductoIdAndVigenteHastaIsNull(tenantId, productoId)
                .ifPresent(h -> {
                    h.setVigenteHasta(Instant.now());
                    historialPrecioRepository.save(h);
                });
    }
}
