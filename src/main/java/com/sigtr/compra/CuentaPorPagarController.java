package com.sigtr.compra;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas-por-pagar")
@RequiredArgsConstructor
public class CuentaPorPagarController {

    private final CuentaPorPagarService cuentaPorPagarService;

    @GetMapping
    public List<CuentaPorPagarDtos.CxpResponse> listar(@RequestParam(required = false) Long proveedorId) {
        List<CuentaPorPagar> lista = proveedorId != null
                ? cuentaPorPagarService.listarPorProveedor(proveedorId)
                : cuentaPorPagarService.listarPendientes();
        return lista.stream().map(CuentaPorPagarDtos.CxpResponse::from).toList();
    }

    @PostMapping("/{id}/abonos")
    @ResponseStatus(HttpStatus.CREATED)
    public void abonar(@PathVariable Long id, @Valid @RequestBody CuentaPorPagarDtos.AbonoRequest req) {
        cuentaPorPagarService.registrarAbono(id, req.clientUuid(), req.monto());
    }
}
