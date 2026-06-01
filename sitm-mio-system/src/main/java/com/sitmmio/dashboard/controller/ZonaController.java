package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.*;
import com.sitmmio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Vista de zona asignada al controlador (R9) - Mejora 5 */
@RestController
@RequestMapping("/api/zona")
public class ZonaController {

    @Autowired private ZonaRepository zonaRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private BusRepository busRepo;
    @Autowired private EstacionRepository estacionRepo;

    /** Zona y recursos asignados al controlador autenticado */
    @GetMapping("/mi-zona")
    public ResponseEntity<?> getMiZona(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return usuarioRepo.findByUsername(auth.getName()).map(usuario -> {
            if (usuario.getZona() == null) {
                return ResponseEntity.ok((Object) Map.of(
                    "tieneZona", false,
                    "mensaje", "No tienes zona asignada"
                ));
            }
            Zona zona = usuario.getZona();
            List<Bus> buses = busRepo.findAll().stream()
                .filter(b -> b.getRuta() != null)
                .collect(Collectors.toList());
            List<Estacion> estaciones = estacionRepo.findAll();
            List<Usuario> controladores = usuarioRepo.findByZonaId(zona.getId());

            long busesActivos = buses.stream()
                .filter(b -> "ACTIVO".equals(b.getEstado()) || "EN_RUTA".equals(b.getEstado()))
                .count();

            return ResponseEntity.ok((Object) Map.of(
                "tieneZona", true,
                "zona", Map.of(
                    "id", zona.getId(),
                    "nombre", zona.getNombre(),
                    "descripcion", zona.getDescripcion() != null ? zona.getDescripcion() : ""
                ),
                "buses", buses.stream().map(b -> Map.of(
                    "id", b.getId(),
                    "placa", b.getNumeroPlaca() != null ? b.getNumeroPlaca() : "",
                    "rutaId", b.getRuta() != null ? b.getRuta().getId() : "",
                    "estado", b.getEstado() != null ? b.getEstado() : "",
                    "prioridad", b.getPrioridad() != null ? b.getPrioridad().name() : "BAJA",
                    "velocidad", b.getVelocidad() != null ? b.getVelocidad() : 0.0
                )).collect(Collectors.toList()),
                "estaciones", estaciones.stream().map(e -> Map.of(
                    "id", e.getId(),
                    "nombre", e.getNombre(),
                    "tipo", e.getTipo() != null ? e.getTipo() : "",
                    "rutaId", e.getRuta() != null ? e.getRuta().getId() : ""
                )).collect(Collectors.toList()),
                "controladores", controladores.stream().map(c -> Map.of(
                    "id", c.getId(),
                    "nombre", c.getNombre() != null ? c.getNombre() : "",
                    "username", c.getUsername()
                )).collect(Collectors.toList()),
                "estadisticas", Map.of(
                    "totalBuses", buses.size(),
                    "busesActivos", busesActivos,
                    "totalEstaciones", estaciones.size(),
                    "totalControladores", controladores.size()
                )
            ));
        }).orElse(ResponseEntity.status(404).build());
    }

    /** Detalle de zona por ID (para admin) */
    @GetMapping("/{id}")
    public ResponseEntity<?> getZona(@PathVariable Long id) {
        return zonaRepo.findById(id).map(zona -> {
            List<Bus> buses = busRepo.findAll();
            List<Estacion> estaciones = estacionRepo.findAll();
            List<Usuario> controladores = usuarioRepo.findByZonaId(id);
            return ResponseEntity.ok((Object) Map.of(
                "zona", zona,
                "buses", buses,
                "estaciones", estaciones,
                "controladores", controladores
            ));
        }).orElse(ResponseEntity.notFound().build());
    }
}
