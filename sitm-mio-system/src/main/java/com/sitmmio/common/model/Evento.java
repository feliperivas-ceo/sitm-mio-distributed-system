package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String busId;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String descripcion;

    private LocalDateTime timestamp;

    private boolean procesado;

    private boolean ackEnviado;

    public Evento(TipoEvento tipoEvento,
                  Prioridad prioridad,
                  String descripcion) {

        this.tipoEvento = tipoEvento;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.timestamp = LocalDateTime.now();
        this.procesado = false;
        this.ackEnviado = false;
    }
}