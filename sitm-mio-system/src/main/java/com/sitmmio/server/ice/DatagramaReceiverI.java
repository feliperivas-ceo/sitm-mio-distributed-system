package com.sitmmio.server.ice;

import SitmMio.AckICE;
import SitmMio.DatagramaICE;
import SitmMio._DatagramaReceiverDisp;
import com.sitmmio.common.model.Datagrama;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;
import com.sitmmio.server.messaging.ReliableMessaging;
import com.sitmmio.server.queue.DatagramaQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Servant ICE — patrón Consumer.
 * Recibe DatagramaICE del bus, lo convierte al modelo de dominio
 * y lo encola para procesamiento asíncrono.
 */
public class DatagramaReceiverI extends _DatagramaReceiverDisp {

    private static final Logger log = LoggerFactory.getLogger(DatagramaReceiverI.class);

    private final DatagramaQueue queue;
    private final ReliableMessaging reliableMessaging;

    public DatagramaReceiverI(DatagramaQueue queue, ReliableMessaging reliableMessaging) {
        this.queue             = queue;
        this.reliableMessaging = reliableMessaging;
    }

    /** Operación estándar — fire-and-forget hacia la EventQueue. */
    @Override
    public void enviarDatagrama(DatagramaICE ice, com.zeroc.Ice.Current current) {
        log.debug("ICE recibido: {}", ice);
        Datagrama datagrama = convertir(ice);
        queue.encolar(datagrama);
    }

    /** Operación crítica — retorna ACK tras encolar y confirmar registro. */
    @Override
    public AckICE enviarDatagramaCritico(DatagramaICE ice, com.zeroc.Ice.Current current) {
        log.info("Datagrama CRÍTICO recibido: bus={} msg={}", ice.busId, ice.messageId);
        Datagrama datagrama = convertir(ice);

        reliableMessaging.registrarPendiente(ice.messageId);
        queue.encolar(datagrama);
        reliableMessaging.confirmarRecepcion(ice.messageId);

        return new AckICE(true, ice.messageId, "Recibido por servidor central");
    }

    private Datagrama convertir(DatagramaICE ice) {
        return Datagrama.builder()
                .busId(ice.busId)
                .latitud(ice.latitud)
                .longitud(ice.longitud)
                .timestamp(ice.timestamp)
                .tipoEvento(TipoEvento.fromString(ice.tipoEvento))
                .prioridad(Prioridad.fromNivel(ice.prioridad))
                .messageId(ice.messageId)
                .payload(ice.payload)
                .receivedAt(LocalDateTime.now())
                .procesado(false)
                .build();
    }
}
