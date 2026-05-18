package SitmMio;

import java.util.concurrent.CompletionStage;

/**
 * Dispatcher base generado manualmente (equivale a lo que produce slice2java).
 * DatagramaReceiverI lo extiende e implementa las operaciones concretas.
 */
public abstract class _DatagramaReceiverDisp
        implements com.zeroc.Ice.Object, _DatagramaReceiverOperations {

    private static final String[] ICE_IDS = {
        "::Ice::Object",
        "::SitmMio::DatagramaReceiver"
    };

    @Override
    public String[] ice_ids(com.zeroc.Ice.Current current) {
        return ICE_IDS;
    }

    @Override
    public String ice_id(com.zeroc.Ice.Current current) {
        return "::SitmMio::DatagramaReceiver";
    }

    @Override
    public CompletionStage<com.zeroc.Ice.OutputStream> _iceDispatch(
            com.zeroc.IceInternal.Incoming in, com.zeroc.Ice.Current current)
            throws com.zeroc.Ice.UserException {
        switch (current.operation) {
            case "enviarDatagrama":
                return _iceD_enviarDatagrama(this, in, current);
            case "enviarDatagramaCritico":
                return _iceD_enviarDatagramaCritico(this, in, current);
            default:
                throw new com.zeroc.Ice.OperationNotExistException(
                        current.id, current.facet, current.operation);
        }
    }

    // --- dispatchers estáticos ---

    static CompletionStage<com.zeroc.Ice.OutputStream> _iceD_enviarDatagrama(
            _DatagramaReceiverOperations obj,
            com.zeroc.IceInternal.Incoming in,
            com.zeroc.Ice.Current current)
            throws com.zeroc.Ice.UserException {

        com.zeroc.Ice.InputStream istr = in.startReadParams();
        DatagramaICE datagrama = DatagramaICE.ice_read(istr);
        in.endReadParams();

        obj.enviarDatagrama(datagrama, current);
        return in.setResult(in.writeEmptyParams());
    }

    static CompletionStage<com.zeroc.Ice.OutputStream> _iceD_enviarDatagramaCritico(
            _DatagramaReceiverOperations obj,
            com.zeroc.IceInternal.Incoming in,
            com.zeroc.Ice.Current current)
            throws com.zeroc.Ice.UserException {

        com.zeroc.Ice.InputStream istr = in.startReadParams();
        DatagramaICE datagrama = DatagramaICE.ice_read(istr);
        in.endReadParams();

        AckICE ret = obj.enviarDatagramaCritico(datagrama, current);

        com.zeroc.Ice.OutputStream ostr = in.startWriteParams();
        AckICE.ice_write(ostr, ret);
        in.endWriteParams(ostr);
        return in.setResult(ostr);
    }
}
