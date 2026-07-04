package com.sigtr.venta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    /** Venta normal: contado o fiado, con carrito completo. */
    @PostMapping("/api/ventas")
    @ResponseStatus(HttpStatus.CREATED)
    public VentaDtos.VentaResponse registrarVenta(@Valid @RequestBody VentaDtos.VentaRequest req) {
        Venta venta = ventaService.registrarVenta(
                req.clientUuid(), req.clienteId(), req.formaPago(), req.items());
        return VentaDtos.VentaResponse.from(venta);
    }

    /**
     * Libreta Digital: "APUNTAR FIADO".
     * Cliente + lista simple de productos. Siempre FIADO. Sin carrito,
     * sin formulario largo -- este es el endpoint que mas van a usar.
     */
    @PostMapping("/api/ventas/libreta")
    @ResponseStatus(HttpStatus.CREATED)
    public VentaDtos.VentaResponse apuntarFiado(@Valid @RequestBody VentaDtos.LibretaFiadoRequest req) {
        Venta venta = ventaService.registrarVenta(
                req.clientUuid(), req.clienteId(), Venta.FormaPago.FIADO, req.items());
        return VentaDtos.VentaResponse.from(venta);
    }

    @PostMapping("/api/abonos")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarAbono(@Valid @RequestBody VentaDtos.AbonoRequest req) {
        ventaService.registrarAbono(req.clientUuid(), req.clienteId(), req.monto());
    }
}
