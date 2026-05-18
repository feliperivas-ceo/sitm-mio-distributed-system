package com.sitmmio.bus.simulator;

import com.sitmmio.bus.ice.IceBusClient;
import com.sitmmio.bus.sensor.SensorGPS;
import com.sitmmio.bus.sensor.SensorMotor;
import com.sitmmio.bus.sensor.SensorPuertas;
import com.sitmmio.common.model.Bus;
import com.sitmmio.common.model.Datagrama;
import com.sitmmio.common.model.Evento;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;

public class BusSimulator implements Runnable {

    private final Bus bus;
    private final SensorGPS sensorGPS;
    private final SensorPuertas sensorPuertas;
    private final SensorMotor sensorMotor;
    private final EventGenerator eventGenerator;
    private final IceBusClient iceBusClient;

    public BusSimulator(Bus bus, IceBusClient iceBusClient) {
        this.bus = bus;
        this.sensorGPS = new SensorGPS(bus.getLatitud(), bus.getLongitud());
        this.sensorPuertas = new SensorPuertas();
        this.sensorMotor = new SensorMotor();
        this.eventGenerator = new EventGenerator();
        this.iceBusClient = iceBusClient;
    }

    @Override
    public void run() {
        while (true) {
            try {
                double[] posicion = sensorGPS.leer();
                String estadoPuertas = sensorPuertas.leer();
                boolean fallaMotor = sensorMotor.leer();

                Evento evento = fallaMotor
                        ? new Evento(TipoEvento.FALLA_MOTOR, Prioridad.CRITICA, "Falla de motor detectada por sensor")
                        : eventGenerator.generarEventoAutomatico();

                Datagrama datagrama = new Datagrama(
                        bus.getIdBus(),
                        bus.getIdRuta(),
                        posicion[0],
                        posicion[1],
                        evento.getTipoEvento(),
                        evento.getPrioridad(),
                        estadoPuertas,
                        evento.getDescripcion()
                );

                iceBusClient.enviarDatagrama(datagrama);

                Thread.sleep(5000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Simulación detenida para el bus " + bus.getIdBus());
                break;
            }
        }
    }
}