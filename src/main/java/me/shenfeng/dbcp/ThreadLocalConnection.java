package me.shenfeng.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadLocalConnection extends ThreadLocal<Connection> {

    private final String url;
    private final String username;
    private final String password;

    private ConcurrentLinkedQueue<Connection> cons = new ConcurrentLinkedQueue<Connection>();

    public ThreadLocalConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void close() {
        Connection con = null;
        SQLException ex = null;
        while ((con = cons.poll()) != null) {
            try {
                con.close();
            } catch (SQLException e) {
                ex = e;
            }
        }
        if (ex != null) {
            throw new DBCPException("close connection error", ex);
        }
    }

    public List<Connection> getAllConnection() {
        return new ArrayList<Connection>(cons);
    }

    public Connection get() {
        Connection con = super.get();
        try {
            if (con.isClosed()) {
                remove();
                return super.get(); // try to create a new one
            }
        } catch (SQLException e) {
            throw new DBCPException("error when asking isClosed!", e);
        }
        return con;
    }

    protected Connection initialValue() {
        try {
            Connection con = DriverManager.getConnection(url, username, password);
            Statement stat = con.createStatement();
            ResultSet rs = stat.executeQuery("show variables like 'wait_timeout'");
            if (rs.next()) {
                int timeout = rs.getInt(2);
                if (timeout == 3600 * 8) {
                    stat.executeUpdate("set wait_timeout = 259200");
                }
            }
            rs.close();
            stat.close();
            cons.add(con);
            return new NoCloseConnection(con);
        } catch (SQLException e) {
            throw new DBCPException("connect to mysql error", e);
        }
    }

}
