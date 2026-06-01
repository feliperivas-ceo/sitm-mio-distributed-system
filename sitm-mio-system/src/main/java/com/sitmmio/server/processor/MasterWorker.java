package com.sitmmio.server.processor;

import com.sitmmio.common.model.Datagrama;
import com.sitmmio.server.persistence.CsvPersistence;
import com.sitmmio.server.queue.DatagramaQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementa el patrón Master-Worker.
 *
 * Master: hilo único que consume la EventQueue y delega cada
 *         Datagrama a un DatagramWorker via ThreadPoolProcessor.
 *
 * Workers: ejecutan DatagramWorker (extracción + CSV) en el pool.
 */
@Component
public class MasterWorker {

    private static final Logger log = LoggerFactory.getLogger(MasterWorker.class);
    private static final long POLL_TIMEOUT_MS = 500;

    private final DatagramaQueue      queue;
    private final ThreadPoolProcessor pool;
    private final CsvPersistence      csv;

    private final AtomicBoolean running    = new AtomicBoolean(false);
    private final AtomicLong    procesados = new AtomicLong(0);

    private Thread masterThread;

    public MasterWorker(DatagramaQueue queue,
                        ThreadPoolProcessor pool,
                        CsvPersistence csv) {
        this.queue = queue;
        this.pool  = pool;
        this.csv   = csv;
    }

    @PostConstruct
    public void iniciar() {
        running.set(true);
        masterThread = new Thread(this::buclemaster, "master-worker");
        masterThread.setDaemon(true);
        masterThread.start();
        log.info("MasterWorker iniciado.");
    }

    /** Bucle principal del Master: consume y delega. */
    private void buclemaster() {
        while (running.get()) {
            try {
                Datagrama d = queue.tomarConTimeout(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (d != null) {
                    pool.submit(new DatagramWorker(d, csv));
                    long total = procesados.incrementAndGet();
                    if (total % 100 == 0) {
                        log.info("Master: {} datagramas despachados | pool=[{}]",
                                total, pool.estadisticas());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("MasterWorker detenido. Total procesados: {}", procesados.get());
    }

    @PreDestroy
    public void detener() {
        running.set(false);
        if (masterThread != null) masterThread.interrupt();
    }

    public long getDatagramasProcesados() { return procesados.get(); }
}
