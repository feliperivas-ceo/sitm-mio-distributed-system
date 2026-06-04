package com.sitmmio.analytics.speed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class SpeedConcurrentCalculator {

    private final SpeedCsvParser parser = new SpeedCsvParser();

    public Map<String, SpeedResult> calculate(String datagramsPath,
                                              Set<String> activeRoutes,
                                              int threadCount,
                                              int chunkSize) {

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Map<String, List<SpeedRecord>>>> futures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(datagramsPath))) {
            String line;
            List<String> chunk = new ArrayList<>(chunkSize);

            while ((line = reader.readLine()) != null) {
                chunk.add(line);

                if (chunk.size() >= chunkSize) {
                    List<String> chunkCopy = new ArrayList<>(chunk);
                    futures.add(executor.submit(() -> groupChunk(chunkCopy, activeRoutes)));
                    chunk.clear();
                }
            }

            if (!chunk.isEmpty()) {
                List<String> chunkCopy = new ArrayList<>(chunk);
                futures.add(executor.submit(() -> groupChunk(chunkCopy, activeRoutes)));
            }

            Map<String, List<SpeedRecord>> grouped = new HashMap<>();

            for (Future<Map<String, List<SpeedRecord>>> future : futures) {
                mergeGrouped(grouped, future.get());
            }

            return calculateSpeeds(grouped);

        } catch (Exception e) {
            throw new RuntimeException("Error en cálculo concurrente: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }

    private Map<String, List<SpeedRecord>> groupChunk(List<String> lines, Set<String> activeRoutes) {
        Map<String, List<SpeedRecord>> grouped = new HashMap<>();

        for (String line : lines) {
            Optional<SpeedRecord> optional = parser.parse(line);
            if (optional.isEmpty()) continue;

            SpeedRecord record = optional.get();

            if (!activeRoutes.contains(record.routeId())) continue;

            String key = record.busId() + "|" + record.routeId();

            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }

        return grouped;
    }

    private void mergeGrouped(Map<String, List<SpeedRecord>> finalGrouped,
                              Map<String, List<SpeedRecord>> partialGrouped) {

        for (Map.Entry<String, List<SpeedRecord>> entry : partialGrouped.entrySet()) {
            finalGrouped
                    .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
        }
    }

    private Map<String, SpeedResult> calculateSpeeds(Map<String, List<SpeedRecord>> grouped) {
        Map<String, SpeedResult> results = new TreeMap<>();

        for (List<SpeedRecord> records : grouped.values()) {
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
            throw new RuntimeException("Error escribiendo resultados concurrentes: " + e.getMessage(), e);
        }
    }
}