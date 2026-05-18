package com.sitmmio.common.model;

public enum TipoEvento {
    GPS_UPDATE,
    DOOR_OPEN,
    DOOR_CLOSE,
    ENGINE_ON,
    ENGINE_OFF,
    PASSENGER_COUNT,
    EMERGENCY;

    public static TipoEvento fromString(String value) {
        if (value == null) return GPS_UPDATE;
        try { return valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) { return GPS_UPDATE; }
    }
}
