package com.sitmmio.analytics.speed;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class SpeedBenchmarkRunner {

    public static void main(String[] args) {

        String routesPath = Path.of(
                "src", "main", "resources", "data", "lines-241-ActiveGT.csv"
        ).toString();

        String datagramsPath = Path.of(
                "src", "main", "resources", "data", "datagrams-MiniPilot.csv"
        ).toString();

        String monolithicOutput = Path.of(
                "data", "output", "velocidad_promedio_monolitico.csv"
        ).toString();

        String concurrentOutput = Path.of(
                "data", "output", "velocidad_promedio_concurrente.csv"
        ).toString();

        ActiveRoutesLoader routesLoader = new ActiveRoutesLoader();
        Set<String> activeRoutes = routesLoader.loadActiveRoutes(routesPath);

        System.out.println("Rutas activas cargadas: " + activeRoutes.size());

        SpeedMonolithicCalculator monolithicCalculator = new SpeedMonolithicCalculator();

        long monoStart = System.currentTimeMillis();
        Map<String, SpeedResult> monoResults =
                monolithicCalculator.calculate(datagramsPath, activeRoutes);
        long monoEnd = System.currentTimeMillis();

        monolithicCalculator.writeResults(monoResults, monolithicOutput);

        long monoTime = monoEnd - monoStart;

        System.out.println("\n=== MONOLÍTICO ===");
        System.out.println("Resultados generados: " + monoResults.size());
        System.out.println("Tiempo: " + monoTime + " ms");
        System.out.println("Archivo generado: " + monolithicOutput);

        int threads = Runtime.getRuntime().availableProcessors();
        int chunkSize = 100_000;

        SpeedConcurrentCalculator concurrentCalculator = new SpeedConcurrentCalculator();

        long concurrentStart = System.currentTimeMillis();
        Map<String, SpeedResult> concurrentResults =
                concurrentCalculator.calculate(datagramsPath, activeRoutes, threads, chunkSize);
        long concurrentEnd = System.currentTimeMillis();

        concurrentCalculator.writeResults(concurrentResults, concurrentOutput);

        long concurrentTime = concurrentEnd - concurrentStart;

        System.out.println("\n=== CONCURRENTE ===");
        System.out.println("Resultados generados: " + concurrentResults.size());
        System.out.println("Hilos usados: " + threads);
        System.out.println("Chunk size: " + chunkSize);
        System.out.println("Tiempo: " + concurrentTime + " ms");
        System.out.println("Archivo generado: " + concurrentOutput);

        if (concurrentTime > 0) {
            double speedup = (double) monoTime / concurrentTime;
            System.out.printf("\nSpeedup concurrente: %.2fx%n", speedup);
        }
    }
}