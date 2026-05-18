package SitmMio;

public class DatagramaICE implements java.lang.Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public String busId     = "";
    public double latitud   = 0.0;
    public double longitud  = 0.0;
    public String timestamp = "";
    public String tipoEvento = "";
    public int    prioridad  = 0;
    public String messageId  = "";
    public String payload    = "";

    public DatagramaICE() {}

    public DatagramaICE(String busId, double latitud, double longitud,
                        String timestamp, String tipoEvento,
                        int prioridad, String messageId, String payload) {
        this.busId      = busId;
        this.latitud    = latitud;
        this.longitud   = longitud;
        this.timestamp  = timestamp;
        this.tipoEvento = tipoEvento;
        this.prioridad  = prioridad;
        this.messageId  = messageId;
        this.payload    = payload;
    }

    @Override
    public DatagramaICE clone() {
        try { return (DatagramaICE) super.clone(); }
        catch (CloneNotSupportedException e) { throw new AssertionError(e); }
    }

    public static void ice_write(com.zeroc.Ice.OutputStream ostr, DatagramaICE v) {
        ostr.writeString(v.busId);
        ostr.writeDouble(v.latitud);
        ostr.writeDouble(v.longitud);
        ostr.writeString(v.timestamp);
        ostr.writeString(v.tipoEvento);
        ostr.writeInt(v.prioridad);
        ostr.writeString(v.messageId);
        ostr.writeString(v.payload);
    }

    public static DatagramaICE ice_read(com.zeroc.Ice.InputStream istr) {
        DatagramaICE v = new DatagramaICE();
        v.busId      = istr.readString();
        v.latitud    = istr.readDouble();
        v.longitud   = istr.readDouble();
        v.timestamp  = istr.readString();
        v.tipoEvento = istr.readString();
        v.prioridad  = istr.readInt();
        v.messageId  = istr.readString();
        v.payload    = istr.readString();
        return v;
    }

    @Override
    public String toString() {
        return "DatagramaICE{busId='" + busId + "', lat=" + latitud
                + ", lon=" + longitud + ", ts='" + timestamp
                + "', tipo='" + tipoEvento + "', prio=" + prioridad + "}";
    }
}
