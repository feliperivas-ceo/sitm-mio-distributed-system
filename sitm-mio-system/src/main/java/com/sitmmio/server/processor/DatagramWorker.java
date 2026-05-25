package com.sitmmio.server.processor;

import com.sitmmio.common.model.Datagrama;
import com.sitmmio.common.util.DateUtils;
import com.sitmmio.server.persistence.CsvPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Worker unitario del patrón Master-Worker (Req 11).
 * Extrae latitud, longitud, timestamp e ID del bus de cada datagrama
 * y los persiste en CSV.
 *
 * Implementa Runnable para ser ejecutado por el ThreadPoolProcessor.
 */
public class DatagramWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DatagramWorker.class);

    private final Datagrama    datagrama;
    private final CsvPersistence csv;

    public DatagramWorker(Datagrama datagrama, CsvPersistence csv) {
        this.datagrama = datagrama;
        this.csv       = csv;
    }

    @Override
    public void run() {
        try {
            // Req 11: extraer campos clave
            String busId     = extraerBusId();
            double latitud   = extraerLatitud();
            double longitud  = extraerLongitud();
            String timestamp = extraerTimestamp();

            log.debug("Procesando: busId={} lat={} lon={} ts={}",
                    busId, latitud, longitud, timestamp);

            // Persistencia CSV (Req 6)
            csv.persistir(busId, latitud, longitud, timestamp,
                    datagrama.getTipoEvento() != null ? datagrama.getTipoEvento().name() : "UNKNOWN",
                    datagrama.getPrioridad()  != null ? datagrama.getPrioridad().getNivel() : 1,
                    datagrama.getMessageId(),
                    datagrama.getReceivedAt());

            datagrama.setProcesado(true);

        } catch (Exception e) {
            log.error("Error procesando datagrama busId={}: {}",
                    datagrama.getId(), e.getMessage(), e);
        }
    }

    // --- extracción de campos (Req 11) ---

    private String extraerBusId() {
        return datagrama.getIdBus() != null ? datagrama.getIdBus() : "DESCONOCIDO";
    }

    private double extraerLatitud() {
        return datagrama.getLatitud();
    }

    private double extraerLongitud() {
        return datagrama.getLongitud();
    }

    private String extraerTimestamp() {
        LocalDateTime ts = datagrama.getTimestamp();

        if (ts != null) {
            return DateUtils.format(ts);
        }
        
        LocalDateTime received = datagrama.getReceivedAt();
        return received != null ? DateUtils.format(received) : DateUtils.nowString();
    }
}
