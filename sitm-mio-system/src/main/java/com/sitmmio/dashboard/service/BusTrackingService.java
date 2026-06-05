package com.sitmmio.dashboard.service;

import com.sitmmio.common.model.Bus;
import com.sitmmio.common.model.Datagrama;
import com.sitmmio.repository.BusRepository;
import com.sitmmio.repository.DatagramaRepository;
import com.sitmmio.dashboard.controller.MapController;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusTrackingService {

    private final DatagramaRepository datagramaRepository;
    private final BusRepository busRepository;
    private final MapController mapController;

    public BusTrackingService(DatagramaRepository datagramaRepository,
                              BusRepository busRepository,
                              MapController mapController) {
        this.datagramaRepository = datagramaRepository;
        this.busRepository = busRepository;
        this.mapController = mapController;
    }

    @Scheduled(fixedRate = 5000)
    public void actualizarMapaTiempoReal() {

        List<Datagrama> datagramas =
                datagramaRepository.findByProcesadoFalse();

        for (Datagrama d : datagramas) {

            Bus bus = busRepository.findById(d.getIdBus())
                    .orElse(new Bus());

            bus.setIdBus(d.getIdBus());

            bus.setLatitud(d.getLatitud());

            bus.setLongitud(d.getLongitud());

            bus.setUltimaActualizacion(LocalDateTime.now());

            bus.setEstado(Bus.EstadoBus.ACTIVO);

            busRepository.save(bus);

            mapController.actualizarBus(bus);

            d.setProcesado(true);
            datagramaRepository.save(d);

            System.out.println("GPS actualizado -> " + bus.getIdBus());
        }
    }

    /**
 * Retransmite cada 5 segundos las posiciones almacenadas.
 * No inventa movimiento: los cambios reales provienen de datagramas.
 */
@Scheduled(fixedRate = 5000)
public void transmitirPosicionesActuales() {
    mapController.broadcastAllBuses();
}
}

