package com.sitmmio.server.service;

import com.sitmmio.common.model.Evento;
import com.sitmmio.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repo;

    public EventoService(EventoRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Evento registrar(Evento evento) {
        return repo.save(evento);
    }

    public List<Evento> obtenerPorBus(String busId) {
        return repo.findByBusId(busId);
    }

    public List<Evento> obtenerNoAcknowledged() {
        return repo.findByAckEnviadoFalse();
    }
}
