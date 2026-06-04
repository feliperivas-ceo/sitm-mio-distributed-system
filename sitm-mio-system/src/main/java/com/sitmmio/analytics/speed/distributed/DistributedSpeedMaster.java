package com.sitmmio.analytics.speed.distributed;

import com.sitmmio.analytics.speed.ActiveRoutesLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DistributedSpeedMaster {

    public static void main(String[] args) {

        String routesPath = Path.of(
                "src", "main", "resources", "data", "lines-241-ActiveGT.csv"
        ).toString();

        String datagramsPath = "/opt/sitm-mio/datagrams4Pilot.csv";

        int workers = 3;

        if (args.length >= 1) {
            workers = Integer.parseInt(args[0]);
        }

        ActiveRoutesLoader loader = new ActiveRoutesLoader();
        Set<String> activeRoutes = loader.loadActiveRoutes(routesPath);

        List<String> routes = new ArrayList<>(activeRoutes);

        System.out.println("Rutas activas: " + routes.size());
        System.out.println("Workers: " + workers);
        System.out.println();

        for (int i = 0; i < workers; i++) {
            List<String> assigned = new ArrayList<>();

            for (int j = i; j < routes.size(); j += workers) {
                assigned.add(routes.get(j));
            }

            String routesArg = String.join(",", assigned);
            String output = "data/output/partial-worker-" + (i + 1) + ".csv";

            System.out.println("=== Worker " + (i + 1) + " ===");
            System.out.println("Rutas asignadas: " + assigned.size());
            System.out.println("Comando:");
            System.out.println("java -cp build/libs/sitm-mio-system-0.0.1-SNAPSHOT.jar " +
                    "com.sitmmio.analytics.speed.distributed.DistributedSpeedWorker " +
                    datagramsPath + " " + routesArg + " " + output);
            System.out.println();
        }

        System.out.println("Después de traer los partial-worker-X.csv al PC master, ejecutar:");
        System.out.print("java -cp build/libs/sitm-mio-system-0.0.1-SNAPSHOT.jar ");
        System.out.print("com.sitmmio.analytics.speed.distributed.PartialResultMerger ");
        System.out.print("data/output/velocidad_promedio_distribuida.csv ");

        for (int i = 0; i < workers; i++) {
            System.out.print("data/output/partial-worker-" + (i + 1) + ".csv ");
        }

        System.out.println();
    }
}