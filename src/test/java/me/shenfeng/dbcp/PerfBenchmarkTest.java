package me.shenfeng.dbcp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PerfBenchmarkTest {

    PerThreadDataSource dataSource;
    AtomicInteger total;

    @Before
    public void setup() {
        total = new AtomicInteger(100 * 10000); // 1000k
        dataSource = new PerThreadDataSource("jdbc:mysql://localhost/test", "feng", "");
    }

    @After
    public void tearDown() throws IOException {
        dataSource.close();
    }

    // 366 ms
    @Test
    public void testGetConnectonBench() throws InterruptedException {

        long start = System.currentTimeMillis();

        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int j = 0; j < threadCount; j++) {
            new Thread(new Runnable() {
                public void run() {
                    while (total.decrementAndGet() > 0) {
                        try {
                            Connection con = dataSource.getConnection();
                            con.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        System.out.println("10 threads, 1000k get connection takes time "
                + (System.currentTimeMillis() - start) + "ms");
    }

    // 9616ms
    @Test
    public void testStatementBench() throws InterruptedException {
        long start = System.currentTimeMillis();

        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int j = 0; j < threadCount; j++) {
            new Thread(new Runnable() {
                public void run() {
                    while (total.decrementAndGet() > 0) {
                        try {
                            Connection con = dataSource.getConnection();

                            Statement stat = con.createStatement();
                            ResultSet rs = stat.executeQuery("select 1");
                            rs.close();
                            stat.close();
                            con.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        System.out.println("10 threads, 1000k get connection then exec statement takes time "
                + (System.currentTimeMillis() - start) + "ms");
    }

    // 10242ms
    @Test
    public void testPrepareStatementBench() throws InterruptedException {

        long start = System.currentTimeMillis();

        int threadCount = 10;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int j = 0; j < threadCount; j++) {
            new Thread(new Runnable() {
                public void run() {
                    while (total.decrementAndGet() > 0) {
                        try {
                            Connection con = dataSource.getConnection();
                            PreparedStatement ps = con.prepareStatement("select 1");
                            ResultSet rs = ps.executeQuery();
                            rs.close();
                            ps.close();
                            con.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        System.out.println("10 threads, 1000k get connection then prepareStatment takes time "
                + (System.currentTimeMillis() - start) + "ms");
    }
}
