package me.shenfeng.dbcp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CloseTest {

    PerThreadDataSource dataSource;

    @Before
    public void setup() {
        dataSource = new PerThreadDataSource("jdbc:mysql://localhost/test", "feng", "");
    }

    @After
    public void tearDown() throws IOException {
        dataSource.close();
    }

    @Test
    public void testDiredThread() throws InterruptedException, SQLException, IOException {
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 20; i++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        Connection con = dataSource.getConnection();
                        con.getMetaData().getDatabaseMajorVersion();
                    } catch (SQLException e) {
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        threads.clear();
        threads = null;

        for (int i = 0; i < 10; i++) {
            System.out.println(dataSource);
            System.gc();
            Thread.sleep(100);
            dataSource.getConnection();
        }
        dataSource.close();
        System.out.println("after close: " + dataSource);
    }

}
