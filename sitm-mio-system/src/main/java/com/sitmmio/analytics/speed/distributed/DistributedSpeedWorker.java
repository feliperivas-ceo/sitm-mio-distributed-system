package com.sitmmio.analytics.speed.distributed;

import com.sitmmio.analytics.speed.SpeedCsvParser;
import com.sitmmio.analytics.speed.SpeedRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public class DistributedSpeedWorker {

    private final SpeedCsvParser parser = new SpeedCsvParser();

    public Map<String, PartialSpeedResult> process(String datagramsPath,
                                                   Set<String> assignedRoutes) {

        Map<String, SpeedRecord> lastRecordByBusRoute = new HashMap<>();
        Map<String, PartialSpeedResult> results = new TreeMap<>();

        long readLines = 0;
        long validRecords = 0;
        long calculatedSpeeds = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(datagramsPath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                readLines++;

                Optional<SpeedRecord> optional = parser.parse(line);
                if (optional.isEmpty()) continue;

                SpeedRecord current = optional.get();

                if (!assignedRoutes.contains(current.routeId())) continue;

                validRecords++;

                String busRouteKey = current.busId() + "|" + current.routeId();

                SpeedRecord previous = lastRecordByBusRoute.get(busRouteKey);

                if (previous != null) {
                    long seconds = Duration.between(previous.timestamp(), current.timestamp()).getSeconds();
                    double meters = current.odometer() - previous.odometer();

                    if (seconds > 0 && meters > 0) {
                        double speedKmh = (meters / seconds) * 3.6;

                        if (speedKmh > 0 && speedKmh <= 120) {
                            String resultKey = current.routeId() + "|" + current.monthKey();

                            PartialSpeedResult result = results.computeIfAbsent(
                                    resultKey,
                                    k -> new PartialSpeedResult(current.routeId(), current.monthKey())
                            );

                            result.add(speedKmh);
                            calculatedSpeeds++;
                        }
                    }
                }

                lastRecordByBusRoute.put(busRouteKey, current);

                if (readLines % 1_000_000 == 0) {
                    System.out.println("Lineas leidas: " + readLines +
                            " | validas: " + validRecords +
                            " | velocidades: " + calculatedSpeeds +
                            " | memoriaKeys: " + lastRecordByBusRoute.size());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error procesando worker distribuido: " + e.getMessage(), e);
        }

        System.out.println("Worker streaming finalizado.");
        System.out.println("Lineas leidas: " + readLines);
        System.out.println("Registros validos: " + validRecords);
        System.out.println("Velocidades calculadas: " + calculatedSpeeds);
        System.out.println("Resultados parciales: " + results.size());

        return results;
    }

    public void writePartialResults(Map<String, PartialSpeedResult> results, String outputPath) {
        try {
            Path path = Path.of(outputPath);

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println("routeId,month,speedSum,count");

                for (PartialSpeedResult result : results.values()) {
                    writer.println(result.toPartialCsvLine());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error escribiendo parcial distribuido: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso:");
            System.out.println("java DistributedSpeedWorker <datagramsPath> <routesCommaSeparated> <outputPath>");
            return;
        }

        String datagramsPath = args[0];
        Set<String> assignedRoutes = new HashSet<>(Arrays.asList(args[1].split(",")));
        String outputPath = args[2];

        DistributedSpeedWorker worker = new DistributedSpeedWorker();

        long start = System.currentTimeMillis();

        Map<String, PartialSpeedResult> results = worker.process(datagramsPath, assignedRoutes);
        worker.writePartialResults(results, outputPath);

        long end = System.currentTimeMillis();

        System.out.println("Worker terminado.");
        System.out.println("Rutas asignadas: " + assignedRoutes.size());
        System.out.println("Resultados parciales: " + results.size());
        System.out.println("Tiempo: " + (end - start) + " ms");
        System.out.println("Archivo generado: " + outputPath);
    }
}