package com.sitmmio.server.persistence;

import com.sitmmio.common.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Persiste datagramas procesados en CSV de forma escalable (Req 6).
 *
 * Escalabilidad:
 *  - BufferedWriter en modo append → evita reescribir el archivo completo.
 *  - ReentrantLock → escritura thread-safe desde el ThreadPool.
 *  - Flush periódico configurable para balancear latencia vs I/O.
 */
@Component
public class CsvPersistence {

    private static final Logger log = LoggerFactory.getLogger(CsvPersistence.class);

    private static final String HEADER =
            "busId,latitud,longitud,timestamp,tipoEvento,prioridad,messageId,receivedAt\n";

    @Value("${sitmmio.csv.path:data/datagramas_live.csv}")
    private String csvPath;

    @Value("${sitmmio.csv.flush-every:50}")
    private int flushEvery;

    private final ReentrantLock lock = new ReentrantLock();
    private BufferedWriter writer;
    private long escrituras = 0;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(csvPath);
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());

            boolean existe = Files.exists(path);
            writer = new BufferedWriter(
                    new FileWriter(path.toFile(), true));   // append=true

            if (!existe) {
                writer.write(HEADER);
                writer.flush();
            }
            log.info("CsvPersistence listo en '{}'", path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir el CSV de persistencia: " + csvPath, e);
        }
    }

    /**
     * Escribe una fila CSV. Thread-safe mediante ReentrantLock.
     */
    public void persistir(String busId, double latitud, double longitud,
                          String timestamp, String tipoEvento, int prioridad,
                          String messageId, LocalDateTime receivedAt) {
        String linea = String.format("%s,%.6f,%.6f,%s,%s,%d,%s,%s\n",
                escapar(busId),
                latitud,
                longitud,
                escapar(timestamp),
                escapar(tipoEvento),
                prioridad,
                escapar(messageId),
                DateUtils.format(receivedAt != null ? receivedAt : LocalDateTime.now()));

        lock.lock();
        try {
            writer.write(linea);
            escrituras++;
            if (escrituras % flushEvery == 0) {
                writer.flush();
                log.debug("CSV flush en escritura #{}", escrituras);
            }
        } catch (IOException e) {
            log.error("Error escribiendo CSV: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /** Fuerza flush del buffer. */
    public void flush() {
        lock.lock();
        try { writer.flush(); }
        catch (IOException e) { log.warn("Flush fallido: {}", e.getMessage()); }
        finally { lock.unlock(); }
    }

    @PreDestroy
    public void cerrar() {
        flush();
        lock.lock();
        try {
            if (writer != null) writer.close();
            log.info("CsvPersistence cerrado. Total escrituras: {}", escrituras);
        } catch (IOException e) {
            log.warn("Error cerrando writer CSV: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    public long getEscrituras() { return escrituras; }
}
