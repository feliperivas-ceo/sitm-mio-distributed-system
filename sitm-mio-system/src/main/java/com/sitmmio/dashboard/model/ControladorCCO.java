package com.sitmmio.dashboard.model;

import java.util.List;

public class ControladorCCO extends Usuario {

    private String codigoControlador;

    private String zonaAsignada;

    private List<String> rutasMonitoreadas;

    public ControladorCCO() {
    }

    public ControladorCCO(String username,
                          String password,
                          Rol rol,
                          String codigoControlador,
                          String zonaAsignada,
                          List<String> rutasMonitoreadas) {

        super(username, password, rol);

        this.codigoControlador = codigoControlador;
        this.zonaAsignada = zonaAsignada;
        this.rutasMonitoreadas = rutasMonitoreadas;
    }

    public String getCodigoControlador() {
        return codigoControlador;
    }

    public void setCodigoControlador(String codigoControlador) {
        this.codigoControlador = codigoControlador;
    }

    public String getZonaAsignada() {
        return zonaAsignada;
    }

    public void setZonaAsignada(String zonaAsignada) {
        this.zonaAsignada = zonaAsignada;
    }

    public List<String> getRutasMonitoreadas() {
        return rutasMonitoreadas;
    }

    public void setRutasMonitoreadas(List<String> rutasMonitoreadas) {
        this.rutasMonitoreadas = rutasMonitoreadas;
    }

    @Override
    public String toString() {
        return "ControladorCCO{" +
                "codigoControlador='" + codigoControlador + '\'' +
                ", zonaAsignada='" + zonaAsignada + '\'' +
                ", rutasMonitoreadas=" + rutasMonitoreadas +
                '}';
    }
}
