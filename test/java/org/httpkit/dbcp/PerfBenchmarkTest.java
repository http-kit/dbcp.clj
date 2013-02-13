package org.httpkit.dbcp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.httpkit.dbcp.PerThreadDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PerfBenchmarkTest {

    PerThreadDataSource dataSource;
    AtomicInteger total;
    final int threadCount = 10;
    private String name;
    long start;
    CountDownLatch latch;

    @Before
    public void setup() {
        total = new AtomicInteger(200 * 1000);
        dataSource = Constants.newDS();
        start = System.currentTimeMillis();
        latch = new CountDownLatch(threadCount);
    }

    @After
    public void tearDown() throws IOException {
        long end = System.currentTimeMillis();
        System.out.println(name + " takes time " + (end - start) + "ms");
        dataSource.close();
    }

    // 366 ms
    @Test
    public void testGetConnectonBench() throws InterruptedException {
        name = threadCount + " threads, " + total.get() + " times, getConnection => close";
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
    }

    // 9616ms
    @Test
    public void testStatementBench() throws InterruptedException {
        name = threadCount + " threads, " + total.get()
                + " times, getConnection => Statement(select 1) => close";

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
        latch.await(); // wait threads finish job
    }

    // 10242ms
    @Test
    public void testPrepareStatementBench() throws InterruptedException {
        name = threadCount + " threads, " + total.get()
                + " times, getConnection => PreparedStatement(select 1) => close";

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
    }
}
