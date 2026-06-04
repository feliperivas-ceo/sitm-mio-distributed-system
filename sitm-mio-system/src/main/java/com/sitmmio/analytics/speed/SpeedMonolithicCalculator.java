package com.sitmmio.analytics.speed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class SpeedMonolithicCalculator {

    private final SpeedCsvParser parser = new SpeedCsvParser();

    public Map<String, SpeedResult> calculate(String datagramsPath, Set<String> activeRoutes) {
        Map<String, List<SpeedRecord>> recordsByBusRoute = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(datagramsPath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Optional<SpeedRecord> optional = parser.parse(line);
                if (optional.isEmpty()) continue;

                SpeedRecord record = optional.get();

                if (!activeRoutes.contains(record.routeId())) continue;

                String key = record.busId() + "|" + record.routeId();

                recordsByBusRoute
                        .computeIfAbsent(key, k -> new ArrayList<>())
                        .add(record);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo datagramas: " + e.getMessage(), e);
        }

        Map<String, SpeedResult> results = new TreeMap<>();

        for (List<SpeedRecord> records : recordsByBusRoute.values()) {
            records.sort(Comparator.comparing(SpeedRecord::timestamp));

            for (int i = 1; i < records.size(); i++) {
                SpeedRecord previous = records.get(i - 1);
                SpeedRecord current = records.get(i);

                long seconds = Duration.between(previous.timestamp(), current.timestamp()).getSeconds();
                double meters = current.odometer() - previous.odometer();

                if (seconds <= 0 || meters <= 0) continue;

                double speedKmh = (meters / seconds) * 3.6;

                if (speedKmh <= 0 || speedKmh > 120) continue;

                String resultKey = current.routeId() + "|" + current.monthKey();

                SpeedResult result = results.computeIfAbsent(
                        resultKey,
                        k -> new SpeedResult(current.routeId(), current.monthKey())
                );

                result.add(speedKmh);
            }
        }

        return results;
    }

    public void writeResults(Map<String, SpeedResult> results, String outputPath) {
        try {
            Path path = Path.of(outputPath);
            Files.createDirectories(path.getParent());

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println("routeId,month,averageSpeedKmh,count");

                for (SpeedResult result : results.values()) {
                    writer.println(result.toCsvLine());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error escribiendo resultados: " + e.getMessage(), e);
        }
    }
}