package com.sigtr.producto;

import com.sigtr.common.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;

    public record ProductoRequest(
            @NotBlank String clientUuid,
            @NotBlank String nombre,
            @NotNull Producto.CategoriaProducto categoria,
            @NotNull BigDecimal precioVenta,
            BigDecimal costo,
            Producto.UnidadMedida unidadMedida,
            BigDecimal stockActual,
            BigDecimal stockMinimo
    ) {
    }

    public record ProductoResponse(
            Long id, String nombre, String categoria, BigDecimal precioVenta,
            String unidadMedida, BigDecimal stockActual, BigDecimal stockMinimo, boolean activo
    ) {
        static ProductoResponse from(Producto p) {
            return new ProductoResponse(p.getId(), p.getNombre(), p.getCategoria().name(),
                    p.getPrecioVenta(), p.getUnidadMedida().name(), p.getStockActual(),
                    p.getStockMinimo(), p.isActivo());
        }
    }

    public record HistorialPrecioResponse(BigDecimal precio, Instant vigenteDesde, Instant vigenteHasta) {
        static HistorialPrecioResponse from(HistorialPrecio h) {
            return new HistorialPrecioResponse(h.getPrecio(), h.getVigenteDesde(), h.getVigenteHasta());
        }
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return productoRepository.findByTenantIdAndActivoTrue(TenantContext.get()).stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return ProductoResponse.from(productoService.crear(req));
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return ProductoResponse.from(productoService.actualizar(id, req));
    }

    /** Evolucion de precios: util para "papa a $1.300 hoy, $1.500 manana". */
    @GetMapping("/{id}/historial-precios")
    public List<HistorialPrecioResponse> historialPrecios(@PathVariable Long id) {
        return productoService.historialPrecios(id).stream()
                .map(HistorialPrecioResponse::from)
                .toList();
    }
}
