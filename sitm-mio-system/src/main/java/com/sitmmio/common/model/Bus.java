package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "buses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    private String id;

    private String placa;
    private String rutaAsignada;

    private double latitudActual;
    private double longitudActual;

    private LocalDateTime ultimaActualizacion;

    @Enumerated(EnumType.STRING)
    private EstadoBus estado;

    public enum EstadoBus { ACTIVO, INACTIVO, EMERGENCIA }
}
