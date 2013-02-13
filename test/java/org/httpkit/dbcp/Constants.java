package org.httpkit.dbcp;

import org.httpkit.dbcp.PerThreadDataSource;

public class Constants {

    public static final String URL = "jdbc:mysql://localhost/test?maintainTimeStats=false";
    public static final String USER = "root";
    public static final String PASS = "";

    public static PerThreadDataSource newDS() {
        return new PerThreadDataSource(URL, USER, PASS);
    }
}
