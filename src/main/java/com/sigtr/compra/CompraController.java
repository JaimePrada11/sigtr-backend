package com.sigtr.compra;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraDtos.CompraResponse registrar(@Valid @RequestBody CompraDtos.CrearCompraRequest req) {
        return CompraDtos.CompraResponse.from(compraService.registrar(req));
    }

    @GetMapping("/{id}")
    public CompraDtos.CompraResponse obtener(@PathVariable Long id) {
        return CompraDtos.CompraResponse.from(compraService.obtener(id));
    }
}
