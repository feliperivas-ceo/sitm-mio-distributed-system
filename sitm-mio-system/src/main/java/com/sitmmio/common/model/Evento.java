package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
    private TipoEvento tipo;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String descripcion;
    private LocalDateTime timestamp;

    private boolean procesado;
    private boolean ackEnviado;
}
