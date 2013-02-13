package org.httpkit.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

public class NoCloseConnection extends DelegateConnection {

    public NoCloseConnection(Connection con) {
        super(con);
    }

    public void close() throws SQLException {
        // noop
    }
}
