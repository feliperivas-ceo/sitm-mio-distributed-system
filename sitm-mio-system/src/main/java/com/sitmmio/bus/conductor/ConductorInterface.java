package com.sitmmio.bus.conductor;

import com.sitmmio.common.model.Evento;
import com.sitmmio.common.model.Prioridad;
import com.sitmmio.common.model.TipoEvento;

import java.util.Scanner;

public class ConductorInterface {

    private final Scanner scanner = new Scanner(System.in);

    public Evento seleccionarEvento() {
        System.out.println("=== Selector de eventos del conductor ===");
        System.out.println("1. Pinchazo");
        System.out.println("2. Avería grave de motor");
        System.out.println("3. Bloqueo o trancón");
        System.out.println("4. Choque del bus");
        System.out.println("5. Incidente de seguridad");

        int opcion = scanner.nextInt();

        return switch (opcion) {
            case 1 -> new Evento(TipoEvento.PINCHAZO, Prioridad.ALTA, "Pinchazo reportado por conductor");
            case 2 -> new Evento(TipoEvento.FALLA_MOTOR, Prioridad.CRITICA, "Avería grave de motor");
            case 3 -> new Evento(TipoEvento.CONGESTION, Prioridad.MEDIA, "Bloqueo o trancón en la vía");
            case 4 -> new Evento(TipoEvento.ACCIDENTE, Prioridad.CRITICA, "Choque del bus");
            case 5 -> new Evento(TipoEvento.SEGURIDAD, Prioridad.ALTA, "Incidente de seguridad");
            default -> new Evento(TipoEvento.POSICION_GPS, Prioridad.BAJA, "Evento normal");
        };
    }
}