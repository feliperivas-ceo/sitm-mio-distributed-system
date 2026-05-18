package com.sitmmio.bus.ice;

import com.sitmmio.common.model.Datagrama;

public class IceBusClient {

    public void enviarDatagrama(Datagrama datagrama) {
        System.out.println("[PROXY ICE] Enviando datagrama al Servidor Central...");
        System.out.println(datagrama);
        System.out.println("--------------------------------------------------");
    }
}


// TODO: Implementar el cliente Ice para enviar datagramas al servidor central 
// -> esas lineas de arriba es para yo poder probar mientras samuel implemente el resto
