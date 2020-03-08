package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
	
	public List<ActivitySubType> getActivitySubTypes() throws SQLException {
		List<ActivitySubType> activitySubTypes = new ArrayList<>();
		
		String sql = "SELECT * FROM ActivitySubTypes";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
	    	activitySubTypes.add(new ActivitySubType(
	    		rs.getInt("activitySubTypeId"),
	    		rs.getInt("activityTypeId"),
	    		rs.getString("subType")
			));
	    }		
		
		ps.close();
		return activitySubTypes;
	}
	
	public List<ActivityType> getActivityTypes() throws SQLException{
		PreparedStatement ps = null;
		List<ActivityType> activityTypes = new ArrayList<>();
		String sql = "SELECT * FROM ActivityTypes";
		
		return activityTypes;
	}
}
