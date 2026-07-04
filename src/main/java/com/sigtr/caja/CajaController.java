package com.sigtr.caja;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    public record AperturaRequest(@NotNull BigDecimal montoApertura) {
    }

    public record CierreRequest(@NotNull BigDecimal montoCierreReal) {
    }

    public record MovimientoRequest(
            @NotBlank String clientUuid,
            @NotNull MovimientoCaja.TipoMovimiento tipo,
            @NotNull BigDecimal monto,
            String descripcion
    ) {
    }

    @PostMapping("/apertura")
    @ResponseStatus(HttpStatus.CREATED)
    public CajaSesion abrir(@RequestBody AperturaRequest req) {
        return cajaService.abrir(req.montoApertura());
    }

    @PostMapping("/cierre")
    public CajaSesion cerrar(@RequestBody CierreRequest req) {
        CajaSesion actual = cajaService.actual();
        return cajaService.cerrar(actual.getId(), req.montoCierreReal());
    }

    @GetMapping("/actual")
    public CajaSesion actual() {
        return cajaService.actual();
    }

    @PostMapping("/movimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoCaja registrarMovimiento(@RequestBody MovimientoRequest req) {
        return cajaService.registrarMovimiento(req.clientUuid(), req.tipo(), req.monto(), req.descripcion());
    }
}
