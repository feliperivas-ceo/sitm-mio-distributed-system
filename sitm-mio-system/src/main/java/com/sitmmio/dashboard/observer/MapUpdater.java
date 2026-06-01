package com.sitmmio.dashboard.observer;

import com.sitmmio.common.model.Bus;
import com.sitmmio.repository.BusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class MapUpdater implements BusPositionObserver {

    @Autowired private BusRepository busRepo;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    private final Random rand = new Random();

    @Override
    public void onBusPositionUpdate(String busId, double lat, double lon) {
        busRepo.findById(busId).ifPresent(bus -> {
            bus.setLatitud(lat);
            bus.setLongitud(lon);
            bus.setUltimoTimestamp(LocalDateTime.now());
            busRepo.save(bus);
        });
    }

    /** Actualiza posiciones de buses periódicamente y emite por WebSocket (R4) */
    @Scheduled(fixedRate = 5000)
    public void actualizarPosicionBuses() {
        List<Bus> buses = busRepo.findAll();
        for (Bus bus : buses) {
            if (bus.getLatitud() != null && bus.getLongitud() != null) {
                double deltaLat = (rand.nextDouble() - 0.5) * 0.0005;
                double deltaLon = (rand.nextDouble() - 0.5) * 0.0005;
                bus.setLatitud(bus.getLatitud() + deltaLat);
                bus.setLongitud(bus.getLongitud() + deltaLon);
                bus.setUltimoTimestamp(LocalDateTime.now());
                bus.setVelocidad(15 + rand.nextDouble() * 45);
                busRepo.save(bus);
            }
        }

        List<Map<String, Object>> actualizaciones = buses.stream()
            .filter(b -> b.getLatitud() != null)
            .map(b -> Map.<String, Object>of(
                "id", b.getId(),
                "lat", b.getLatitud(),
                "lon", b.getLongitud(),
                "estado", b.getEstado() != null ? b.getEstado() : "ACTIVO",
                "prioridad", b.getPrioridad() != null ? b.getPrioridad().name() : "BAJA",
                "ultimoEvento", b.getUltimoEvento() != null ? b.getUltimoEvento().name() : "GPS_NORMAL",
                "velocidad", b.getVelocidad() != null ? b.getVelocidad() : 0.0
            ))
            .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/buses", actualizaciones);
    }
}
