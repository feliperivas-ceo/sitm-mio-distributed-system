package com.sitmmio.server.service;

import com.sitmmio.common.model.Datagrama;
import com.sitmmio.repository.DatagramaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DatagramaService {

    private final DatagramaRepository repo;

    public DatagramaService(DatagramaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Datagrama guardar(Datagrama datagrama) {
        return repo.save(datagrama);
    }

    public List<Datagrama> obtenerPorBus(String busId) {
        return repo.findByIdBus(busId);
    }

    public List<Datagrama> obtenerNoProcesados() {
        return repo.findByProcesadoFalse();
    }
}
