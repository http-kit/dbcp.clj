package me.shenfeng.dbcp;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalConnection extends ThreadLocal<Connection> {

    private final String url;
    private final String username;
    private final String password;

    private final Map<WeakReference<Thread>, Connection> connections = new HashMap<WeakReference<Thread>, Connection>();
    private final ReferenceQueue<Thread> queue = new ReferenceQueue<Thread>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public ThreadLocalConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * close all connections
     */
    public void close() {
        SQLException ex = null;
        synchronized (connections) {
            for (Connection c : connections.values()) {
                try {
                    c.close();
                } catch (SQLException e) {
                    ex = e;
                }
            }
            connections.clear();
        }

        if (ex != null) {
            throw new DBCPException("close connection error", ex);
        }
    }

    public String toString() {
        int active = 0;
        List<String> names;
        synchronized (connections) {
            active = connections.size();
            names = new ArrayList<String>(active);
            for (WeakReference<Thread> r : connections.keySet()) {
                Thread t = r.get();
                if (t != null) {
                    names.add(t.getName());
                } else {
                    names.add("null");
                }
            }
        }

        return "opened=" + counter.get() + ", active=" + active + ", threads=" + names;
    }

    private void closeDiedThreadConnection() {
        Reference<? extends Thread> r;
        while ((r = queue.poll()) != null) {
            synchronized (connections) { // does not run often
                Connection c = connections.get(r);
                connections.remove(r);
                try {
                    c.close();
                } catch (SQLException e) {
                    throw new DBCPException("closed died thread connecton", e);
                }
            }
        }
    }

    public Connection get() {
        closeDiedThreadConnection();
        Connection con = super.get();
        try {
            if (con.isClosed()) {
                synchronized (connections) { // remove from connections
                    Thread c = Thread.currentThread();
                    Iterator<Entry<WeakReference<Thread>, Connection>> it = connections
                            .entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<WeakReference<Thread>, Connection> e = it.next();
                        if (e.getKey().get() == c) {
                            it.remove();
                        }
                    }
                }
                remove(); // all recreate one
                return super.get(); // try to create a new one
            }
        } catch (SQLException e) {
            throw new DBCPException("error when asking isClosed ??", e);
        }
        return con;
    }

    protected Connection initialValue() {
        counter.incrementAndGet();
        try {
            Connection con = DriverManager.getConnection(url, username, password);
            Statement stat = con.createStatement();
            ResultSet rs = stat.executeQuery("show variables like 'wait_timeout'");
            if (rs.next()) {
                int timeout = rs.getInt(2);
                if (timeout == 3600 * 8) { //
                    // server will close idle connection, default 8 hours, change to 3 days
                    stat.executeUpdate("set wait_timeout = 259200");
                }
            }
            rs.close();
            stat.close();
            synchronized (connections) {
                connections.put(new WeakReference<Thread>(Thread.currentThread(), queue), con);
            }
            return new NoCloseConnection(con);
        } catch (SQLException e) {
            throw new DBCPException("connect to mysql error", e);
        }
    }

}
