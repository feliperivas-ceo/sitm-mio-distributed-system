package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.Evento;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;
import com.sitmmio.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eventos")
public class EventosController {

    @Autowired private EventoRepository eventoRepo;

    /** Historial completo de eventos (R6, R11) */
    @GetMapping
    public List<Evento> getEventos(
            @RequestParam(required = false) String rutaId,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String tipoEvento) {

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
        return eventos;
    }

    /** Eventos críticos (prioridad ALTA) */
    @GetMapping("/criticos")
    public List<Evento> getEventosCriticos() {
        return eventoRepo.findByPrioridad(Prioridad.ALTA)
            .stream()
            .sorted((a, b) -> {
                if (a.getTimestamp() == null) return 1;
                if (b.getTimestamp() == null) return -1;
                return b.getTimestamp().compareTo(a.getTimestamp());
            })
            .collect(Collectors.toList());
    }
}
