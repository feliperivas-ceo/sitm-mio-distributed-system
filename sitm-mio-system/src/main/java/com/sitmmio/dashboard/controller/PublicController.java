package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.Bus;
import com.sitmmio.common.model.Estacion;
import com.sitmmio.common.model.Ruta;
import com.sitmmio.repository.BusRepository;
import com.sitmmio.repository.EstacionRepository;
import com.sitmmio.repository.EventoRepository;
import com.sitmmio.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Consulta pública sin autenticación (R14) */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired private RutaRepository rutaRepo;
    @Autowired private BusRepository busRepo;
    @Autowired private EstacionRepository estacionRepo;
    @Autowired private EventoRepository eventoRepo;

    @GetMapping("/rutas")
    public List<Map<String, Object>> getEstadoRutas() {
        return rutaRepo.findAll().stream().map(ruta -> {
            List<Bus> buses = busRepo.findByRutaId(ruta.getId());
            long activos = buses.stream()
                .filter(b -> "ACTIVO".equals(b.getEstado()) || "EN_RUTA".equals(b.getEstado()))
                .count();
            double velProm = buses.stream()
                .filter(b -> b.getVelocidad() != null)
                .mapToDouble(Bus::getVelocidad)
                .average().orElse(0.0);
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("rutaId", ruta.getId());
            m.put("nombre", ruta.getNombre());
            m.put("color", ruta.getColor() != null ? ruta.getColor() : "#000");
            m.put("totalBuses", buses.size());
            m.put("busesActivos", activos);
            m.put("velocidadPromedio", Math.round(velProm * 10.0) / 10.0);
            m.put("estado", activos > 0 ? "OPERANDO" : "SUSPENDIDA");
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/rutas/{rutaId}/llegada/{estacionId}")
    public ResponseEntity<?> estimacionLlegada(
            @PathVariable String rutaId,
            @PathVariable Long estacionId) {
        return rutaRepo.findById(rutaId).map(ruta -> {
            List<Bus> buses = busRepo.findByRutaId(rutaId).stream()
                .filter(b -> b.getLatitud() != null && ("ACTIVO".equals(b.getEstado()) || "EN_RUTA".equals(b.getEstado())))
                .collect(Collectors.toList());

            return estacionRepo.findById(estacionId).map(est -> {
                if (buses.isEmpty()) {
                    return ResponseEntity.ok((Object) Map.of(
                        "rutaId", rutaId,
                        "estacion", est.getNombre(),
                        "mensaje", "No hay buses activos en esta ruta",
                        "estimacionMinutos", -1
                    ));
                }
                // Estimación básica por distancia euclidiana
                Bus busCercano = buses.stream()
                    .min((a, b) -> Double.compare(
                        distancia(a.getLatitud(), a.getLongitud(), est.getLatitud(), est.getLongitud()),
                        distancia(b.getLatitud(), b.getLongitud(), est.getLatitud(), est.getLongitud())
                    )).get();

                double dist = distancia(busCercano.getLatitud(), busCercano.getLongitud(),
                    est.getLatitud(), est.getLongitud());
                double velKmH = busCercano.getVelocidad() != null ? busCercano.getVelocidad() : 25.0;
                double tiempoMin = (dist / velKmH) * 60;

                return ResponseEntity.ok((Object) Map.of(
                    "rutaId", rutaId,
                    "estacion", est.getNombre(),
                    "busId", busCercano.getId(),
                    "velocidadPromedio", Math.round(velKmH * 10) / 10.0,
                    "estimacionMinutos", (int) Math.round(tiempoMin),
                    "distanciaKm", Math.round(dist * 100) / 100.0
                ));
            }).orElse(ResponseEntity.notFound().build());
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumenPublico() {
        long totalBuses = busRepo.count();
        long busesActivos = busRepo.findByEstado("ACTIVO").size() + busRepo.findByEstado("EN_RUTA").size();
        long totalRutas = rutaRepo.count();
        long totalEstaciones = estacionRepo.count();
        return Map.of(
            "totalBuses", totalBuses,
            "busesActivos", busesActivos,
            "totalRutas", totalRutas,
            "totalEstaciones", totalEstaciones,
            "sistemaOperativo", busesActivos > 0
        );
    }

    private double distancia(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
