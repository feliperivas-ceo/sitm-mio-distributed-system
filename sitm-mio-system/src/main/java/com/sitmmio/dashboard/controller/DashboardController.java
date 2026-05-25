package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.model.Usuario;
import com.sitmmio.dashboard.service.AnalyticsService;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class DashboardController {

    private final AnalyticsService analyticsService;

    public DashboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public void mostrarDashboard(Usuario usuario) {

        System.out.println("=== DASHBOARD CCO ===");
        System.out.println("Usuario: " + usuario.getUsername());
        System.out.println("Rol: " + usuario.getRol());

        Map<String, Object> stats = analyticsService.obtenerEstadisticas();

        System.out.println("Buses activos: " + stats.get("busesActivos"));
        System.out.println("Eventos críticos: " + stats.get("eventosCriticos"));
    }
}
