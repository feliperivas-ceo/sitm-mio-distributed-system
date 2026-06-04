package com.sitmmio.analytics.speed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ActiveRoutesLoader {

    public Set<String> loadActiveRoutes(String filePath) {
        Set<String> routes = new HashSet<>();
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("No existe el archivo de rutas activas: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] columns = line.split(",");

                if (firstLine && containsLetters(columns[0])) {
                    firstLine = false;
                    continue;
                }

                firstLine = false;

                String routeId = columns[0].trim();
                if (!routeId.isEmpty()) {
                    routes.add(routeId);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error leyendo rutas activas: " + e.getMessage(), e);
        }

        return routes;
    }

    private boolean containsLetters(String value) {
        return value != null && value.matches(".*[a-zA-Z].*");
    }
}