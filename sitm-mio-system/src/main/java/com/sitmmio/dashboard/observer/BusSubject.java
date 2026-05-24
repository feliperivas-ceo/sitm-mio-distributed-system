package com.sitmmio.dashboard.observer;

import com.sitmmio.common.model.Bus;

public interface BusSubject {

    void addObserver(BusObserver observer);

    void removeObserver(BusObserver observer);

    void notifyObservers(Bus bus);

}

