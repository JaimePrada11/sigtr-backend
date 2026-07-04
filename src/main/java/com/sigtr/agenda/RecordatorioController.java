package com.sigtr.agenda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recordatorios")
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    public record CrearTareaRequest(@NotBlank String titulo, String descripcion, @NotNull LocalDate fecha) {
    }

    public record RecordatorioResponse(Long id, String tipo, String referenciaTipo, Long referenciaId,
                                        String titulo, String descripcion, LocalDate fechaRecordatorio,
                                        String estado) {
        static RecordatorioResponse from(Recordatorio r) {
            return new RecordatorioResponse(r.getId(), r.getTipo().name(), r.getReferenciaTipo(),
                    r.getReferenciaId(), r.getTitulo(), r.getDescripcion(), r.getFechaRecordatorio(),
                    r.getEstado().name());
        }
    }

    @GetMapping
    public List<RecordatorioResponse> listar(@RequestParam(required = false) Recordatorio.EstadoRecordatorio estado) {
        return recordatorioService.listar(estado).stream().map(RecordatorioResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecordatorioResponse crear(@Valid @RequestBody CrearTareaRequest req) {
        return RecordatorioResponse.from(
                recordatorioService.crearManual(req.titulo(), req.descripcion(), req.fecha()));
    }

    @PostMapping("/{id}/completar")
    public RecordatorioResponse completar(@PathVariable Long id) {
        return RecordatorioResponse.from(recordatorioService.completar(id));
    }

    @PostMapping("/{id}/descartar")
    public RecordatorioResponse descartar(@PathVariable Long id) {
        return RecordatorioResponse.from(recordatorioService.descartar(id));
    }
}
