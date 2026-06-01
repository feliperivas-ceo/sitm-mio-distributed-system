package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/** Panel de analítica (R7, R11) - Mejora 9 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired private AnalyticsService analyticsService;

    /** KPIs generales: buses activos, total eventos, eventos críticos, rutas */
    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return analyticsService.resumenGeneral();
    }

    /** Velocidad promedio por ruta (km/h) */
    @GetMapping("/velocidad-por-ruta")
    public Map<String, Object> velocidadPorRuta() {
        return analyticsService.velocidadPromedioPorRuta();
    }

    /** Cantidad de eventos recibidos agrupados por mes */
    @GetMapping("/eventos-por-mes")
    public Map<String, Object> eventosPorMes() {
        return analyticsService.eventosPorMes();
    }

    /** Distribución de eventos críticos por prioridad */
    @GetMapping("/eventos-criticos")
    public Map<String, Object> eventosCriticos() {
        return analyticsService.eventosCriticos();
    }

    /** Estado de buses: activos, en ruta, en parada, inactivos */
    @GetMapping("/buses-activos")
    public Map<String, Object> busesActivos() {
        return analyticsService.busesActivos();
    }

    /** Dashboard completo: todos los datos en una sola llamada (R7) */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboardCompleto() {
        return Map.of(
            "resumen", analyticsService.resumenGeneral(),
            "velocidadPorRuta", analyticsService.velocidadPromedioPorRuta(),
            "eventosPorMes", analyticsService.eventosPorMes(),
            "eventosCriticos", analyticsService.eventosCriticos(),
            "busesActivos", analyticsService.busesActivos()
        );
    }
}
