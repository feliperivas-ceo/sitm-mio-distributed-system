package com.sitmmio.server.processor;

import com.sitmmio.common.model.Datagrama;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Procesa eventos de emergencia o prioridad CRITICAL.
 * En producción dispararía alertas a operadores.
 */
@Component
public class CriticalEventProcessor implements EventProcessor {

    private static final Logger log = LoggerFactory.getLogger(CriticalEventProcessor.class);

    @Override
    public boolean acepta(Datagrama datagrama) {
        return datagrama.getTipoEvento() == TipoEvento.EMERGENCY
                || datagrama.getPrioridad() == Prioridad.CRITICAL;
    }

    @Override
    public String procesar(Datagrama datagrama) {
        log.error("*** EVENTO CRÍTICO *** bus={} tipo={} msg={}",
                datagrama.getBusId(),
                datagrama.getTipoEvento(),
                datagrama.getMessageId());
        // Aquí se integraría: WebSocket push al dashboard, SMS, etc.
        return String.format("CRITICO[%s] tipo=%s",
                datagrama.getBusId(), datagrama.getTipoEvento());
    }
}
