package me.shenfeng.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCReconnectTest {

    public static String mysqlSafe(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws SQLException {

        // String str = "\uD83D\uDC4A\u1f44a";

        String str = "abc\uD83D\uDC4Adefg";

        System.out.println(mysqlSafe(str));

        Connection con = DriverManager.getConnection(Constants.URL, Constants.USER,
                Constants.PASS);

        PreparedStatement ps = con.prepareStatement("insert into tt (c) values (?)");

        ps.setString(1, str);
        ps.executeUpdate();

        System.out.println(str + "\t" + str.length());
    }
}
