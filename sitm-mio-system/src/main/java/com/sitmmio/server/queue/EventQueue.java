package com.sitmmio.server.queue;

import com.sitmmio.common.model.Datagrama;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Cola asíncrona basada en LinkedBlockingQueue (Req async queue).
 * Capacidad máxima configurable para evitar desbordamiento de memoria.
 */
@Component
public class EventQueue {

    private static final int CAPACIDAD_DEFAULT = 10_000;

    private final LinkedBlockingQueue<Datagrama> queue;

    public EventQueue() {
        this.queue = new LinkedBlockingQueue<>(CAPACIDAD_DEFAULT);
    }

    /** Bloquea si la cola está llena (back-pressure). */
    public void enqueue(Datagrama datagrama) throws InterruptedException {
        queue.put(datagrama);
    }

    /** No bloquea: retorna false si la cola está llena. */
    public boolean offerNonBlocking(Datagrama datagrama) {
        return queue.offer(datagrama);
    }

    /** Bloquea hasta que haya un elemento disponible. */
    public Datagrama dequeue() throws InterruptedException {
        return queue.take();
    }

    /** Espera hasta timeout; retorna null si no hay elemento. */
    public Datagrama poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int size()    { return queue.size(); }
    public boolean isEmpty() { return queue.isEmpty(); }
    public int remainingCapacity() { return queue.remainingCapacity(); }
}
