package com.sitmmio.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
public class Bus {

    @Id
    private String id;
    private String numeroPlaca;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    private Double latitud;
    private Double longitud;
    private LocalDateTime ultimoTimestamp;

    @Enumerated(EnumType.STRING)
    private TipoEvento ultimoEvento;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String estado;
    private Double velocidad;

    // ── Métodos de compatibilidad con API de origin/main ─────────────────────

    public String getIdBus() { return id; }
    public void setIdBus(String idBus) { this.id = idBus; }

    public String getIdRuta() { return ruta != null ? ruta.getId() : null; }

    public void setUltimaActualizacion(LocalDateTime ts) { this.ultimoTimestamp = ts; }
    public LocalDateTime getUltimaActualizacion() { return ultimoTimestamp; }

    public void setEstado(String estado) { this.estado = estado; }

    public void setEstado(EstadoBus estadoBus) {
        this.estado = estadoBus != null ? estadoBus.name() : null;
    }

    public enum EstadoBus {
        ACTIVO, INACTIVO, EMERGENCIA
    }
}
