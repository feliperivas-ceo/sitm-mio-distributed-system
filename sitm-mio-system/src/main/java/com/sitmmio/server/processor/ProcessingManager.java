package com.sitmmio.server.processor;

import com.sitmmio.common.model.Datagrama;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Encadena los EventProcessor registrados en Spring (Chain of Responsibility).
 * Cada datagrama pasa por todos los procesadores que lo acepten.
 */
@Component
public class ProcessingManager {

    private static final Logger log = LoggerFactory.getLogger(ProcessingManager.class);

    private final List<EventProcessor> procesadores;

    public ProcessingManager(List<EventProcessor> procesadores) {
        this.procesadores = procesadores;
        log.info("ProcessingManager con {} procesadores: {}",
                procesadores.size(),
                procesadores.stream().map(p -> p.getClass().getSimpleName()).toList());
    }

    public void procesar(Datagrama datagrama) {
        procesadores.stream()
                .filter(p -> p.acepta(datagrama))
                .forEach(p -> {
                    try {
                        String resultado = p.procesar(datagrama);
                        log.debug("{} → {}", p.getClass().getSimpleName(), resultado);
                    } catch (Exception e) {
                        log.error("Error en {}: {}", p.getClass().getSimpleName(), e.getMessage());
                    }
                });
    }
}
