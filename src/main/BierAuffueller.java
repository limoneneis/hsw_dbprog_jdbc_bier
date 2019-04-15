package main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BierAuffueller implements Runnable {

	private Connection conn;
	private String name;

	public BierAuffueller(Connection conn, String name) {
		this.conn = conn;
		this.name = name;
	}

	@Override
	public void run() {
		System.out.println("run-methode gestartet\n");

		try {
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			ResultSet rs = stmt.executeQuery("SELECT * FROM Person_Krug");
			DBManager.krugAuffuellen(rs, name);

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		System.out.println("run-methode durchgelaufen\n");
	}
}
