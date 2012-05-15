package me.shenfeng.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

	protected Connection initialValue() {
		try {
			int i = id.incrementAndGet();
			logger.info("create connection, {}", i);
			Connection con = DriverManager.getConnection(url, username,
					password);
			cons.add(con);
			return new NoCloseConnection(con);
		} catch (SQLException e) {
			logger.error("create connection", e);
			throw new RuntimeException(e);
		}
	}

}
