package main;

import java.sql.*;

public class DBManager {

	private static final String dbName = "jdbc_test";
	private static final String username = "testuser";
	private static final String password = "test1234";
	private static final String url = "jdbc:mysql://localhost:3306/" + dbName;

	private static Connection conn;

	public static void printResult(ResultSet resultSet) {
		try {
			ResultSetMetaData metaData = resultSet.getMetaData();
			int[] widths = new int[metaData.getColumnCount() + 1];
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				System.out.print(" | " + metaData.getColumnName(i));
				widths[i] = metaData.getColumnName(i).length();
			}
			while(resultSet.next()) {
				System.out.println();
				for (int i = 1; i <= columnCount; i++) {
					String result = resultSet.getString(i);
					if(result == null)
						result = "";
					if(result.length() > widths[i]) {
						result = result.substring(0, widths[i] - 3) + "...";
					} else if (result.length() < widths[i]) {
						String test = new String(new char[widths[i] - result.length()]);
						result += test.replaceAll(""+((char) 0), " ");
					}
					System.out.print(" | " + result);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void krugAuffuellen(ResultSet resultSet, String name) {
		try {
			while (resultSet.next()) {
				if(resultSet.getString(2).equals(name)) {
					int diff = resultSet.getInt(4) - resultSet.getInt(3);
					System.out.println("Krug von " + name + " wird um " + diff + "ml aufgefüllt.\n");
					resultSet.updateInt(3, resultSet.getInt(3) + diff);
					resultSet.updateRow();
					System.out.println("Der Krug von " + name + " enthält jetzt wieder " + resultSet.getInt(3) + "ml.\n");
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String createTableSQLQuery =
				"CREATE TABLE IF NOT EXISTS " + dbName +
				".Person_Krug " +
				"(ID int NOT NULL AUTO_INCREMENT, " +
				"Name varchar(40) NOT NULL, " +
				"Inhalt int NOT NULL, " +
				"MaxInhalt int NOT NULL, " +
				"PRIMARY KEY (ID))";

		String dropTableSQLQuery =
				"DROP TABLE IF EXISTS " + dbName + ".Person_Krug";

		String selectQuery =
				"SELECT * FROM Person_Krug";

		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException ex) {
			System.out.println("Failed to create the database connection.");
			ex.printStackTrace();
		}

		Statement stmt = null;

		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			// CLEANUP
			stmt.execute(dropTableSQLQuery);

			// DEFINE TABLE
			stmt.execute(createTableSQLQuery);

			// FILL TABLE
			stmt.executeUpdate("INSERT INTO Person_Krug (Name, Inhalt, MaxInhalt) VALUES('Hans', 200, 500)");
			stmt.executeUpdate("INSERT INTO Person_Krug (Name, Inhalt, MaxInhalt) VALUES('Peter', 150, 500)");
			stmt.executeUpdate("INSERT INTO Person_Krug (Name, Inhalt, MaxInhalt) VALUES('Anna', 0, 1000)");
			stmt.executeUpdate("INSERT INTO Person_Krug (Name, Inhalt, MaxInhalt) VALUES('Felix', 450, 500)");

			// VORHER
			ResultSet rs = stmt.executeQuery(selectQuery);
			printResult(rs);
			System.out.println("\n");

			// KRUG AUFFÜLLEN
			try {
				Connection conn2 = DriverManager.getConnection(url, username, password);
				Connection conn3 = DriverManager.getConnection(url, username, password);
				new Thread(new BierAuffueller(conn2, "Peter")).start();
				new Thread(new BierAuffueller(conn3, "Peter")).start();
			} catch (SQLException ex) {
				System.out.println("Failed to create the database connection.");
				ex.printStackTrace();
			}

			// NACHHER

			Thread.sleep(100);
			rs = stmt.executeQuery(selectQuery);
			printResult(rs);
			System.out.println("\n");

		} catch(SQLException sqle) {
			System.out.println("Could not execute query.");
			sqle.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqle) {
					System.out.println("Could not close statement.");
					sqle.printStackTrace();
				}
			}
		}
	}
}
