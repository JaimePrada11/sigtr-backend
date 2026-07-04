package com.sigtr.prestamo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @GetMapping
    public List<PrestamoDtos.PrestamoResponse> listar(@RequestParam(required = false) Prestamo.EstadoPrestamo estado) {
        return prestamoService.listar(estado).stream().map(PrestamoDtos.PrestamoResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoDtos.PrestamoResponse crear(@Valid @RequestBody PrestamoDtos.CrearPrestamoRequest req) {
        return PrestamoDtos.PrestamoResponse.from(prestamoService.crear(req));
    }

    @PostMapping("/{id}/abonos")
    @ResponseStatus(HttpStatus.CREATED)
    public void abonar(@PathVariable Long id, @Valid @RequestBody PrestamoDtos.AbonoRequest req) {
        prestamoService.registrarAbono(id, req.clientUuid(), req.monto());
    }
}
