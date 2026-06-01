package com.sitmmio.dashboard.view;

import com.sitmmio.common.model.Bus;
import com.sitmmio.common.model.Evento;
import com.sitmmio.common.util.DateUtils;
import lombok.Data;
import java.util.List;

@Data
public class MapView {

    private String id;
    private String numeroPlaca;
    private String rutaId;
    private String rutaNombre;
    private String rutaColor;
    private Double latitud;
    private Double longitud;
    private String ultimoTimestamp;
    private String ultimoEvento;
    private String prioridad;
    private String estado;
    private Double velocidad;

    public static MapView fromBus(Bus bus) {
        MapView v = new MapView();
        v.setId(bus.getId());
        v.setNumeroPlaca(bus.getNumeroPlaca());
        if (bus.getRuta() != null) {
            v.setRutaId(bus.getRuta().getId());
            v.setRutaNombre(bus.getRuta().getNombre());
            v.setRutaColor(bus.getRuta().getColor());
        }
        v.setLatitud(bus.getLatitud());
        v.setLongitud(bus.getLongitud());
        v.setUltimoTimestamp(DateUtils.formatDateTime(bus.getUltimoTimestamp()));
        v.setUltimoEvento(bus.getUltimoEvento() != null ? bus.getUltimoEvento().name() : "N/A");
        v.setPrioridad(bus.getPrioridad() != null ? bus.getPrioridad().name() : "N/A");
        v.setEstado(bus.getEstado());
        v.setVelocidad(bus.getVelocidad());
        return v;
    }
}
