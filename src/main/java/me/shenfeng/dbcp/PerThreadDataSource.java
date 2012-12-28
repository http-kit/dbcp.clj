package me.shenfeng.dbcp;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PerThreadDataSource implements DataSource, Closeable {

    static {
        DriverManager.getDrivers();
    }
    private final ThreadLocalConnection connection;

    public PerThreadDataSource(String url, String username, String password) {
        connection = new ThreadLocalConnection(url, username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    public void setLoginTimeout(int seconds) throws SQLException {

    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public String toString() {
        return "PerThreadDataSource[" + connection.toString() + "]";
    }

    public Connection getConnection() throws SQLException {
        return connection.get();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new NotImplementedException();
    }

    public void close() throws IOException {
        connection.close();
    }
}
