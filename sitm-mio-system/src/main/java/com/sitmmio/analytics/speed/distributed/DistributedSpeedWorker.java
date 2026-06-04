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

        Map<String, List<SpeedRecord>> grouped = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(datagramsPath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Optional<SpeedRecord> optional = parser.parse(line);

                if (optional.isEmpty()) continue;

                SpeedRecord record = optional.get();

                if (!assignedRoutes.contains(record.routeId())) continue;

                String key = record.busId() + "|" + record.routeId();

                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error procesando worker distribuido: " + e.getMessage(), e);
        }

        return calculatePartialResults(grouped);
    }

    private Map<String, PartialSpeedResult> calculatePartialResults(Map<String, List<SpeedRecord>> grouped) {
        Map<String, PartialSpeedResult> results = new TreeMap<>();

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

                String key = current.routeId() + "|" + current.monthKey();

                PartialSpeedResult result = results.computeIfAbsent(
                        key,
                        k -> new PartialSpeedResult(current.routeId(), current.monthKey())
                );

                result.add(speedKmh);
            }
        }

        return results;
    }

    public void writePartialResults(Map<String, PartialSpeedResult> results, String outputPath) {
        try {
            Path path = Path.of(outputPath);
            Files.createDirectories(path.getParent());

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
            System.out.println("Ejemplo:");
            System.out.println("java DistributedSpeedWorker /opt/sitm-mio/datagrams4Pilot.csv 131,140,2241 data/output/partial-worker-1.csv");
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