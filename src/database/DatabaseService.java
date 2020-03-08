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
	
	private Role mapRole(ResultSet rs) throws SQLException {
		return new Role(
				rs.getInt("roleId"),
				rs.getString("role")
				);
	}
	
	private ActivityType mapActivityType(ResultSet rs) throws SQLException {
		return new ActivityType(
				rs.getInt("activityTypeId"),
				rs.getString("type")
				);
	}
	
	public List<Project> getAllProjects() throws SQLException {
		List<Project> projects = new ArrayList<>();
		
		String sql = "SELECT * FROM Projects";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			projects.add(new Project(
	    		rs.getInt("projectId"),
	    		rs.getString("name")
			));
	    }		
		
		ps.close();
		return projects;
	}
	
	public List<Project> getAllProjects(int userId) throws SQLException {
		List<Project> projects = new ArrayList<>();
		
		String sql = "SELECT Projects.* " + 
				"FROM Projects JOIN ProjectUsers USING (projectId) " + 
				"WHERE userId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, userId);
		
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			projects.add(new Project(
	    		rs.getInt("projectId"),
	    		rs.getString("name")
			));
	    }		
		
		ps.close();
		return projects;
	}
	
	public Project getProject(int projectId) throws SQLException {
		Project project = null;
		
		String sql = "SELECT * FROM Projects WHERE projectId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, projectId);
		
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			project = new Project(
	    		rs.getInt("projectId"),
	    		rs.getString("name")
			);
	    }		
		
		ps.close();
		return project;
	}
	
	public Project createProject(Project project) throws SQLException {
		String sql = "INSERT INTO Projects (`name`) values (?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, project.getName());
		
		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();
		
		Project p = null;
		if(rs.next()) {
			p = new Project(rs.getInt(1), project.getName());
		}
		
		ps.close();
		return project;
	}
	
	public void deleteProject(int projectId) throws Exception {
		String sql = "DELETE FROM Projects WHERE projectId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, projectId);
		
		int deleted = ps.executeUpdate();		
	
		ps.close();
		
		if (deleted == 0) {
			throw new Exception("Project does not exist");
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
	
	public List<ActivitySubType> getActivitySubTypes(int activityTypeId) throws SQLException {
		List<ActivitySubType> activitySubTypes = new ArrayList<>();
		
		String sql = "SELECT * FROM ActivitySubTypes WHERE activityTypeId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, activityTypeId);
		
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
	
	public List<ActivityType> getActivityTypes() throws SQLException {		
		List<ActivityType> activityTypes = new ArrayList<>();
		
		String sql = "SELECT * FROM ActivityTypes";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			activityTypes.add(mapActivityType(rs));
		}
		
		ps.close();
		return activityTypes;
	}
	
	public List<Role> getAllRoles() throws SQLException {
		List<Role> allRoles = new ArrayList<>();
		
		String sql = "SELECT * FROM Roles";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			allRoles.add(mapRole(rs));
		}
		
		ps.close();
		return allRoles;
	}
	
	public Role getRole(int userId, int projectId) throws SQLException {
		Role role = null;
		String sql = "SELECT Roles.roleId, role FROM Roles, ProjectUsers, WHERE (userId, projectId) = (?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, userId);
		ps.setInt(2, projectId);
		
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
	    	role = mapRole(rs);
		}
		
		ps.close();
		return role;	    	
	}
}
