package com.sitmmio.bus;

import com.sitmmio.bus.simulator.MultiBusSimulator;

public class BusSimulatorRunner {

    public static void main(String[] args) {
        MultiBusSimulator simulator = new MultiBusSimulator(5);
        simulator.iniciarSimulacion();
    }
}