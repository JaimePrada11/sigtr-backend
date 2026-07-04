package com.sigtr.compra;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenCompraDtos.OrdenResponse crear(@Valid @RequestBody OrdenCompraDtos.CrearOrdenRequest req) {
        return OrdenCompraDtos.OrdenResponse.from(ordenCompraService.crear(req));
    }

    @GetMapping("/{id}")
    public OrdenCompraDtos.OrdenResponse obtener(@PathVariable Long id) {
        return OrdenCompraDtos.OrdenResponse.from(ordenCompraService.obtener(id));
    }

    @PostMapping("/{id}/enviar")
    public OrdenCompraDtos.OrdenResponse enviar(@PathVariable Long id) {
        return OrdenCompraDtos.OrdenResponse.from(ordenCompraService.enviar(id));
    }

    @PostMapping("/{id}/cancelar")
    public OrdenCompraDtos.OrdenResponse cancelar(@PathVariable Long id) {
        return OrdenCompraDtos.OrdenResponse.from(ordenCompraService.cancelar(id));
    }
}
