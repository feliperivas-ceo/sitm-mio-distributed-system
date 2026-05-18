package com.sitmmio.server.processor;

import com.sitmmio.common.model.Datagrama;
import com.sitmmio.common.model.TipoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Procesa actualizaciones de posición GPS.
 * Valida que lat/lon estén dentro de la zona de Cali, Colombia.
 */
@Component
public class GPSProcessor implements EventProcessor {

    private static final Logger log = LoggerFactory.getLogger(GPSProcessor.class);

    // Bounding box aproximado de Cali
    private static final double LAT_MIN =  3.3, LAT_MAX =  3.6;
    private static final double LON_MIN = -76.7, LON_MAX = -76.4;

    @Override
    public boolean acepta(Datagrama datagrama) {
        return datagrama.getTipoEvento() == TipoEvento.GPS_UPDATE;
    }

    @Override
    public String procesar(Datagrama datagrama) {
        double lat = datagrama.getLatitud();
        double lon = datagrama.getLongitud();
        boolean dentroDeZona = lat >= LAT_MIN && lat <= LAT_MAX
                            && lon >= LON_MIN && lon <= LON_MAX;
        if (!dentroDeZona) {
            log.warn("GPS fuera de zona Cali: bus={} lat={} lon={}",
                    datagrama.getBusId(), lat, lon);
        }
        log.debug("GPS procesado: bus={} lat={} lon={} zona={}",
                datagrama.getBusId(), lat, lon, dentroDeZona);
        return String.format("GPS[%s] lat=%.5f lon=%.5f zona=%b",
                datagrama.getBusId(), lat, lon, dentroDeZona);
    }
}
