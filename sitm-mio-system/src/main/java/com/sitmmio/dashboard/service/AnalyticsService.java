package com.sitmmio.dashboard.service;

import com.sitmmio.common.model.Prioridad;
import com.sitmmio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired private BusRepository busRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private RutaRepository rutaRepo;

    public Map<String, Object> resumenGeneral() {
        long totalBuses = busRepo.count();
        long busesActivos = busRepo.findByEstado("ACTIVO").size() + busRepo.findByEstado("EN_RUTA").size();
        long totalEventos = eventoRepo.count();
       long eventosCriticos =
        eventoRepo.countByPrioridad(Prioridad.ALTA)
        + eventoRepo.countByPrioridad(Prioridad.CRITICA);;
        long totalRutas = rutaRepo.count();

        return Map.of(
            "totalBuses", totalBuses,
            "busesActivos", busesActivos,
            "totalEventos", totalEventos,
            "eventosCriticos", eventosCriticos,
            "totalRutas", totalRutas
        );
    }

    public Map<String, Object> velocidadPromedioPorRuta() {
        List<Object[]> datos = eventoRepo.avgVelocidadPorRuta();
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Object[] row : datos) {
            if (row[0] != null && row[1] != null) {
                resultado.put(row[0].toString(), Math.round((Double) row[1] * 100.0) / 100.0);
            }
        }
        // Completar con buses directamente si hay pocas rutas en eventos
        busRepo.findAll().forEach(b -> {
            if (b.getRuta() != null && b.getVelocidad() != null) {
                resultado.merge(b.getRuta().getId(), b.getVelocidad(), (old, v) -> (old + v) / 2);
            }
        });
        return Map.of("velocidadPorRuta", resultado);
    }

    public Map<String, Object> eventosPorMes() {
        List<Object[]> datos = eventoRepo.eventosPorMes();
        Map<String, Long> resultado = new LinkedHashMap<>();
        String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        for (Object[] row : datos) {
            if (row[0] != null) {
                int mes = ((Number) row[0]).intValue();
                resultado.put(meses[mes - 1], ((Number) row[1]).longValue());
            }
        }
        return Map.of("eventosPorMes", resultado);
    }

    public Map<String, Object> eventosCriticos() {
    long critica = eventoRepo.countByPrioridad(Prioridad.CRITICA);
    long alta    = eventoRepo.countByPrioridad(Prioridad.ALTA);
    long media   = eventoRepo.countByPrioridad(Prioridad.MEDIA);
    long baja    = eventoRepo.countByPrioridad(Prioridad.BAJA);

    return Map.of(
            "CRITICA", critica,
            "ALTA", alta,
            "MEDIA", media,
            "BAJA", baja
    );
}

    public Map<String, Object> busesActivos() {
        long activos   = busRepo.findByEstado("ACTIVO").size();
        long enRuta    = busRepo.findByEstado("EN_RUTA").size();
        long enParada  = busRepo.findByEstado("EN_PARADA").size();
        long inactivos = busRepo.findByEstado("INACTIVO").size();
        return Map.of(
            "ACTIVO", activos,
            "EN_RUTA", enRuta,
            "EN_PARADA", enParada,
            "INACTIVO", inactivos
        );
    }

    /** Resumen rápido para DashboardController de origin/main */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("busesActivos", busRepo.findByEstado("ACTIVO").size() + busRepo.findByEstado("EN_RUTA").size());
        stats.put("eventosCriticos", eventoRepo.countByPrioridad(Prioridad.ALTA) + eventoRepo.countByPrioridad(Prioridad.CRITICA));
        stats.put("totalBuses", busRepo.count());
        stats.put("totalRutas", rutaRepo.count());
        return stats;
    }
}
