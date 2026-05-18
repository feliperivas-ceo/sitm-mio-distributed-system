package com.sitmmio.bus.simulator;

import com.sitmmio.bus.ice.IceBusClient;
import com.sitmmio.common.model.Bus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiBusSimulator {

    private final ExecutorService executorService;
    private final IceBusClient iceBusClient;

    public MultiBusSimulator(int cantidadBuses) {
        this.executorService = Executors.newFixedThreadPool(cantidadBuses);
        this.iceBusClient = new IceBusClient();
    }

    public void iniciarSimulacion() {
        List<Bus> buses = List.of(
                new Bus("BUS-001", "RUTA-E21", 3.3415, -76.5300),
                new Bus("BUS-002", "RUTA-E21", 3.3420, -76.5310),
                new Bus("BUS-003", "RUTA-P21", 3.3450, -76.5320),
                new Bus("BUS-004", "RUTA-T31", 3.3480, -76.5340),
                new Bus("BUS-005", "RUTA-A11", 3.3500, -76.5360)
        );

        for (Bus bus : buses) {
            executorService.submit(new BusSimulator(bus, iceBusClient));
        }
    }

    public void detenerSimulacion() {
        executorService.shutdownNow();
    }
}