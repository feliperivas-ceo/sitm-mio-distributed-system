package com.sitmmio.bus.sensor;

import java.util.Random;

public class SensorMotor implements Sensor<Boolean> {

    private final Random random = new Random();

    @Override
    public Boolean leer() {
        return random.nextInt(100) < 5;
    }
}