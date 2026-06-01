package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
public class Ruta {

    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String color;
    private String origen;
    private String destino;
}
