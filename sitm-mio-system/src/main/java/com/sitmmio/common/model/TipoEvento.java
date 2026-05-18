package com.sitmmio.common.model;

public enum TipoEvento {

    POSICION_GPS,
    PUERTAS_ABIERTAS,
    PUERTAS_CERRADAS,
    FALLA_MOTOR,
    PINCHAZO,
    ACCIDENTE,
    CONGESTION,
    SEGURIDAD,

    ENGINE_ON,
    ENGINE_OFF,
    PASSENGER_COUNT,
    EMERGENCY;

    public static TipoEvento fromString(String value) {

        if (value == null) {
            return POSICION_GPS;
        }

        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return POSICION_GPS;
        }
    }
}