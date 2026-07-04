package com.sigtr.inventario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    // Fase 1/2: simplificado, ver nota equivalente en VentaService/CajaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    public InventarioDtos.LoteResponse entrada(@Valid @RequestBody InventarioDtos.EntradaRequest req) {
        Lote lote = inventarioService.registrarEntrada(
                req.clientUuid(), req.productoId(), req.cantidad(), req.costoUnitario(),
                req.numeroLote(), req.fechaVencimiento(), usuarioActualId());
        return InventarioDtos.LoteResponse.from(lote);
    }

    @PostMapping("/mermas")
    @ResponseStatus(HttpStatus.CREATED)
    public void merma(@Valid @RequestBody InventarioDtos.AjusteRequest req) {
        // Reutiliza AjusteRequest por la forma (productoId, delta, motivo obligatorio),
        // pero la merma siempre resta y queda tipada como MERMA, no AJUSTE.
        inventarioService.registrarSalida(req.productoId(), req.delta().abs(),
                MovimientoInventario.TipoMovimientoInventario.MERMA, req.motivo(),
                usuarioActualId(), "MERMA_MANUAL", null);
    }

    @PostMapping("/consumo-propio")
    @ResponseStatus(HttpStatus.CREATED)
    public void consumoPropio(@Valid @RequestBody InventarioDtos.SalidaRequest req) {
        inventarioService.registrarSalida(req.productoId(), req.cantidad(),
                MovimientoInventario.TipoMovimientoInventario.CONSUMO_PROPIO, req.motivo(),
                usuarioActualId(), "CONSUMO_PROPIO", null);
    }

    @PostMapping("/vencimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public void vencimiento(@Valid @RequestBody InventarioDtos.SalidaRequest req) {
        inventarioService.registrarSalida(req.productoId(), req.cantidad(),
                MovimientoInventario.TipoMovimientoInventario.VENCIMIENTO, req.motivo(),
                usuarioActualId(), "VENCIMIENTO_MANUAL", null);
    }

    @PostMapping("/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    public void ajuste(@Valid @RequestBody InventarioDtos.AjusteRequest req) {
        inventarioService.registrarAjuste(req.productoId(), req.delta(), req.motivo(), usuarioActualId());
    }

    @GetMapping("/movimientos")
    public List<InventarioDtos.MovimientoResponse> movimientos(@RequestParam Long productoId) {
        return inventarioService.listarMovimientos(productoId).stream()
                .map(InventarioDtos.MovimientoResponse::from)
                .toList();
    }

    @GetMapping("/lotes")
    public List<InventarioDtos.LoteResponse> lotes(@RequestParam Long productoId) {
        return inventarioService.listarLotes(productoId).stream()
                .map(InventarioDtos.LoteResponse::from)
                .toList();
    }
}
