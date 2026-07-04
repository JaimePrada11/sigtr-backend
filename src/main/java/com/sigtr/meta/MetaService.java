package com.sigtr.meta;

import com.sigtr.common.TenantContext;
import com.sigtr.inventario.MovimientoInventario;
import com.sigtr.inventario.MovimientoInventarioRepository;
import com.sigtr.venta.AbonoCarteraRepository;
import com.sigtr.venta.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * El progreso de una meta nunca se captura a mano -- se calcula on-demand
 * contra los datos reales del periodo (ventas, abonos, mermas), para que
 * nunca quede desincronizado de la realidad.
 */
@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final VentaRepository ventaRepository;
    private final AbonoCarteraRepository abonoCarteraRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    // Fase 1-4: simplificado, ver nota equivalente en VentaService.
    private Long usuarioActualId() {
        return 1L;
    }

    @Transactional
    public Meta crear(MetaDtos.CrearMetaRequest req) {
        Meta meta = new Meta();
        meta.setTenantId(TenantContext.get());
        meta.setClientUuid(req.clientUuid());
        meta.setTipo(req.tipo());
        meta.setDescripcion(req.descripcion());
        meta.setValorObjetivo(req.valorObjetivo());
        meta.setPeriodoInicio(req.periodoInicio());
        meta.setPeriodoFin(req.periodoFin());
        meta.setEstado(Meta.EstadoMeta.ACTIVA);
        meta.setUsuarioId(usuarioActualId());
        return metaRepository.save(meta);
    }

    public List<Meta> listar() {
        return metaRepository.findByTenantId(TenantContext.get());
    }

    public MetaDtos.ProgresoResponse progreso(Long metaId) {
        Long tenantId = TenantContext.get();
        Meta meta = metaRepository.findByIdAndTenantId(metaId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Meta no encontrada: " + metaId));

        Instant desde = meta.getPeriodoInicio().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant hasta = meta.getPeriodoFin().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        BigDecimal valorActual = switch (meta.getTipo()) {
            case VENTAS -> ventaRepository.findByTenantIdAndCreatedAtBetween(tenantId, desde, hasta).stream()
                    .map(v -> v.getTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            case COBRO_CARTERA -> abonoCarteraRepository
                    .findByTenantIdAndCreatedAtBetween(tenantId, desde, hasta).stream()
                    .map(a -> a.getMonto())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            case REDUCCION_MERMAS -> movimientoInventarioRepository
                    .findByTenantIdAndTipoAndCreatedAtBetween(
                            tenantId, MovimientoInventario.TipoMovimientoInventario.MERMA, desde, hasta)
                    .stream()
                    .map(m -> m.getCostoUnitario() != null
                            ? m.getCantidad().multiply(m.getCostoUnitario())
                            : m.getCantidad())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        };

        // Para VENTAS/COBRO_CARTERA mas alto = mejor. Para REDUCCION_MERMAS es al
        // reves (el objetivo es un tope maximo aceptable de merma) -- el porcentaje
        // se interpreta como "que tanto del presupuesto de merma se ha consumido".
        BigDecimal porcentaje = meta.getValorObjetivo().compareTo(BigDecimal.ZERO) > 0
                ? valorActual.divide(meta.getValorObjetivo(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean vencida = LocalDate.now().isAfter(meta.getPeriodoFin());

        return new MetaDtos.ProgresoResponse(meta.getId(), meta.getTipo().name(),
                meta.getValorObjetivo(), valorActual, porcentaje, vencida);
    }
}
