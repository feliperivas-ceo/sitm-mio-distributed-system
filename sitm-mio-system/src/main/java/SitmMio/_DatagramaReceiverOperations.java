package SitmMio;

public interface _DatagramaReceiverOperations {
    void   enviarDatagrama(DatagramaICE datagrama, com.zeroc.Ice.Current current);
    AckICE enviarDatagramaCritico(DatagramaICE datagrama, com.zeroc.Ice.Current current);
}
