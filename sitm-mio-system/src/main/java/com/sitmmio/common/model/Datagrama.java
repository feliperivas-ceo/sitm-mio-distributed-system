package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "datagramas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Datagrama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idBus;
    private String idRuta;

    private Double latitud;
    private Double longitud;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String estadoPuertas;

    @Column(length = 1024)
    private String descripcion;

    private String messageId;

    @Column(length = 2048)
    private String payload;

    private LocalDateTime receivedAt;
    private boolean procesado;

    public Datagrama(String idBus, String idRuta, double latitud, double longitud,
                     TipoEvento tipoEvento, Prioridad prioridad,
                     String estadoPuertas, String descripcion) {
        this.idBus = idBus;
        this.idRuta = idRuta;
        this.latitud = latitud;
        this.longitud = longitud;
        this.timestamp = LocalDateTime.now();
        this.tipoEvento = tipoEvento;
        this.prioridad = prioridad;
        this.estadoPuertas = estadoPuertas;
        this.descripcion = descripcion;
        this.messageId = UUID.randomUUID().toString();
        this.receivedAt = null;
        this.procesado = false;
        this.payload = descripcion;
    }

    @Override
    public String toString() {
        return "Datagrama{" +
                "id=" + id +
                ", idBus='" + idBus + '\'' +
                ", idRuta='" + idRuta + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", timestamp=" + timestamp +
                ", tipoEvento=" + tipoEvento +
                ", prioridad=" + prioridad +
                ", estadoPuertas='" + estadoPuertas + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", messageId='" + messageId + '\'' +
                ", procesado=" + procesado +
                '}';
    }
}
