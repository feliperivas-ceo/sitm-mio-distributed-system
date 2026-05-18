package com.sitmmio.bus.sensor;

import java.util.Random;

public class SensorGPS implements Sensor<double[]> {

    private final Random random = new Random();
    private double latitud;
    private double longitud;

    public SensorGPS(double latitudInicial, double longitudInicial) {
        this.latitud = latitudInicial;
        this.longitud = longitudInicial;
    }

    @Override
    public double[] leer() {
        latitud += (random.nextDouble() - 0.5) * 0.001;
        longitud += (random.nextDouble() - 0.5) * 0.001;

        return new double[]{latitud, longitud};
    }
}