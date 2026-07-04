package com.sigtr.proveedor;

import com.sigtr.common.TenantContext;
import com.sigtr.compra.Compra;
import com.sigtr.compra.CompraDtos;
import com.sigtr.compra.CompraRepository;
import com.sigtr.compra.CuentaPorPagar;
import com.sigtr.compra.CuentaPorPagarDtos;
import com.sigtr.compra.CuentaPorPagarRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;

    @GetMapping
    public List<ProveedorDtos.ProveedorResponse> listar() {
        return proveedorRepository.findByTenantIdAndActivoTrue(TenantContext.get()).stream()
                .map(ProveedorDtos.ProveedorResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProveedorDtos.ProveedorResponse crear(@Valid @RequestBody ProveedorDtos.ProveedorRequest req) {
        Proveedor proveedor = proveedorRepository.findByClientUuid(req.clientUuid()).orElseGet(Proveedor::new);
        proveedor.setTenantId(TenantContext.get());
        proveedor.setClientUuid(req.clientUuid());
        proveedor.setNombre(req.nombre());
        proveedor.setTelefono(req.telefono());
        proveedor.setEmail(req.email());
        proveedor.setContacto(req.contacto());
        proveedor.setDireccion(req.direccion());
        return ProveedorDtos.ProveedorResponse.from(proveedorRepository.save(proveedor));
    }

    @PutMapping("/{id}")
    public ProveedorDtos.ProveedorResponse actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ProveedorDtos.ProveedorRequest req) {
        Proveedor proveedor = proveedorRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado: " + id));
        proveedor.setNombre(req.nombre());
        proveedor.setTelefono(req.telefono());
        proveedor.setEmail(req.email());
        proveedor.setContacto(req.contacto());
        proveedor.setDireccion(req.direccion());
        return ProveedorDtos.ProveedorResponse.from(proveedorRepository.save(proveedor));
    }

    @GetMapping("/{id}/historial-compras")
    public List<CompraDtos.CompraResponse> historialCompras(@PathVariable Long id) {
        List<Compra> compras = compraRepository.findByTenantIdAndProveedorIdOrderByCreatedAtDesc(
                TenantContext.get(), id);
        return compras.stream().map(CompraDtos.CompraResponse::from).toList();
    }

    @GetMapping("/{id}/facturas-pendientes")
    public List<CuentaPorPagarDtos.CxpResponse> facturasPendientes(@PathVariable Long id) {
        List<CuentaPorPagar> cxps = cuentaPorPagarRepository.findByTenantIdAndProveedorIdAndEstadoNot(
                TenantContext.get(), id, CuentaPorPagar.EstadoCuentaPorPagar.PAGADA);
        return cxps.stream().map(CuentaPorPagarDtos.CxpResponse::from).toList();
    }
}
