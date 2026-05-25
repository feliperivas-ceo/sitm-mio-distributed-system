package com.sitmmio.dashboard.observer;

import com.sitmmio.common.model.Bus;

public interface BusObserver {

    void onBusUpdated(Bus bus);

}