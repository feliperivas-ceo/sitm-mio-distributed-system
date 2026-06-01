package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.Bus;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;
import com.sitmmio.repository.BusRepository;
import com.sitmmio.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Alertas críticas visuales (Mejora 12)
 * Rojo = ACCIDENTE, Naranja = CONGESTION, Amarillo = MEDIA, Verde = GPS_NORMAL
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertasController {

    @Autowired private BusRepository busRepo;
    @Autowired private EventoRepository eventoRepo;

    /** Buses en estado crítico con su nivel de alerta y color */
    @GetMapping
    public List<Map<String, Object>> getAlertas() {
        return busRepo.findAll().stream()
            .filter(b -> b.getPrioridad() != null || b.getUltimoEvento() != null)
            .map(b -> {
                AlertaNivel nivel = calcularNivel(b);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("busId", b.getId());
                m.put("rutaId", b.getRuta() != null ? b.getRuta().getId() : null);
                m.put("estado", b.getEstado());
                m.put("ultimoEvento", b.getUltimoEvento() != null ? b.getUltimoEvento().name() : null);
                m.put("prioridad", b.getPrioridad() != null ? b.getPrioridad().name() : null);
                m.put("nivelAlerta", nivel.nombre);
                m.put("colorAlerta", nivel.color);
                m.put("iconoAlerta", nivel.icono);
                m.put("latitud", b.getLatitud());
                m.put("longitud", b.getLongitud());
                return m;
            })
            .sorted(Comparator.comparing((Map<String,Object> m) -> m.get("nivelAlerta").toString()))
            .collect(Collectors.toList());
    }

    /** Solo alertas de nivel crítico (ACCIDENTE o prioridad ALTA) */
    @GetMapping("/criticas")
    public List<Map<String, Object>> getAlertasCriticas() {
        return getAlertas().stream()
            .filter(a -> "CRITICO".equals(a.get("nivelAlerta")) || "ALTO".equals(a.get("nivelAlerta")))
            .collect(Collectors.toList());
    }

    /** Resumen de alertas por nivel */
    @GetMapping("/resumen")
    public Map<String, Object> resumenAlertas() {
        List<Map<String, Object>> todas = getAlertas();
        Map<String, Long> porNivel = todas.stream()
            .collect(Collectors.groupingBy(
                a -> a.get("nivelAlerta").toString(),
                Collectors.counting()
            ));
        return Map.of(
            "total", todas.size(),
            "critico", porNivel.getOrDefault("CRITICO", 0L),
            "alto",    porNivel.getOrDefault("ALTO", 0L),
            "medio",   porNivel.getOrDefault("MEDIO", 0L),
            "normal",  porNivel.getOrDefault("NORMAL", 0L)
        );
    }

    private AlertaNivel calcularNivel(Bus bus) {
        TipoEvento evento = bus.getUltimoEvento();
        Prioridad prioridad = bus.getPrioridad();

        if (TipoEvento.ACCIDENTE.equals(evento)) {
            return AlertaNivel.CRITICO;
        }
        if (Prioridad.ALTA.equals(prioridad)) {
            return AlertaNivel.ALTO;
        }
        if (TipoEvento.CONGESTION.equals(evento) || Prioridad.MEDIA.equals(prioridad)) {
            return AlertaNivel.MEDIO;
        }
        return AlertaNivel.NORMAL;
    }

    /** Niveles de alerta con código de color */
    enum AlertaNivel {
        CRITICO("CRITICO", "#C0392B", "🚨"),  // Rojo oscuro - ACCIDENTE
        ALTO(   "ALTO",    "#E74C3C", "🔴"),  // Rojo - Prioridad ALTA
        MEDIO(  "MEDIO",   "#E67E22", "🟠"),  // Naranja - CONGESTION / MEDIA
        NORMAL( "NORMAL",  "#27AE60", "🟢");  // Verde - GPS_NORMAL / BAJA

        final String nombre;
        final String color;
        final String icono;

        AlertaNivel(String nombre, String color, String icono) {
            this.nombre = nombre; this.color = color; this.icono = icono;
        }
    }
}
