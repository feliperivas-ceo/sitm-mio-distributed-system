package com.sitmmio.server;

import com.sitmmio.server.ice.DatagramaReceiverI;
import com.sitmmio.server.ice.IceServerConfig;
import com.sitmmio.server.messaging.ReliableMessaging;
import com.sitmmio.server.queue.DatagramaQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Servidor Central — patrón Consumer.
 *
 * Responsabilidades:
 *  - Arrancar el ObjectAdapter ICE y registrar el servant DatagramaReceiverI.
 *  - El ICE server corre en un hilo daemon para no bloquear Spring.
 *  - El pipeline de procesamiento (MasterWorker + ThreadPool) es gestionado
 *    por sus propios @PostConstruct en sus respectivos @Component.
 */
@Component
public class CentralServer {

    private static final Logger log = LoggerFactory.getLogger(CentralServer.class);

    @Value("${sitmmio.ice.port:10000}")
    private int icePuerto;

    private final DatagramaQueue    queue;
    private final ReliableMessaging reliableMessaging;

    private final IceServerConfig iceConfig = new IceServerConfig();
    private Thread                iceThread;

    public CentralServer(DatagramaQueue queue, ReliableMessaging reliableMessaging) {
        this.queue             = queue;
        this.reliableMessaging = reliableMessaging;
    }

    @PostConstruct
    public void iniciar() {
        log.info("Iniciando CentralServer en puerto ICE {}…", icePuerto);

        DatagramaReceiverI servant = new DatagramaReceiverI(queue, reliableMessaging);

        // ICE corre en hilo daemon para no bloquear el contexto Spring
        iceThread = new Thread(() -> {
            try {
                iceConfig.iniciar(servant, icePuerto);
                iceConfig.waitForShutdown();     // bloquea hasta destroy()
            } catch (Exception e) {
                log.error("Error en servidor ICE: {}", e.getMessage(), e);
            }
        }, "ice-server");
        iceThread.setDaemon(true);
        iceThread.start();

        log.info("CentralServer activo. Esperando datagramas ICE en puerto {}.", icePuerto);
    }

    @PreDestroy
    public void detener() {
        log.info("Deteniendo CentralServer…");
        iceConfig.detener();
        reliableMessaging.shutdown();
        if (iceThread != null) iceThread.interrupt();
    }
}
