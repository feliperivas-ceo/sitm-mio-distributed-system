package com.sitmmio.bus.sensor;

import java.util.Random;

public class SensorPuertas implements Sensor<String> {

    private final Random random = new Random();

    @Override
    public String leer() {
        return random.nextBoolean() ? "ABIERTAS" : "CERRADAS";
    }
}