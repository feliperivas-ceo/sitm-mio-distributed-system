package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "datagramas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Datagrama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String busId;

    private double latitud;
    private double longitud;

    /** Timestamp original del bus (ISO-8601). */
    private String timestamp;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    /** UUID generado por el bus para mensajería confiable. */
    private String messageId;

    @Column(length = 1024)
    private String payload;

    /** Momento en que el servidor central lo recibió. */
    private LocalDateTime receivedAt;

    /** true si ya fue procesado por el pipeline. */
    private boolean procesado;
}
