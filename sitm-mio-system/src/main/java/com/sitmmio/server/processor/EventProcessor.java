package com.sitmmio.server.processor;

import com.sitmmio.common.model.Datagrama;

/**
 * Contrato base para los procesadores especializados de eventos.
 */
public interface EventProcessor {

    /** Retorna true si este procesador maneja el tipo de datagrama. */
    boolean acepta(Datagrama datagrama);

    /** Procesa el datagrama y retorna descripción del resultado. */
    String procesar(Datagrama datagrama);
}
