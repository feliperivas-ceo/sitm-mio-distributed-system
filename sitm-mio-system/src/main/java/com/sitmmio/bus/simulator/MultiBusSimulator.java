package com.sitmmio.bus.simulator;

public class MultiBusSimulator {

    private int cantidadBuses;

    public MultiBusSimulator(int cantidadBuses) {
        this.cantidadBuses = cantidadBuses;
    }

    public void iniciarSimulacion() {

        System.out.println(
            "Iniciando simulación con "
            + cantidadBuses
            + " buses"
        );

        for (int i = 1; i <= cantidadBuses; i++) {

            System.out.println(
                "Bus simulado #" + i
            );
        }
    }
}
