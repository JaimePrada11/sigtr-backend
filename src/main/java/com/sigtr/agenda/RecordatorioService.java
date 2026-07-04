package com.sigtr.agenda;

import com.sigtr.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Los recordatorios se generan de dos formas: manual (el tendero crea una
 * tarea) o automatica (otros modulos llaman a generarAutomatico cuando ya
 * tienen una fecha relevante -- vencimiento de CxP, lote, prestamo, etc.).
 */
@Service
@RequiredArgsConstructor
public class RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;

    // Fase 1-4: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public Recordatorio crearManual(String titulo, String descripcion, LocalDate fecha) {
        Recordatorio r = base(Recordatorio.TipoRecordatorio.TAREA, null, null, titulo, descripcion, fecha);
        return recordatorioRepository.save(r);
    }

    /** Llamado desde otros servicios (CompraService, InventarioService, PrestamoService). */
    @Transactional
    public void generarAutomatico(Recordatorio.TipoRecordatorio tipo, String referenciaTipo, Long referenciaId,
                                   String titulo, String descripcion, LocalDate fecha) {
        Recordatorio r = base(tipo, referenciaTipo, referenciaId, titulo, descripcion, fecha);
        recordatorioRepository.save(r);
    }

    @Transactional
    public Recordatorio completar(Long id) {
        Recordatorio r = obtener(id);
        r.setEstado(Recordatorio.EstadoRecordatorio.COMPLETADO);
        return recordatorioRepository.save(r);
    }

    @Transactional
    public Recordatorio descartar(Long id) {
        Recordatorio r = obtener(id);
        r.setEstado(Recordatorio.EstadoRecordatorio.DESCARTADO);
        return recordatorioRepository.save(r);
    }

    public List<Recordatorio> listar(Recordatorio.EstadoRecordatorio estado) {
        Long tenantId = TenantContext.get();
        return estado != null
                ? recordatorioRepository.findByTenantIdAndEstadoOrderByFechaRecordatorioAsc(tenantId, estado)
                : recordatorioRepository.findByTenantIdOrderByFechaRecordatorioAsc(tenantId);
    }

    private Recordatorio obtener(Long id) {
        return recordatorioRepository.findById(id)
                .filter(r -> r.getTenantId().equals(TenantContext.get()))
                .orElseThrow(() -> new EntityNotFoundException("Recordatorio no encontrado: " + id));
    }

    private Recordatorio base(Recordatorio.TipoRecordatorio tipo, String referenciaTipo, Long referenciaId,
                               String titulo, String descripcion, LocalDate fecha) {
        Recordatorio r = new Recordatorio();
        r.setTenantId(TenantContext.get());
        r.setClientUuid(UUID.randomUUID().toString());
        r.setTipo(tipo);
        r.setReferenciaTipo(referenciaTipo);
        r.setReferenciaId(referenciaId);
        r.setTitulo(titulo);
        r.setDescripcion(descripcion);
        r.setFechaRecordatorio(fecha);
        r.setUsuarioId(usuarioActualId());
        return r;
    }
}
