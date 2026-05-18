package com.sitmmio.server.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Mensajería confiable con ACK para eventos críticos.
 *
 * Flujo:
 *  1. registrarPendiente(messageId)  — el servidor anota que espera confirmar.
 *  2. confirmarRecepcion(messageId)  — confirmado: el ACK fue enviado al bus.
 *  3. Scheduler periódico detecta mensajes sin confirmar en > TTL segundos
 *     y los registra como fallidos para reintento o alerta.
 */
@Component
public class ReliableMessaging {

    private static final Logger log = LoggerFactory.getLogger(ReliableMessaging.class);

    private static final long TTL_SEGUNDOS   = 10;
    private static final long SWEEP_SEGUNDOS = 5;

    /** messageId → timestamp de registro */
    private final Map<String, Instant> pendientes  = new ConcurrentHashMap<>();
    /** messageId confirmados correctamente */
    private final Set<String> confirmados = ConcurrentHashMap.newKeySet();
    /** messageId que expiraron sin confirmación */
    private final Set<String> fallidos    = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService sweeper =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "reliable-messaging-sweeper");
                t.setDaemon(true);
                return t;
            });

    public ReliableMessaging() {
        sweeper.scheduleAtFixedRate(this::barrerExpirados,
                SWEEP_SEGUNDOS, SWEEP_SEGUNDOS, TimeUnit.SECONDS);
    }

    /**
     * Registra un messageId como pendiente de confirmación.
     * Llamado antes de encolar el datagrama crítico.
     */
    public void registrarPendiente(String messageId) {
        if (messageId == null || messageId.isBlank()) return;
        pendientes.put(messageId, Instant.now());
        log.debug("ACK pendiente registrado: {}", messageId);
    }

    /**
     * Confirma que el mensaje fue procesado y el ACK enviado al bus.
     */
    public void confirmarRecepcion(String messageId) {
        if (messageId == null) return;
        boolean existia = pendientes.remove(messageId) != null;
        if (existia) {
            confirmados.add(messageId);
            log.debug("ACK confirmado: {}", messageId);
        } else {
            log.warn("Intento de confirmar messageId no registrado: {}", messageId);
        }
    }

    /**
     * Marca un messageId como fallido manualmente (p.ej. excepción en procesamiento).
     */
    public void marcarFallido(String messageId, String razon) {
        pendientes.remove(messageId);
        fallidos.add(messageId);
        log.error("Mensaje crítico fallido: id={} razon={}", messageId, razon);
    }

    public boolean estaConfirmado(String messageId) { return confirmados.contains(messageId); }
    public boolean estaFallido(String messageId)    { return fallidos.contains(messageId); }
    public int     pendientesCount()                { return pendientes.size(); }

    /** Detecta mensajes que llevan más de TTL segundos sin confirmarse. */
    private void barrerExpirados() {
        Instant limite = Instant.now().minusSeconds(TTL_SEGUNDOS);
        pendientes.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(limite)) {
                fallidos.add(entry.getKey());
                log.warn("Mensaje crítico expiró sin ACK: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void shutdown() {
        sweeper.shutdown();
    }
}
