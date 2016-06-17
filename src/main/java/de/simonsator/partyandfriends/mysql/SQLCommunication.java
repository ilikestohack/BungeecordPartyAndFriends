package de.simonsator.partyandfriends.mysql;

import java.sql.*;

/**
 * @author simonsator
 * @version 1.0.0 created on 12.06.16
 */
public abstract class SQLCommunication {
	/**
	 * The MySQL database
	 */
	final String database;
	/**
	 * The url of the SQL server
	 */
	private final String url;
	private Connection connection;

	SQLCommunication(String pDatabase, String pURL) {
		this.database = pDatabase;
		this.url = pURL;
		connection = createConnection();
	}

	void close(ResultSet rs, Statement stmt) {
		try {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected Connection getConnection() {
		try {
			if (connection != null && connection.isValid(6))
				return connection;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection = createConnection();
	}

	private Connection createConnection() {
		try {
			closeConnection();
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(url);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void closeConnection() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException ignored) {
		}
	}

}
