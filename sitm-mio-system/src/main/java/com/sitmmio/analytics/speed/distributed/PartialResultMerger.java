package com.sitmmio.analytics.speed.distributed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class PartialResultMerger {

    public Map<String, PartialSpeedResult> merge(String... partialFiles) {
        Map<String, PartialSpeedResult> finalResults = new TreeMap<>();

        for (String file : partialFiles) {
            readPartialFile(file, finalResults);
        }

        return finalResults;
    }

    private void readPartialFile(String filePath, Map<String, PartialSpeedResult> finalResults) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] c = line.split(",");

                if (c.length < 4) continue;

                String routeId = c[0].trim();
                String month = c[1].trim();
                double speedSum = Double.parseDouble(c[2].trim());
                long count = Long.parseLong(c[3].trim());

                String key = routeId + "|" + month;

                PartialSpeedResult incoming = new PartialSpeedResult(routeId, month);

                for (long i = 0; i < count; i++) {
                    incoming.add(speedSum / count);
                }

                finalResults.merge(
                        key,
                        incoming,
                        (current, value) -> {
                            current.merge(value);
                            return current;
                        }
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo parcial: " + filePath + " - " + e.getMessage(), e);
        }
    }

    public void writeFinalResults(Map<String, PartialSpeedResult> results, String outputPath) {
        try {
            Path path = Path.of(outputPath);
            Files.createDirectories(path.getParent());

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println("routeId,month,averageSpeedKmh,count");

                for (PartialSpeedResult result : results.values()) {
                    writer.println(result.toFinalCsvLine());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error escribiendo resultado final distribuido: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso:");
            System.out.println("java PartialResultMerger <outputFinalPath> <partial1> <partial2> ...");
            return;
        }

        String outputPath = args[0];

        String[] partials = new String[args.length - 1];
        System.arraycopy(args, 1, partials, 0, partials.length);

        PartialResultMerger merger = new PartialResultMerger();

        long start = System.currentTimeMillis();

        Map<String, PartialSpeedResult> results = merger.merge(partials);
        merger.writeFinalResults(results, outputPath);

        long end = System.currentTimeMillis();

        System.out.println("Merge distribuido terminado.");
        System.out.println("Archivos parciales unidos: " + partials.length);
        System.out.println("Resultados finales: " + results.size());
        System.out.println("Tiempo merge: " + (end - start) + " ms");
        System.out.println("Archivo generado: " + outputPath);
    }
}