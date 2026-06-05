package com.sitmmio.dashboard.service;

import com.sitmmio.dashboard.dto.HistoricalSpeedRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class HistoricalSpeedAnalyticsService {

    @Value("${analytics.historical.monolithic-path}")
    private String monolithicPath;

    @Value("${analytics.historical.concurrent-path}")
    private String concurrentPath;

    @Value("${analytics.historical.distributed-path}")
    private String distributedPath;

    public List<HistoricalSpeedRow> obtenerVelocidades(
            String version,
            String routeId,
            String month
    ) {
        String filePath = resolverRuta(version);

        if (!Files.exists(Path.of(filePath))) {
            throw new IllegalArgumentException(
                    "No existe el archivo histórico para la versión: " + version
            );
        }

        List<HistoricalSpeedRow> resultados = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean primeraLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }

                if (line.isBlank()) continue;

                String[] columns = line.split(",");

                if (columns.length < 4) continue;

                HistoricalSpeedRow row = new HistoricalSpeedRow(
                        columns[0].trim(),
                        columns[1].trim(),
                        Double.parseDouble(columns[2].trim()),
                        Long.parseLong(columns[3].trim())
                );

                boolean coincideRuta =
                        routeId == null ||
                        routeId.isBlank() ||
                        row.routeId().equals(routeId);

                boolean coincideMes =
                        month == null ||
                        month.isBlank() ||
                        row.month().equals(month);

                if (coincideRuta && coincideMes) {
                    resultados.add(row);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error leyendo archivo histórico: " + e.getMessage(),
                    e
            );
        }

        return resultados;
    }

    public String resolverRuta(String version) {
        if (version == null) {
            return concurrentPath;
        }

        return switch (version.toLowerCase(Locale.ROOT)) {
            case "monolitico" -> monolithicPath;
            case "concurrente" -> concurrentPath;
            case "distribuido" -> distributedPath;
            default -> throw new IllegalArgumentException(
                    "Versión no válida: " + version
            );
        };
    }
}