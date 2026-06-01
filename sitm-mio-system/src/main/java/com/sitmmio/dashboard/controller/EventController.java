package com.sitmmio.dashboard.controller;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class EventController {

    private final List<String> eventos = new ArrayList<>();

    public void registrarEvento(String evento) {

        eventos.add(evento);

        System.out.println("EVENTO REGISTRADO: " + evento);
    }

    public List<String> obtenerEventos() {
        return eventos;
    }

    public List<String> obtenerEventosCriticos() {

        return eventos.stream()
                .filter(e -> e.toLowerCase().contains("accidente")
                        || e.toLowerCase().contains("falla")
                        || e.toLowerCase().contains("choque"))
                .toList();
    }
}
