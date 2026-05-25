package com.sitmmio.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PublicService {

    public List<String> obtenerRutasDisponibles() {

        return Arrays.asList(
                "Ruta A - Norte",
                "Ruta B - Centro",
                "Ruta C - Sur"
        );
    }

    public String estadoSistema() {
        return "Sistema operativo - flujo normal";
    }

    public String avisoGeneral() {
        return "No hay novedades críticas en el sistema SITM-MIO";
    }
}
