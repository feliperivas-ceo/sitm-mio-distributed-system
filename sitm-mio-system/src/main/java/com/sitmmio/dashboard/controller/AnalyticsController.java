package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired private AnalyticsService analyticsService;

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return analyticsService.resumenGeneral();
    }

    @GetMapping("/velocidad-por-ruta")
    public Map<String, Object> velocidadPorRuta() {
        return analyticsService.velocidadPromedioPorRuta();
    }

    @GetMapping("/eventos-por-mes")
    public Map<String, Object> eventosPorMes() {
        return analyticsService.eventosPorMes();
    }

    @GetMapping("/eventos-criticos")
    public Map<String, Object> eventosCriticos() {
        return analyticsService.eventosCriticos();
    }

    @GetMapping("/buses-activos")
    public Map<String, Object> busesActivos() {
        return analyticsService.busesActivos();
    }
}
