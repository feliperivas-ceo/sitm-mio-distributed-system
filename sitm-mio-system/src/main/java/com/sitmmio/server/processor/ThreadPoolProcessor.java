package com.sitmmio.server.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * Abstracción sobre ExecutorService con pool de hilos fijo.
 * Todos los DatagramWorker son enviados aquí para ejecución concurrente.
 */
@Component
public class ThreadPoolProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolProcessor.class);

    private static final int POOL_SIZE       = Runtime.getRuntime().availableProcessors() * 2;
    private static final int QUEUE_CAPACITY  = 5_000;
    private static final long SHUTDOWN_SECS  = 10;

    private final ExecutorService executor;

    public ThreadPoolProcessor() {
        this.executor = new ThreadPoolExecutor(
                POOL_SIZE,
                POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                r -> {
                    Thread t = new Thread(r, "datagram-worker-" + System.nanoTime());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()   // back-pressure: el caller ejecuta
        );
        log.info("ThreadPoolProcessor iniciado con {} hilos", POOL_SIZE);
    }

    /** Envía un worker para ejecución asíncrona. */
    public Future<?> submit(Runnable worker) {
        return executor.submit(worker);
    }

    /** Estadísticas para monitoreo. */
    public String estadisticas() {
        if (executor instanceof ThreadPoolExecutor tpe) {
            return String.format("activos=%d completados=%d encolados=%d",
                    tpe.getActiveCount(),
                    tpe.getCompletedTaskCount(),
                    tpe.getQueue().size());
        }
        return "N/A";
    }

    @PreDestroy
    public void shutdown() {
        log.info("Cerrando ThreadPoolProcessor…");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_SECS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
