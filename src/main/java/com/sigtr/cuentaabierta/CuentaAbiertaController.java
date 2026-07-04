package com.sigtr.cuentaabierta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas-abiertas")
@RequiredArgsConstructor
public class CuentaAbiertaController {

    private final CuentaAbiertaService cuentaAbiertaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaAbiertaDtos.CuentaResponse abrir(@Valid @RequestBody CuentaAbiertaDtos.AbrirRequest req) {
        return CuentaAbiertaDtos.CuentaResponse.from(cuentaAbiertaService.abrir(req.clientUuid(), req.nombre()));
    }

    @PostMapping("/{id}/participantes")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipanteCuenta agregarParticipante(@PathVariable Long id,
                                                    @Valid @RequestBody CuentaAbiertaDtos.ParticipanteRequest req) {
        return cuentaAbiertaService.agregarParticipante(id, req.nombre(), req.clienteId());
    }

    @PostMapping("/{id}/consumos")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsumoCuenta registrarConsumo(@PathVariable Long id,
                                            @Valid @RequestBody CuentaAbiertaDtos.ConsumoRequest req) {
        return cuentaAbiertaService.registrarConsumo(id, req.participanteId(), req.productoId(), req.cantidad());
    }

    @GetMapping("/{id}/division")
    public CuentaAbiertaDtos.DivisionResponse division(@PathVariable Long id) {
        return cuentaAbiertaService.calcularDivision(id);
    }

    @PostMapping("/{id}/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoCuenta registrarPago(@PathVariable Long id, @Valid @RequestBody CuentaAbiertaDtos.PagoRequest req) {
        return cuentaAbiertaService.registrarPago(id, req.clientUuid(), req.participanteId(), req.monto(), req.formaPago());
    }

    @PostMapping("/{id}/cerrar")
    public CuentaAbiertaDtos.CuentaResponse cerrar(@PathVariable Long id) {
        return CuentaAbiertaDtos.CuentaResponse.from(cuentaAbiertaService.cerrar(id));
    }
}
