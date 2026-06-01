package com.sitmmio.dashboard.service;

import com.sitmmio.common.model.*;
import com.sitmmio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ControladorOperacionService {

    @Autowired private BusRepository busRepo;
    @Autowired private EstacionRepository estacionRepo;
    @Autowired private ZonaRepository zonaRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    public Map<String, Object> getZonaInfo(Long zonaId) {
        Zona zona = zonaRepo.findById(zonaId).orElseThrow();
        List<Usuario> controladores = usuarioRepo.findByZonaId(zonaId);
        List<Bus> buses = busRepo.findAll().stream()
            .filter(b -> b.getRuta() != null)
            .toList();
        List<Estacion> estaciones = estacionRepo.findAll();

        return Map.of(
            "zona", zona,
            "controladores", controladores,
            "buses", buses,
            "estaciones", estaciones
        );
    }
}
