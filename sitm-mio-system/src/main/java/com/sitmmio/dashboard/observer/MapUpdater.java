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

/**
 * Observer responsable de actualizar y transmitir posiciones de buses.
 *
 * En el entorno académico se mantiene una simulación visual para demostrar
 * actualizaciones en tiempo real mediante WebSocket.
 *
 * Los resultados históricos de los CSV se muestran en el panel de analítica,
 * pero no se utilizan directamente para mover los marcadores del mapa.
 */
@Component
public class MapUpdater implements BusPositionObserver {

    @Autowired
    private BusRepository busRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    /**
     * Actualiza un bus cuando llega una posición desde el flujo de datagramas.
     */
    @Override
    public void onBusPositionUpdate(String busId, double lat, double lon) {
        busRepo.findById(busId).ifPresent(bus -> {
            bus.setLatitud(lat);
            bus.setLongitud(lon);
            bus.setUltimoTimestamp(LocalDateTime.now());

            busRepo.save(bus);

            messagingTemplate.convertAndSend(
                    "/topic/buses",
                    List.of(convertirActualizacion(bus))
            );
        });
    }

    /**
     * Movimiento simulado para mantener activa la demostración visual.
     *
     * Se ejecuta cada 5 segundos y transmite las nuevas posiciones por WebSocket.
     */
    @Scheduled(fixedRate = 5000)
    public void actualizarPosicionesSimuladas() {

        List<Bus> buses = busRepo.findAll();

        for (Bus bus : buses) {
            if (bus.getLatitud() == null || bus.getLongitud() == null) {
                continue;
            }

            double deltaLat = (random.nextDouble() - 0.5) * 0.0005;
            double deltaLon = (random.nextDouble() - 0.5) * 0.0005;

            bus.setLatitud(bus.getLatitud() + deltaLat);
            bus.setLongitud(bus.getLongitud() + deltaLon);
            bus.setUltimoTimestamp(LocalDateTime.now());

            if (bus.getVelocidad() == null) {
                bus.setVelocidad(20.0 + random.nextDouble() * 30.0);
            }

            busRepo.save(bus);
        }

        List<Map<String, Object>> actualizaciones = buses.stream()
                .filter(bus ->
                        bus.getLatitud() != null &&
                        bus.getLongitud() != null
                )
                .map(this::convertirActualizacion)
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend(
                "/topic/buses",
                actualizaciones
        );
    }

    /**
     * Convierte la entidad Bus al formato esperado por map.js.
     */
    private Map<String, Object> convertirActualizacion(Bus bus) {
        return Map.of(
                "id", bus.getId(),
                "lat", bus.getLatitud() != null
                        ? bus.getLatitud()
                        : 0.0,
                "lon", bus.getLongitud() != null
                        ? bus.getLongitud()
                        : 0.0,
                "estado", bus.getEstado() != null
                        ? bus.getEstado()
                        : "ACTIVO",
                "prioridad", bus.getPrioridad() != null
                        ? bus.getPrioridad().name()
                        : "BAJA",
                "ultimoEvento", bus.getUltimoEvento() != null
                        ? bus.getUltimoEvento().name()
                        : "POSICION_GPS",
                "velocidad", bus.getVelocidad() != null
                        ? bus.getVelocidad()
                        : 0.0
        );
    }
}