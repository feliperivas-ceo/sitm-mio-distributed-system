package com.sitmmio.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AnalyticsService {

    private final Random random = new Random();

    public Map<String, Object> obtenerEstadisticas() {

        Map<String, Object> stats = new HashMap<>();

        stats.put("busesActivos", 850 + random.nextInt(50));
        stats.put("eventosCriticos", random.nextInt(10));
        stats.put("pasajerosEstimados", 450000 + random.nextInt(50000));

        return stats;
    }
}
