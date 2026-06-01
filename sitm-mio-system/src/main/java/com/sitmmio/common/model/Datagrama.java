package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "datagramas")
@Getter
@Setter
@NoArgsConstructor
public class Datagrama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String busId;
    private Double latitud;
    private Double longitud;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String messageId;
    private LocalDateTime receivedAt;
}
