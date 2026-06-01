package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estaciones")
@Getter
@Setter
@NoArgsConstructor
public class Estacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    private Double latitud;
    private Double longitud;
    private String tipo;  // PARADA o ESTACION_MAYOR
}
