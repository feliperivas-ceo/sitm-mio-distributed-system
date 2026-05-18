package com.sitmmio.bus.simulator;

import com.sitmmio.common.model.Evento;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;

import java.util.Random;

public class EventGenerator {

    private final Random random = new Random();

    public Evento generarEventoAutomatico() {
        int numero = random.nextInt(100);

        if (numero < 70) {
            return new Evento(
                    TipoEvento.POSICION_GPS,
                    Prioridad.BAJA,
                    "Actualización normal de posición GPS"
            );
        }

        if (numero < 80) {
            return new Evento(
                    TipoEvento.CONGESTION,
                    Prioridad.MEDIA,
                    "Congestión detectada en la vía"
            );
        }

        if (numero < 90) {
            return new Evento(
                    TipoEvento.PINCHAZO,
                    Prioridad.ALTA,
                    "Posible pinchazo detectado"
            );
        }

        return new Evento(
                TipoEvento.ACCIDENTE,
                Prioridad.CRITICA,
                "Evento crítico reportado automáticamente"
        );
    }
}