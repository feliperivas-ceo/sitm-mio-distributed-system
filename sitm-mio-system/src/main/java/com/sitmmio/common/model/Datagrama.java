package com.sitmmio.common.model;

import java.time.LocalDateTime;

public class Datagrama {

    private String idBus;
    private String idRuta;
    private double latitud;
    private double longitud;
    private LocalDateTime timestamp;
    private TipoEvento tipoEvento;
    private Prioridad prioridad;
    private String estadoPuertas;
    private String descripcion;

    public Datagrama(String idBus, String idRuta, double latitud, double longitud,
                     TipoEvento tipoEvento, Prioridad prioridad,
                     String estadoPuertas, String descripcion) {
        this.idBus = idBus;
        this.idRuta = idRuta;
        this.latitud = latitud;
        this.longitud = longitud;
        this.timestamp = LocalDateTime.now();
        this.tipoEvento = tipoEvento;
        this.prioridad = prioridad;
        this.estadoPuertas = estadoPuertas;
        this.descripcion = descripcion;
    }

    public String getIdBus() {
        return idBus;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getEstadoPuertas() {
        return estadoPuertas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return "Datagrama{" +
                "idBus='" + idBus + '\'' +
                ", idRuta='" + idRuta + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", timestamp=" + timestamp +
                ", tipoEvento=" + tipoEvento +
                ", prioridad=" + prioridad +
                ", estadoPuertas='" + estadoPuertas + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}