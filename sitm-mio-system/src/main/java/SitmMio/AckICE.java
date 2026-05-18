package SitmMio;

public class AckICE implements java.lang.Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public boolean success   = false;
    public String  messageId = "";
    public String  mensaje   = "";

    public AckICE() {}

    public AckICE(boolean success, String messageId, String mensaje) {
        this.success   = success;
        this.messageId = messageId;
        this.mensaje   = mensaje;
    }

    @Override
    public AckICE clone() {
        try { return (AckICE) super.clone(); }
        catch (CloneNotSupportedException e) { throw new AssertionError(e); }
    }

    public static void ice_write(com.zeroc.Ice.OutputStream ostr, AckICE v) {
        ostr.writeBool(v.success);
        ostr.writeString(v.messageId);
        ostr.writeString(v.mensaje);
    }

    public static AckICE ice_read(com.zeroc.Ice.InputStream istr) {
        AckICE v = new AckICE();
        v.success   = istr.readBool();
        v.messageId = istr.readString();
        v.mensaje   = istr.readString();
        return v;
    }
}
