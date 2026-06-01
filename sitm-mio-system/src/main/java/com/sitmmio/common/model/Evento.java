package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private Double latitud;
    private Double longitud;
    private LocalDateTime timestamp;
    private Boolean estadoProcesado;
    private String descripcion;
    private boolean ackEnviado;

    public Evento(TipoEvento tipoEvento, Prioridad prioridad, String descripcion) {
        this.tipoEvento = tipoEvento;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.timestamp = java.time.LocalDateTime.now();
        this.estadoProcesado = false;
        this.ackEnviado = false;
    }
}
