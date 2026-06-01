package com.sitmmio.common.model;

public enum TipoEvento {

    // Valores del sistema de dashboard (UI)
    GPS_NORMAL,
    CONGESTION,
    ACCIDENTE,
    MOTOR,
    PUERTAS,
    OTRO,

    // Valores del sistema de datagramas (ICE)
    POSICION_GPS,
    PUERTAS_ABIERTAS,
    PUERTAS_CERRADAS,
    FALLA_MOTOR,
    PINCHAZO,
    SEGURIDAD,
    ENGINE_ON,
    ENGINE_OFF,
    PASSENGER_COUNT,
    EMERGENCY;

    public static TipoEvento fromString(String value) {
        if (value == null) {
            return GPS_NORMAL;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GPS_NORMAL;
        }
    }
}
