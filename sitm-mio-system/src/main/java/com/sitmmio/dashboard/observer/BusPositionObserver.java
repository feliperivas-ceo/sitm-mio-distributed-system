package com.sitmmio.dashboard.observer;

public interface BusPositionObserver {
    void onBusPositionUpdate(String busId, double lat, double lon);
}
