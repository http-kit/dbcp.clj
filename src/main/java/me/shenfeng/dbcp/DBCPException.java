package me.shenfeng.dbcp;

public class DBCPException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DBCPException(String mesg, Throwable cause) {
        super(mesg, cause);
    }

    public DBCPException(String mesg) {
        super(mesg);
    }
}
