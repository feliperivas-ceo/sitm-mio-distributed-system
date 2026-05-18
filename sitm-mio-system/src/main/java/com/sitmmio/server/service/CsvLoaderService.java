package com.sitmmio.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos históricos desde CSV al arrancar la aplicación.
 */
@Service
public class CsvLoaderService {

    private static final Logger log = LoggerFactory.getLogger(CsvLoaderService.class);

    /**
     * Lee un CSV y retorna sus filas como listas de strings (sin header).
     */
    public List<List<String>> cargar(String rutaArchivo) {
        List<List<String>> filas = new ArrayList<>();
        Path path = Paths.get(rutaArchivo);

        if (!Files.exists(path)) {
            log.warn("Archivo CSV no encontrado: {}", rutaArchivo);
            return filas;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; } // saltar header
                if (linea.isBlank()) continue;
                filas.add(List.of(linea.split(",")));
            }
            log.info("CSV cargado: {} → {} filas", rutaArchivo, filas.size());
        } catch (IOException e) {
            log.error("Error leyendo CSV '{}': {}", rutaArchivo, e.getMessage());
        }
        return filas;
    }
}
