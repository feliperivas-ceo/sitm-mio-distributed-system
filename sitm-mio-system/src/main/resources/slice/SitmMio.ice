module SitmMio {
    struct DatagramaDTO {
        string idBus;
        string idRuta;
        double latitud;
        double longitud;
        string timestamp;
        string tipoEvento;
        string prioridad;
        string estadoPuertas;
        string descripcion;
    };

    interface DatagramaReceiver {
        void recibirDatagrama(DatagramaDTO datagrama);
    };
};