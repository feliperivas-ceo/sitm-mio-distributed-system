package com.sitmmio.common.model;

public enum Prioridad {

    BAJA(1),
    MEDIA(2),
    ALTA(3),
    CRITICA(4);

    private final int nivel;

    Prioridad(int nivel) {
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }

    public static Prioridad fromNivel(int nivel) {
        for (Prioridad prioridad : values()) {
            if (prioridad.nivel == nivel) {
                return prioridad;
            }
        }
        return BAJA;
    }

    public boolean esCritica() {
        return this == CRITICA;
    }
}