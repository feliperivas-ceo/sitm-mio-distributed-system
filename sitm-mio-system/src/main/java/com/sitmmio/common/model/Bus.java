package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    private String idBus;

    private String placa;
    private String idRuta;

    private double latitud;
    private double longitud;

    private LocalDateTime ultimaActualizacion;

    @Enumerated(EnumType.STRING)
    private EstadoBus estado;

    public Bus(String idBus, String idRuta, double latitud, double longitud) {
        this.idBus = idBus;
        this.idRuta = idRuta;
        this.latitud = latitud;
        this.longitud = longitud;
        this.ultimaActualizacion = LocalDateTime.now();
        this.estado = EstadoBus.ACTIVO;
    }

    public void actualizarPosicion(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public enum EstadoBus {
        ACTIVO,
        INACTIVO,
        EMERGENCIA
    }
}