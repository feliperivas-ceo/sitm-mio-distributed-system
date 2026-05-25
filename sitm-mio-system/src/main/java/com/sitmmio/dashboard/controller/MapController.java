package com.sitmmio.dashboard.controller;

import com.sitmmio.common.model.Bus;
import com.sitmmio.dashboard.observer.BusObserver;
import com.sitmmio.dashboard.observer.BusSubject;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MapController implements BusSubject {

    private final List<BusObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(BusObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(BusObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Bus bus) {

        for (BusObserver observer : observers) {
            observer.onBusUpdated(bus);
        }
    }

    public void actualizarBus(Bus bus) {

        System.out.println("Bus actualizado: " + bus.getIdBus());

        notifyObservers(bus);
    }
}