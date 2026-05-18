package com.sitmmio.common.model;

import java.time.LocalDateTime;

public class Evento {

    private TipoEvento tipoEvento;
    private Prioridad prioridad;
    private String descripcion;
    private LocalDateTime timestamp;

    public Evento(TipoEvento tipoEvento, Prioridad prioridad, String descripcion) {
        this.tipoEvento = tipoEvento;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.timestamp = LocalDateTime.now();
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}