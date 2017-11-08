package org.uab.android.incidencies.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseConnection {
	private static String servername = "158.109.79.13";
	private static String dbname = "reporting";
	private static String username = "ctanas";
	private static String password = "sfaecgsw";
	private static String charEncoding = "?useUnicode=yes&characterEncoding=UTF-8";
	
	private Connection conn = null;
	
	public void connect() throws SQLException, ClassNotFoundException {
		String url = "jdbc:mysql://" + servername + "/" + dbname + charEncoding;
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(url, username, password);
	}
	
	public ResultSet query(String q) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(q);
		return stmt.executeQuery();
	}
	
	public int insert(String u) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(u);
		return stmt.executeUpdate();
	}
	
	public boolean execute(String s) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(s);
		return stmt.execute();
	}
	
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		conn.setAutoCommit(autoCommit);
	}
	
	public void commit() throws SQLException {
		conn.commit();
	}
	
	public void rollback() throws SQLException {
		conn.rollback();
	}
	
	public void close() throws SQLException {
		conn.close();
	}
}