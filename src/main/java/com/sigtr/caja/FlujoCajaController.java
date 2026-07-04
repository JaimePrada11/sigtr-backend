package com.sigtr.caja;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class FlujoCajaController {

    private final FlujoCajaService flujoCajaService;

    @GetMapping("/api/flujo-caja")
    public FlujoCajaDtos.FlujoCajaResponse flujoCaja(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return flujoCajaService.calcular(desde, hasta);
    }
}
