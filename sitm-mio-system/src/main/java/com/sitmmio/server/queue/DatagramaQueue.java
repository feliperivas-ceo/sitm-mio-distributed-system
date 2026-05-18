package com.sitmmio.server.queue;

import com.sitmmio.common.model.Datagrama;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Fachada sobre EventQueue con métricas de monitoreo.
 */
@Component
public class DatagramaQueue {

    private static final Logger log = LoggerFactory.getLogger(DatagramaQueue.class);

    private final EventQueue eventQueue;

    public DatagramaQueue(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void encolar(Datagrama datagrama) {
        boolean aceptado = eventQueue.offerNonBlocking(datagrama);
        if (!aceptado) {
            log.warn("Cola llena — descartando datagrama de bus={}", datagrama.getBusId());
        } else {
            log.debug("Datagrama encolado: bus={} tipo={} [size={}]",
                    datagrama.getBusId(), datagrama.getTipoEvento(), eventQueue.size());
        }
    }

    public Datagrama tomar() throws InterruptedException {
        return eventQueue.dequeue();
    }

    public Datagrama tomarConTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        return eventQueue.poll(timeout, unit);
    }

    public int tamanio() { return eventQueue.size(); }
    public boolean estaVacia() { return eventQueue.isEmpty(); }
}
