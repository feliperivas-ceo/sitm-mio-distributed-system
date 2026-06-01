package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.Evento;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;
import com.sitmmio.common.util.DateUtils;
import com.sitmmio.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/** Historial de eventos (R6, R11) - Mejora 11 */
@RestController
@RequestMapping("/api/eventos")
public class EventosController {

    @Autowired private EventoRepository eventoRepo;

    /**
     * Historial de eventos con filtros por ruta, prioridad y tipo.
     * Retorna lista ordenada por timestamp descendente (más reciente primero).
     */
    @GetMapping
    public List<Map<String, Object>> getEventos(
            @RequestParam(required = false) String rutaId,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) String busId) {

        List<Evento> eventos = eventoRepo.findAll()
            .stream()
            .sorted((a, b) -> {
                if (a.getTimestamp() == null) return 1;
                if (b.getTimestamp() == null) return -1;
                return b.getTimestamp().compareTo(a.getTimestamp());
            })
            .collect(Collectors.toList());

        if (rutaId != null && !rutaId.isBlank()) {
            eventos = eventos.stream()
                .filter(e -> e.getRuta() != null && rutaId.equals(e.getRuta().getId()))
                .collect(Collectors.toList());
        }
        if (prioridad != null && !prioridad.isBlank()) {
            Prioridad p = Prioridad.valueOf(prioridad);
            eventos = eventos.stream().filter(e -> p.equals(e.getPrioridad())).collect(Collectors.toList());
        }
        if (tipoEvento != null && !tipoEvento.isBlank()) {
            TipoEvento te = TipoEvento.valueOf(tipoEvento);
            eventos = eventos.stream().filter(e -> te.equals(e.getTipoEvento())).collect(Collectors.toList());
        }
        if (busId != null && !busId.isBlank()) {
            eventos = eventos.stream()
                .filter(e -> e.getBus() != null && busId.equals(e.getBus().getId()))
                .collect(Collectors.toList());
        }

        // Retorna DTO plano para evitar problemas de serialización circular
        return eventos.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Eventos críticos (prioridad ALTA) */
    @GetMapping("/criticos")
    public List<Map<String, Object>> getEventosCriticos() {
        return eventoRepo.findByPrioridad(Prioridad.ALTA)
            .stream()
            .sorted((a, b) -> {
                if (a.getTimestamp() == null) return 1;
                if (b.getTimestamp() == null) return -1;
                return b.getTimestamp().compareTo(a.getTimestamp());
            })
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /** Resumen de estadísticas de eventos */
    @GetMapping("/estadisticas")
    public Map<String, Object> estadisticas() {
        long total = eventoRepo.count();
        long alta  = eventoRepo.countByPrioridad(Prioridad.ALTA);
        long media = eventoRepo.countByPrioridad(Prioridad.MEDIA);
        long baja  = eventoRepo.countByPrioridad(Prioridad.BAJA);
        long procesados = eventoRepo.findAll().stream().filter(e -> Boolean.TRUE.equals(e.getEstadoProcesado())).count();
        return Map.of(
            "total", total,
            "alta", alta,
            "media", media,
            "baja", baja,
            "procesados", procesados,
            "pendientes", total - procesados
        );
    }

    private Map<String, Object> toDto(Evento e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("busId",   e.getBus()  != null ? e.getBus().getId()  : null);
        m.put("busPlaca",e.getBus()  != null ? e.getBus().getNumeroPlaca() : null);
        m.put("rutaId",  e.getRuta() != null ? e.getRuta().getId() : null);
        m.put("tipoEvento", e.getTipoEvento() != null ? e.getTipoEvento().name() : null);
        m.put("prioridad",  e.getPrioridad()  != null ? e.getPrioridad().name()  : null);
        m.put("latitud",    e.getLatitud());
        m.put("longitud",   e.getLongitud());
        m.put("timestamp",  DateUtils.formatDateTime(e.getTimestamp()));
        m.put("estadoProcesado", Boolean.TRUE.equals(e.getEstadoProcesado()));
        m.put("descripcion", e.getDescripcion());
        return m;
    }
}
