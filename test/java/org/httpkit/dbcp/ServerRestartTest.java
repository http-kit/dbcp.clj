package org.httpkit.dbcp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.httpkit.dbcp.PerThreadDataSource;

class Task implements Runnable {

    final PerThreadDataSource ds;

    public Task(PerThreadDataSource ds) {
        this.ds = ds;
    }

    public void run() {
        try {
            Connection con = ds.getConnection();

            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery("select * from mysql.user");
            while (rs.next()) {
                String s = rs.getString(1);
                // System.out.println(s);
            }
        } catch (SQLException e) {
            System.out.println(ds + "\t" + e);
        }

    }

}

public class ServerRestartTest {

    public static void main(String[] args) throws InterruptedException, IOException {

        final int threads = 10;

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        PerThreadDataSource ds = Constants.newDS();

        new Task(ds).run();

        for (int i = 0; i < threads * 100; i++) {
            pool.submit(new Task(ds));
        }

        Thread.sleep(1000);
        System.out.println(ds);

        // wait for mysql restart
        System.in.read();

        new Task(ds).run();
        new Task(ds).run();
        Thread.sleep(100);

        for (int i = 0; i < threads * 100; i++) {
            pool.submit(new Task(ds));
        }

        pool.shutdown();
        pool.awaitTermination(100, TimeUnit.SECONDS);
        ds.close();
        System.out.println(ds);

    }
}
