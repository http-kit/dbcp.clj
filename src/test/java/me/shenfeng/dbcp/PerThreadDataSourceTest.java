package me.shenfeng.dbcp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

class Tester implements Runnable {

    DataSource dataSource;
    private int loop;

    public Tester(DataSource dataSource, int loop) {
        this.dataSource = dataSource;
        this.loop = loop;
    }

    public void run() {
        while (loop-- > 0) {
            Connection con = null;
            try {
                con = dataSource.getConnection();

                Statement sta = con.createStatement();
                ResultSet s = sta.executeQuery("select count(*) from user");
                while (s.next()) {
//                    System.out.println(s.getInt(1));
                }
                Thread.sleep(100);
                s.close();
                sta.close();

                PreparedStatement ps = con
                        .prepareStatement("select count(*) from user where user = ?");
                ps.setString(1, "root");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
//                    System.out.println(rs.getInt(1));
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
                // System.out.println(e);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                }
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ignore) {
                    }
                }
            }
        }
    }
}

public class PerThreadDataSourceTest {
    public static final String url = "jdbc:mysql://localhost/mysql";
    public static final String user = "feng";
    public static final String password = "";

    private static final int LOOP = 10;

    @Test
    public void test1() throws InterruptedException, IOException {

        PerThreadDataSource dataSource = new PerThreadDataSource(url, user, password);

        Thread t1 = new Thread(new Tester(dataSource, LOOP));
        Thread t2 = new Thread(new Tester(dataSource, LOOP));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        dataSource.close();
    }

    @Test
    public void testBasicDataSource() throws InterruptedException {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);

        Thread t1 = new Thread(new Tester(dataSource, LOOP));
        Thread t2 = new Thread(new Tester(dataSource, LOOP));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
