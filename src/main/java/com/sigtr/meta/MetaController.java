package com.sigtr.meta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    @GetMapping
    public List<MetaDtos.MetaResponse> listar() {
        return metaService.listar().stream().map(MetaDtos.MetaResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetaDtos.MetaResponse crear(@Valid @RequestBody MetaDtos.CrearMetaRequest req) {
        return MetaDtos.MetaResponse.from(metaService.crear(req));
    }

    @GetMapping("/{id}/progreso")
    public MetaDtos.ProgresoResponse progreso(@PathVariable Long id) {
        return metaService.progreso(id);
    }
}
