package database;

import java.sql.*;

public class DatabaseService {
	private final String DATABASE_SERVER_ADDRESS = "vm23.cs.lth.se";
	private final String DATABASE_USER = "pusp2002hbg";
	private final String DATABASE_PASSWORD = ""; // Fill this in, but do NOT commit it!
	private final String DATABASE = "pusp2002hbg";
	private Connection conn;
	
	public DatabaseService() throws SQLException {
		openConnection();
	}
	
	public boolean openConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + DATABASE_SERVER_ADDRESS + "/" + DATABASE, DATABASE_USER, DATABASE_PASSWORD);			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean closeConnection() {
		try {
			conn.close();		
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
