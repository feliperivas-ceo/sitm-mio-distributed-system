package com.sitmmio.dashboard.controller;

import com.sitmmio.dashboard.dto.HistoricalSpeedRow;
import com.sitmmio.dashboard.service.HistoricalSpeedAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/historico")
public class HistoricalSpeedAnalyticsController {

    private final HistoricalSpeedAnalyticsService service;

    public HistoricalSpeedAnalyticsController(
            HistoricalSpeedAnalyticsService service
    ) {
        this.service = service;
    }

    @GetMapping("/velocidades")
    public Map<String, Object> velocidades(
            @RequestParam(defaultValue = "concurrente") String version,
            @RequestParam(required = false) String routeId,
            @RequestParam(required = false) String month
    ) {
        List<HistoricalSpeedRow> resultados =
                service.obtenerVelocidades(version, routeId, month);

        long totalRegistros = resultados.stream()
                .mapToLong(HistoricalSpeedRow::count)
                .sum();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("version", version);
        response.put("cantidadResultados", resultados.size());
        response.put("totalRegistros", totalRegistros);
        response.put("resultados", resultados);

        return response;
    }

    @GetMapping("/benchmark")
    public Map<String, Object> benchmark() {
        return Map.of(
                "monolitico", Map.of(
                        "tiempoMs", 13342,
                        "resultados", 97
                ),
                "concurrente", Map.of(
                        "tiempoMs", 8068,
                        "resultados", 97,
                        "hilos", 12,
                        "chunkSize", 100000,
                        "speedup", 1.65
                ),
                "distribuido", Map.of(
                        "worker1TiempoMs", 212652,
                        "worker2TiempoMs", 253800,
                        "mergeTiempoMs", 95,
                        "resultadosFinales", 708,
                        "datasetGb", 67
                )
        );
    }
}