package com.sitmmio.server.ice;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configura y arranca el servidor ICE.
 * Llamado desde CentralServer en @PostConstruct.
 */
public class IceServerConfig {

    private static final Logger log = LoggerFactory.getLogger(IceServerConfig.class);

    private static final String ADAPTER_NAME = "DatagramaReceiverAdapter";
    private static final String IDENTITY     = "DatagramaReceiver";

    private Communicator  communicator;
    private ObjectAdapter adapter;

    public void iniciar(DatagramaReceiverI servant, int puerto) {
        String[] args = {};
        communicator = Util.initialize(args);

        String endpoint = "tcp -p " + puerto;
        adapter = communicator.createObjectAdapterWithEndpoints(ADAPTER_NAME, endpoint);
        adapter.add(servant, Util.stringToIdentity(IDENTITY));
        adapter.activate();

        log.info("Servidor ICE activo en puerto {} — identity='{}'", puerto, IDENTITY);
    }

    public void detener() {
        if (adapter != null)     { adapter.destroy(); }
        if (communicator != null) { communicator.destroy(); }
        log.info("Servidor ICE detenido.");
    }

    /** Bloquea el hilo llamador hasta que el communicator sea destruido. */
    public void waitForShutdown() {
        if (communicator != null) communicator.waitForShutdown();
    }
}
