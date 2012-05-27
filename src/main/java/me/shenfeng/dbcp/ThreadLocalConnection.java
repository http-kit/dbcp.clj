package me.shenfeng.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalConnection extends ThreadLocal<Connection> {

    private final String url;
    private final String username;
    private final String password;

    private final static Logger logger = LoggerFactory
            .getLogger(ThreadLocalConnection.class);

    private ConcurrentLinkedQueue<Connection> cons = new ConcurrentLinkedQueue<Connection>();
    private AtomicInteger id = new AtomicInteger(0);

    public ThreadLocalConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void close() {
        logger.info("close all connections");
        Connection con = null;
        while ((con = cons.poll()) != null) {
            try {
                con.close();
            } catch (SQLException e) {
                logger.error("close all connections", e);
            }
        }
    }

    public Connection get() {
        Connection con = super.get();
        try {
            if (con.isClosed()) {
                logger.trace("a connection is closed, try to create a new one");
                remove();
                return super.get(); // try to create a new one
            }
        } catch (SQLException e) {
            logger.error("error when asking isClosed!", e);
        }
        return con;
    }

    protected Connection initialValue() {
        try {
            int i = id.incrementAndGet();
            logger.trace("create connection " + i);
            Connection con = DriverManager.getConnection(url, username,
                    password);
            Statement stat = con.createStatement();
            ResultSet rs = stat
                    .executeQuery("show variables like 'wait_timeout'");
            if (rs.next()) {
                int timeout = rs.getInt(2);
                if (timeout == 3600 * 8) {
                    logger.debug("change default wait_timeout from 8 hours to 3 days");
                    stat.executeUpdate("set wait_timeout = 259200");
                }
            }
            rs.close();
            stat.close();

            cons.add(con);
            return new NoCloseConnection(con);
        } catch (SQLException e) {
            logger.error("create connection", e);
            throw new RuntimeException(e);
        }
    }

}
