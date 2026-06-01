package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.*;
import com.sitmmio.dashboard.view.MapView;
import com.sitmmio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MapController {

    @Autowired private BusRepository busRepo;
    @Autowired private RutaRepository rutaRepo;
    @Autowired private EstacionRepository estacionRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    /** Todos los buses con su posición actual */
    @GetMapping("/buses")
    public List<MapView> getAllBuses() {
        return busRepo.findAll().stream()
            .map(MapView::fromBus)
            .collect(Collectors.toList());
    }

    /** Detalle completo de un bus al hacer clic en el mapa (R4, R12) */
    @GetMapping("/buses/{id}")
    public ResponseEntity<MapView> getBusDetail(@PathVariable String id) {
        return busRepo.findById(id)
            .map(bus -> ResponseEntity.ok(MapView.fromBus(bus)))
            .orElse(ResponseEntity.notFound().build());
    }

    /** Buses filtrados por ruta, prioridad, estado o tipo de evento (R4, R9, R11) */
    @GetMapping("/buses/filter")
    public List<MapView> filterBuses(
            @RequestParam(required = false) String rutaId,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoEvento) {

        List<Bus> buses = busRepo.findAll();

        if (rutaId != null && !rutaId.isBlank()) {
            buses = buses.stream()
                .filter(b -> b.getRuta() != null && rutaId.equals(b.getRuta().getId()))
                .collect(Collectors.toList());
        }
        if (prioridad != null && !prioridad.isBlank()) {
            Prioridad p = Prioridad.valueOf(prioridad);
            buses = buses.stream().filter(b -> p.equals(b.getPrioridad())).collect(Collectors.toList());
        }
        if (estado != null && !estado.isBlank()) {
            buses = buses.stream().filter(b -> estado.equals(b.getEstado())).collect(Collectors.toList());
        }
        if (tipoEvento != null && !tipoEvento.isBlank()) {
            TipoEvento te = TipoEvento.valueOf(tipoEvento);
            buses = buses.stream().filter(b -> te.equals(b.getUltimoEvento())).collect(Collectors.toList());
        }

        return buses.stream().map(MapView::fromBus).collect(Collectors.toList());
    }

    /** Buses en alerta (prioridad ALTA o evento ACCIDENTE/CONGESTION) - Filtro crítico (R9, R11) */
    @GetMapping("/buses/alertas")
    public List<MapView> getBusesEnAlerta() {
        return busRepo.findAll().stream()
            .filter(b -> b.getPrioridad() == Prioridad.ALTA
                || b.getUltimoEvento() == TipoEvento.ACCIDENTE
                || b.getUltimoEvento() == TipoEvento.CONGESTION)
            .map(MapView::fromBus)
            .collect(Collectors.toList());
    }

    /** Todas las rutas */
    @GetMapping("/rutas")
    public List<Ruta> getAllRutas() {
        return rutaRepo.findAll();
    }

    /** Todas las estaciones */
    @GetMapping("/estaciones")
    public List<Estacion> getAllEstaciones() {
        return estacionRepo.findAll();
    }

    /** Estaciones filtradas por ruta */
    @GetMapping("/estaciones/filter")
    public List<Estacion> filterEstaciones(@RequestParam(required = false) String rutaId) {
        if (rutaId != null && !rutaId.isBlank()) {
            return estacionRepo.findByRutaId(rutaId);
        }
        return estacionRepo.findAll();
    }

    /** Resumen del mapa */
    @GetMapping("/mapa/resumen")
    public Map<String, Object> resumenMapa() {
        return Map.of(
            "totalBuses", busRepo.count(),
            "busesActivos", busRepo.findByEstado("ACTIVO").size() + busRepo.findByEstado("EN_RUTA").size(),
            "totalRutas", rutaRepo.count(),
            "totalEstaciones", estacionRepo.count()
        );
    }

    /** Notifica actualización de un bus por WebSocket (usado por BusTrackingService) */
    public void actualizarBus(Bus bus) {
        busRepo.save(bus);
        messagingTemplate.convertAndSend("/topic/buses", MapView.fromBus(bus));
    }
}
