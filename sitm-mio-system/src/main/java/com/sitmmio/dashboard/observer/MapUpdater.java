package com.sitmmio.dashboard.observer;

import com.sitmmio.common.model.Bus;
import com.sitmmio.dashboard.websocket.BusWebSocketHandler;

public class MapUpdater implements BusObserver {

    private final BusWebSocketHandler socketHandler;

    public MapUpdater(BusWebSocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public void onBusUpdated(Bus bus) {

        String mensaje =
                "{ \"busId\": \"" + bus.getIdBus() + "\"," +
                "\"lat\": " + bus.getLatitud() + "," +
                "\"lon\": " + bus.getLongitud() + "}";

        socketHandler.enviarUbicacionBus(mensaje);

        System.out.println("Mapa actualizado: " + mensaje);
    }
}
