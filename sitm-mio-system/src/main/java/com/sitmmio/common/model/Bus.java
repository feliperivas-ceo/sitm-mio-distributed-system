package com.sitmmio.common.model;

public class Bus {

    private String idBus;
    private String idRuta;
    private double latitud;
    private double longitud;

    public Bus(String idBus, String idRuta, double latitud, double longitud) {
        this.idBus = idBus;
        this.idRuta = idRuta;
        this.latitud = latitud;
        this.longitud = longitud;
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

    public void actualizarPosicion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
}