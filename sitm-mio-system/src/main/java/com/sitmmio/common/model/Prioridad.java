package com.sitmmio.common.model;

public enum Prioridad {
    LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

    private final int nivel;

    Prioridad(int nivel) { this.nivel = nivel; }

    public int getNivel() { return nivel; }

    public static Prioridad fromNivel(int nivel) {
        for (Prioridad p : values()) {
            if (p.nivel == nivel) return p;
        }
        return LOW;
    }

    public boolean esCritica() { return this == CRITICAL; }
}
