package database;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This abstracts and manages all communication with the database. All returned
 * rows are represented with models to make for a easy dev experience.
 * 
 * @author OliverEkberg and JesperAnnefors
 * @version 0.1
 * @since 2020-03-14
 */
public class DatabaseService {
	private final String DATABASE_SERVER_ADDRESS = "vm23.cs.lth.se";
	private final String DATABASE_USER = "pusp2002hbg";
	private final String DATABASE_PASSWORD = ""; // Fill this in, but do NOT commit it!
	private final String DATABASE = "pusp2002hbg";
	private Connection conn;

	public DatabaseService() throws SQLException {
		openConnection();
	}

	/**
	 * Opens the connection to the database
	 * 
	 * @return whether it was successful or not
	 */
	public boolean openConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + DATABASE_SERVER_ADDRESS + "/" + DATABASE,
					DATABASE_USER, DATABASE_PASSWORD);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Closes the connection to the database
	 * 
	 * @return whether it was successful or not
	 */
	public boolean closeConnection() {
		try {
			conn.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Maps a ResultSet to a Role model
	 * 
	 * @param rs The result from the SQL query
	 * @return The Role model
	 * @throws SQLException if anything goes wrong
	 */
	private Role mapRole(ResultSet rs) throws SQLException {
		return new Role(rs.getInt("roleId"), rs.getString("role"));
	}

	/**
	 * Maps a ResultSet to an ActivityType model
	 * 
	 * @param rs The result from the SQL query
	 * @return The ActivityType model
	 * @throws SQLException if anything goes wrong
	 */
	private ActivityType mapActivityType(ResultSet rs) throws SQLException {
		return new ActivityType(rs.getInt("activityTypeId"), rs.getString("type"));
	}

	/**
	 * Maps a ResultSet to an ActivitySubType model
	 * 
	 * @param rs The result from the SQL query
	 * @return The ActivitySubType model
	 * @throws SQLException if anything goes wrong
	 */
	private ActivitySubType mapActivitySubType(ResultSet rs) throws SQLException {
		return new ActivitySubType(rs.getInt("activitySubTypeId"), rs.getInt("activityTypeId"),
				rs.getString("subType"));
	}
	
	/**
	 * Maps a ResultSet to an ActivityReport model
	 * 
	 * @param rs The result from the SQL query
	 * @return The ActivityReport model
	 * @throws SQLException if anything goes wrong
	 */
	private ActivityReport mapActivityReport(ResultSet rs) throws SQLException {
		return new ActivityReport(rs.getInt("activityReportId"), rs.getInt("activityTypeId"),
				rs.getInt("activitySubTypeId"), rs.getInt("timeReportId"), rs.getDate("reportDate").toLocalDate(),
				rs.getInt("minutes"));
	}

	/**
	 * Maps a ResultSet to an User model
	 * 
	 * @param rs The result from the SQL query
	 * @return The User model
	 * @throws SQLException if anything goes wrong
	 */
	private User mapUser(ResultSet rs) throws SQLException {
		return new User(rs.getInt("userId"), rs.getString("username"), rs.getString("password"),
				rs.getBoolean("isAdmin"));
	}

	/**
	 * Maps a ResultSet to a Project model
	 * 
	 * @param rs The result from the SQL query
	 * @return The Project model
	 * @throws SQLException if anything goes wrong
	 */
	private Project mapProject(ResultSet rs) throws SQLException {
		return new Project(rs.getInt("projectId"), rs.getString("name"));
	}

	/**
	 * Maps a ResultSet to a TimeReport model
	 * 
	 * @param rs The result from the SQL query
	 * @return The TimeReport model
	 * @throws SQLException if anything goes wrong
	 */
	private TimeReport mapTimeReport(ResultSet rs) throws SQLException {
		Timestamp signedAt = rs.getTimestamp("signedAt");

		return new TimeReport(rs.getInt("timeReportId"), rs.getInt("projectUserId"), rs.getInt("signedById"),
				signedAt == null ? null : signedAt.toLocalDateTime(), rs.getInt("year"), rs.getInt("week"),
				rs.getTimestamp("updatedAt").toLocalDateTime(), rs.getBoolean("finished"));
	}

	/**
	 * Gets user by credentials. Will return null if user could not be found
	 * 
	 * @param username The username of the user
	 * @param password The password of the user
	 * @return User model or null
	 */
	public User getUserByCredentials(String username, String password) {
		User user = null;

		String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				user = mapUser(rs);
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * Gets user by userId. Will return null if user could not be found
	 * 
	 * @param userId The unique identifier of the user
	 * @return User model or null
	 */
	public User getUserById(int userId) {
		User user = null;

		String sql = "SELECT * FROM Users WHERE userId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				user = mapUser(rs);
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	/**
	 * Gets user associated with given timeReportId.
	 * @param timeReportId The unique identifier of the timeReport
	 * @return User model or null
	 */
	public User getUserByTimeReportId(int timeReportId) {
		User user = null;

		String sql = "SELECT Users.* " + 
				"FROM TimeReports " + 
				"JOIN ProjectUsers USING (projectUserId) " + 
				"JOIN  Users USING (userId) " + 
				"WHERE timeReportId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, timeReportId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				user = mapUser(rs);
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * Gets all users
	 * 
	 * @return A list of all users in the database
	 * @throws SQLException if anything goes wrong
	 */
	public List<User> getAllUsers() throws SQLException {
		List<User> users = new ArrayList<>();

		String sql = "SELECT * FROM Users";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				users.add(mapUser(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}

	/**
	 * Gets all users participating in the given project
	 * 
	 * @param projectId The unique identifier of the project to find users for
	 * @return A list of all users participating in the project
	 */
	public List<User> getAllUsers(int projectId) {
		List<User> users = new ArrayList<>();

		String sql = "SELECT Users.* FROM " + "Users JOIN ProjectUsers USING (userId) " + "WHERE projectId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				users.add(mapUser(rs));
			}

			ps.close();
		} catch  (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}

	/**
	 * Deletes a user by given id
	 * 
	 * @param userId The unique identifier of the user to delete
	 */
	public void deleteUserById(int userId) {
		String sql = "DELETE FROM Users WHERE userId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a user by given username. Will throw if user does not exist
	 * 
	 * @param username The unique username of the user to delete
	 */
	public void deleteUserByUsername(String username) {
		String sql = "DELETE FROM Users WHERE username = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates all fields to the provided values. User must already exist for this
	 * to work
	 * 
	 * @param user The user and its values to update
	 * @return The updated user
	 */
	public User updateUser(User user) {
		String sql = "UPDATE Users " + "SET `username` = ?, `password` = ?, `isAdmin` = ? " + "WHERE userId = ?";
		int updated =  0;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPassword());
			ps.setBoolean(3, user.isAdmin());
			ps.setInt(4, user.getUserId());

			updated = ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return updated == 0 ? user : getUserById(user.getUserId());
	}

	/**
	 * Creates a new user
	 * 
	 * @param user The user model to insert
	 * @return the created user model
	 * @throws SQLException if anything goes wrong
	 */
	public User createUser(User user) throws SQLException {
		String sql = "INSERT INTO Users (`username`, `password`, `isAdmin`) values (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, user.getUsername());
		ps.setString(2, user.getPassword());
		ps.setBoolean(3, user.isAdmin());

		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();

		User u = null;
		if (rs.next()) {
			u = getUserById(rs.getInt(1));
		}

		ps.close();
		return u;
	}

	/**
	 * Adds user to project with given role
	 * 
	 * @param userId Unique identifier of user to add
	 * @param projectId Unique identifier of which project to add the user
	 * @param roleId Unique identifier of the role the user should have
	 * @throws Exception If user could not be added
	 */
	public void addUserToProject(int userId, int projectId, int roleId) throws Exception {
		String sql = "INSERT INTO ProjectUsers (`userId`, `projectId`, `roleId`) values (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, userId);
		ps.setInt(2, projectId);
		ps.setInt(3, roleId);

		int added = ps.executeUpdate();

		if (added == 0) {
			throw new Exception("Could not add user to project");
		}
	}

	/**
	 * Removes user from project
	 * 
	 * @param userId Unique identifier of the user to remove
	 * @param projectId Unique identifier of the the project to which the user
	 *                  should be removed from
	 */
	public void removeUserFromProject(int userId, int projectId) {
		String sql = "DELETE FROM ProjectUsers WHERE userId = ? AND projectId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setInt(2, projectId);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change role for a user in a project
	 * 
	 * @param userId Unique identifier of the user for which to change role
	 * @param projectId Unique identifier of the particular project
	 * @param roleId Unique identifier of the new role
	 * @throws Exception If the role could not be changed
	 */
	public void updateUserProjectRole(int userId, int projectId, int roleId) throws Exception {
		String sql = "UPDATE ProjectUsers " + "SET roleId = ? " + "WHERE userId = ? AND projectId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, roleId);
		ps.setInt(2, userId);
		ps.setInt(3, projectId);

		int updated = ps.executeUpdate();

		ps.close();

		if (updated == 0) {
			throw new Exception("Could not update user's role in project");
		}
	}
	
	/**
	 * Gets projectUserId for for given userId in given project. Will throw if user does not exist in project
	 * 
	 * @param userId Unique identifier of user
	 * @param projectId Unique identifier of project
	 * @return projectUserId
	 * @throws Exception If user can not be found in project
	 */
	public int getProjectUserIdByUserIdAndProjectId(int userId, int projectId) throws Exception {
		String sql = "SELECT projectUserId FROM ProjectUsers WHERE (userId, projectId) = (?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, userId);
		ps.setInt(2, projectId);

		ResultSet rs = ps.executeQuery();

		
		int projectUserId = rs.next() ? rs.getInt("projectUserId") : 0;
		ps.close();
		
		if (projectUserId == 0) {
			throw new Exception("Could not find given user in given project");
		}
		
		return projectUserId;
	}

	/**
	 * Gets all projects
	 * 
	 * @return A list of project models
	 */
	public List<Project> getAllProjects() {
		List<Project> projects = new ArrayList<>();

		String sql = "SELECT * FROM Projects";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				projects.add(mapProject(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return projects;
	}

	/**
	 * Gets all projects in which provided user is participating
	 * 
	 * @param userId Unique identifier of the user
	 * @return A list of all associated projects
	 */
	public List<Project> getAllProjects(int userId) {
		List<Project> projects = new ArrayList<>();

		String sql = "SELECT Projects.* " + "FROM Projects JOIN ProjectUsers USING (projectId) " + "WHERE userId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				projects.add(mapProject(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return projects;
	}

	/**
	 * Gets a project by its unique identifier
	 * 
	 * @param projectId The unique identifier of the project to get
	 * @return project model or null if project can not be found
	 */
	public Project getProject(int projectId) {
		Project project = null;

		String sql = "SELECT * FROM Projects WHERE projectId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				project = mapProject(rs);
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return project;
	}

	/**
	 * Creates a new project
	 * 
	 * @param project The project model to persist
	 * @return the created project
	 * @throws SQLException If project with given name already exists
	 */
	public Project createProject(Project project) throws SQLException {
		String sql = "INSERT INTO Projects (`name`) values (?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, project.getName());

		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();

		Project p = null;
		if (rs.next()) {
			p = getProject(rs.getInt(1));
		}

		ps.close();
		return p;
	}

	/**
	 * Removes a project using its unique identifier
	 * 
	 * @param projectId The unique identifier of the project
	 */
	public void deleteProject(int projectId) {
		String sql = "DELETE FROM Projects WHERE projectId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a list of all activitySubTypes
	 * 
	 * @return A list of activitySubType models
	 */
	public List<ActivitySubType> getActivitySubTypes() {
		List<ActivitySubType> activitySubTypes = new ArrayList<>();

		String sql = "SELECT * FROM ActivitySubTypes ORDER BY activitySubTypeId";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				activitySubTypes.add(mapActivitySubType(rs));
			}

			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return activitySubTypes;
	}

	/**
	 * Gets a list of all activitySubTypes for given activityType
	 * 
	 * @param activityTypeId Unique identifier of activityType
	 * @return A list of activitySubType models
	 */
	public List<ActivitySubType> getActivitySubTypes(int activityTypeId) {
		List<ActivitySubType> activitySubTypes = new ArrayList<>();

		String sql = "SELECT * FROM ActivitySubTypes WHERE activityTypeId = ? ORDER BY activitySubTypeId";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, activityTypeId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				activitySubTypes.add(mapActivitySubType(rs));
			}

			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return activitySubTypes;
	}

	/**
	 * Gets a list of all activityTypes
	 * 
	 * @return A list of activityType models
	 */
	public List<ActivityType> getActivityTypes() {
		List<ActivityType> activityTypes = new ArrayList<>();

		String sql = "SELECT * FROM ActivityTypes ORDER BY activityTypeId";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				activityTypes.add(mapActivityType(rs));
			}

			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return activityTypes;
	}

	/**
	 * Gets a list of all roles
	 * 
	 * @return A list of role models
	 */
	public List<Role> getAllRoles() {
		List<Role> allRoles = new ArrayList<>();

		String sql = "SELECT * FROM Roles";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				allRoles.add(mapRole(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allRoles;
	}

	/**
	 * Gets the role for the given user in the given project
	 * 
	 * @param userId Unique identifier of the user
	 * @param projectId Unique identifier of the project
	 * @return role model
	 * @throws Exception If the user actually does not belong to given project
	 */
	public Role getRole(int userId, int projectId) throws Exception {
		String sql = "SELECT Roles.* FROM ProjectUsers JOIN Roles USING (roleId) WHERE (userId, projectId) = (?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, userId);
		ps.setInt(2, projectId);

		ResultSet rs = ps.executeQuery();

		Role role = rs.next() ? mapRole(rs) : null;
		ps.close();
		
		if (role == null) {
			throw new Exception("User does not belong to project");
		}
		
		return role;
	}

	/**
	 * Gets all activityReports connected to given timeReport
	 * 
	 * @param timeReportId Unique identifier of the timeReport for which to find
	 *                     connected activity reports
	 * @return List of activityReport models
	 * @throws SQLException if the timereport does not exist
	 */
	public List<ActivityReport> getActivityReports(int timeReportId) throws SQLException {
		List<ActivityReport> activityReports = new ArrayList<>();

		String sql = "SELECT * FROM ActivityReports WHERE timeReportId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, timeReportId);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			activityReports.add(mapActivityReport(rs));
		}

		ps.close();
		return activityReports;
	}

	/**
	 * Gets activityReport by its unique identifier
	 * 
	 * @param activityReportId Unique identifier of the activityReport
	 * @return ActivityReport model or null if it can not be found
	 */
	public ActivityReport getActivityReport(int activityReportId) {
		ActivityReport activityReport = null;

		String sql = "SELECT * FROM ActivityReports WHERE activityReportId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, activityReportId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				activityReport = mapActivityReport(rs);
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return activityReport;
	}

	/**
	 * Creates an activityReport
	 * 
	 * @param activityReport The activityReport to be persisted
	 * @return The persisted activityReport model
	 * @throws SQLException if faulty values provided
	 */
	public ActivityReport createActivityReport(ActivityReport activityReport) throws SQLException {
		String sql = "INSERT INTO ActivityReports (`activityTypeId`, `activitySubTypeId`, `timeReportId`, `reportDate`, `minutes`) "
				+ "values (?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

		ps.setInt(1, activityReport.getActivityTypeId());
		ps.setObject(2, activityReport.getActivitySubTypeId() == 0 ? null : activityReport.getActivitySubTypeId());
		ps.setInt(3, activityReport.getTimeReportId());
		ps.setDate(4, Date.valueOf(activityReport.getReportDate()));
		ps.setInt(5, activityReport.getMinutes());

		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();

		ActivityReport newAR = null;
		if (rs.next()) {
			newAR = getActivityReport(rs.getInt(1));
		}

		ps.close();
		return newAR;
	}

	/**
	 * Updates an activityReport
	 * 
	 * @param activityReport The activityReport to be updated. Must exist in the
	 *                       database before running this
	 * @return The updated and persisted activityReport model
	 * @throws Exception if the update goes wrong
	 */
	public ActivityReport updateActivityReport(ActivityReport activityReport) throws Exception {
		String sql = "UPDATE ActivityReports "
				+ "SET `activityTypeId` = ?, `activitySubTypeId` = ?, `timeReportId` = ?, `reportDate` = ?, `minutes` = ? "
				+ "WHERE activityReportId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, activityReport.getActivityTypeId());
		ps.setObject(2, activityReport.getActivitySubTypeId() == 0 ? null : activityReport.getActivitySubTypeId());
		ps.setInt(3, activityReport.getTimeReportId());
		ps.setDate(4, Date.valueOf(activityReport.getReportDate()));
		ps.setInt(5, activityReport.getMinutes());
		ps.setInt(6, activityReport.getActivityReportId());

		int updated = ps.executeUpdate();

		ps.close();

		if (updated == 0) {
			return activityReport;
		} else {
			return getActivityReport(activityReport.getActivityReportId());
		}
	}

	/**
	 * Removes an activityReport by its unique identifier
	 * 
	 * @param activityReportId Unique identifier of the activityReport to remove
	 */
	public void deleteActivityReport(int activityReportId) {
		String sql = "DELETE FROM ActivityReports WHERE activityReportId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, activityReportId);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets all time reports that exists in the given project
	 * 
	 * @param projectId The unique identifier of the project to find time reports
	 *                  for
	 * @return A list of all time reports in a project
	 */
	public List<TimeReport> getTimeReportsByProject(int projectId) {
		List<TimeReport> timeReports = new ArrayList<>();

		String sql = "SELECT TimeReports.* " + "FROM TimeReports JOIN ProjectUsers USING (projectUserId) "
				+ "WHERE projectId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				timeReports.add(mapTimeReport(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return timeReports;
	}

	/**
	 * Gets all time reports made by a given user
	 * 
	 * @param userId The unique identifier of the user to find time reports for
	 * @return A list of all time reports made by a user
	 */
	public List<TimeReport> getTimeReportsByUser(int userId) {
		List<TimeReport> timeReports = new ArrayList<>();

		String sql = "SELECT TimeReports.* " + "FROM TimeReports JOIN ProjectUsers USING (projectUserId) "
				+ "WHERE userId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				timeReports.add(mapTimeReport(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return timeReports;
	}
	
	/**
	 * Gets all time reports made by a given user in given project
	 * 
	 * @param userId The unique identifier of the user to find time reports for
	 * @param projectId The unique identifier of the project to find time reports
	 * @return A list of all time reports made by a in project
	 */
	public List<TimeReport> getTimeReportsByUserAndProject(int userId, int projectId) {
		List<TimeReport> timeReports = new ArrayList<>();

		String sql = "SELECT TimeReports.* " + "FROM TimeReports JOIN ProjectUsers USING (projectUserId) "
				+ "WHERE userId = ? AND projectId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setInt(2, projectId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				timeReports.add(mapTimeReport(rs));
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return timeReports;
	}

	/**
	 * Gets time report by timeReportId. Will return null if time report could not
	 * be found
	 * 
	 * @param timeReportId - The unique identifier of the time report
	 * @return TimeReport model or null
	 */
	public TimeReport getTimeReportById(int timeReportId) {
		TimeReport timeReport = null;

		String sql = "SELECT * FROM TimeReports WHERE timeReportId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, timeReportId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				timeReport = mapTimeReport(rs);
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return timeReport;
	}

	/**
	 * Checks if a time report exists for the given parameters
	 * 
	 * @param week The specific week the time report applies to
	 * @param year The specific year the time report applies to
	 * @param userId The unique identifier of the user to find time reports for
	 * @param projectId The unique identifier of the project to find time reports
	 *                  for
	 * @return True if the time report exists, otherwise False
	 */
	public boolean hasTimeReport(int week, int year, int userId, int projectId) {
		int count = 0;

		String sql = "SELECT COUNT(timeReportId) AS numberOfReports "
				+ "FROM TimeReports JOIN ProjectUsers USING (projectUserId) "
				+ "WHERE (week, year, userId, projectId) = (?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, week);
			ps.setInt(2, year);
			ps.setInt(3, userId);
			ps.setInt(4, projectId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				count = rs.getInt("numberOfReports");
			}

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count > 0;
	}

	/**
	 * Creates a time report
	 * 
	 * @param timeReport The TimeReport to be persisted
	 * @return The persisted TimeReport model
	 * @throws SQLException if anything goes wrong
	 */
	public TimeReport createTimeReport(TimeReport timeReport) throws SQLException {
		String sql = "INSERT INTO TimeReports (`projectUserId`, `signedById`, `signedAt`, `year`, `week`, `updatedAt`, `finished`) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		ps.setInt(1, timeReport.getProjectUserId());

		if (timeReport.getSignedById() == 0) {
			ps.setObject(2, null);
		} else {
			ps.setInt(2, timeReport.getSignedById());
		}

		if (timeReport.getSignedAt() == null) {
			ps.setObject(3, null);
		} else {
			ps.setTimestamp(3, Timestamp.valueOf(timeReport.getSignedAt()));
		}

		ps.setInt(4, timeReport.getYear());
		ps.setInt(5, timeReport.getWeek());

		if (timeReport.getUpdatedAt() == null) {
			timeReport.setUpdatedAt(LocalDateTime.now());
		}
		ps.setTimestamp(6, Timestamp.valueOf(timeReport.getUpdatedAt()));
		ps.setBoolean(7, timeReport.isFinished());

		ps.executeUpdate();
		ResultSet rs = ps.getGeneratedKeys();

		TimeReport newTR = null;
		if (rs.next()) {
			newTR = getTimeReportById(rs.getInt(1));
		}

		ps.close();
		return newTR;
	}

	/**
	 * Updates a time report
	 * 
	 * @param timeReport The TimeReport to be updated. Must exist in the database
	 *                   before running this
	 * @return The updated and persisted TimeReport model
	 * @throws Exception if the update goes wrong
	 */
	public TimeReport updateTimeReport(TimeReport timeReport) throws Exception {
		String sql = "UPDATE TimeReports " + "SET `projectUserId` = ?, `signedById` = ?, `signedAt` = ?, "
				+ "`year` = ?, `week` = ?, `updatedAt` = ?, `finished` = ? " + "WHERE timeReportId = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, timeReport.getProjectUserId());

		if (timeReport.getSignedById() == 0) {
			ps.setObject(2, null);
		} else {
			ps.setInt(2, timeReport.getSignedById());
		}

		if (timeReport.getSignedAt() == null) {
			ps.setObject(3, null);
		} else {
			ps.setTimestamp(3, Timestamp.valueOf(timeReport.getSignedAt()));
		}

		ps.setInt(4, timeReport.getYear());
		ps.setInt(5, timeReport.getWeek());

		if (timeReport.getUpdatedAt() == null) {
			timeReport.setUpdatedAt(LocalDateTime.now());
		}
		ps.setTimestamp(6, Timestamp.valueOf(timeReport.getUpdatedAt()));
		ps.setBoolean(7, timeReport.isFinished());
		ps.setInt(8, timeReport.getTimeReportId());

		int updated = ps.executeUpdate();

		ps.close();

		if (updated == 0) {
			return timeReport;
		} else {
			return getTimeReportById(timeReport.getTimeReportId());
		}
	} 

	/**
	 * Removes a time report by its unique identifier
	 * 
	 * @param timeReportId Unique identifier of the time report to remove
	 */
	public void deleteTimeReport(int timeReportId) {
		String sql = "DELETE FROM TimeReports WHERE timeReportId = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, timeReportId);

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	enum StatisticType {
		ALL,
		USER,
		ROLE
	}

	/**
	 * Gets statistics of the total minutes put in a project between two dates based on 
	 * all users in the project, a specific user in the project or a specific role in the project.
	 * @param from Is the start date to gather the data from.
	 * @param to Is the end date to gather the data from.
	 * @param type Is the type of data to gather. From all users, a specific user or a specific role.
	 * @param projectId Is the unique identifier for Project.
	 * @param userId Is the unique identifier for User. Only needed when showing data for a specific user.
	 * @param roleId Is the unique identifier for Role. Only needed when showing data for a specific role.
	 * @return This returns statistics for all users in a project, a specific user in a project or a specific role in a project.
	 */
	private Statistic getStatistics(LocalDate from, LocalDate to, StatisticType type, int projectId, int userId, int roleId) {
		Set<LocalDate> datesT = new TreeSet<>();
		datesT.add(from);

		for (LocalDate date = from; date.isBefore(to); date = date.plusDays(1)) {
			if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
				datesT.add(date);
			}
		}
		datesT.add(to);

		List<LocalDate> dates = new ArrayList<LocalDate>(datesT);

		List<ActivityType> at = getActivityTypes();

		String[] rowLabels = new String[at.size()];
		String[] columnLabels = new String[dates.size() - 1];

		Map<String, Integer> rowLabelMap = new HashMap<String, Integer>();

		for (int i = 0; i < at.size(); i++) {
			rowLabels[i] = at.get(i).getType();
			rowLabelMap.put(rowLabels[i], i);
		}

		for (int i = 0; i < dates.size() - 1; i++) {
			columnLabels[i] = "v." + dates.get(i).get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
		}

		int[][] data = new int[rowLabels.length][columnLabels.length];
		
		StringBuilder sb = new  StringBuilder();
		sb.append("SELECT AcT.type, SUM(AR.minutes) AS minutes ");
		sb.append("FROM ActivityReports AR ");
		sb.append("JOIN ActivityTypes AcT USING (activityTypeId) ");
		sb.append("JOIN TimeReports TR USING (timeReportId) ");
		sb.append("JOIN ProjectUsers PU USING (projectUserId) ");
		sb.append("WHERE TR.signedAt IS NOT NULL ");
		sb.append("AND AR.reportDate >= ? ");
		sb.append("AND AR.reportDate < ? ");
		sb.append("AND PU.projectId = ? ");

		switch (type) {
		case ALL:
			break;
		case USER:
			sb.append("AND PU.userId = ? ");
			break;
		case ROLE:
			sb.append("AND PU.roleId = ? ");
			break;
		}
		
		sb.append("GROUP BY AcT.activityTypeId");
		
		String sql = sb.toString();

		for (int i = 1; i < dates.size(); i++) {
			int col = i - 1;
			
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, dates.get(i - 1).toString());
				ps.setString(2, dates.get(i).toString());
				ps.setInt(3, projectId);
				
				switch (type) {
				case ALL:
					break;
				case USER:
					ps.setInt(4, userId);
					break;
				case ROLE:
					ps.setInt(4, roleId);
					break;
				}

				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					String t = rs.getString("type");
					int minutes = rs.getInt("minutes");

					data[rowLabelMap.get(t)][col] = minutes;
				}

				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return new Statistic(columnLabels, rowLabels, data);
	}
	
	/**
	 * Gets statistics of the total minutes put in a project between two dates based on 
	 * all users in the project.
	 * @param projectId Is the unique identifier for Project.
	 * @param from Is the start date to gather the data from.
	 * @param to Is the end date to gather the data from.
	 * @return This returns statistics for all users in a project.
	 */
	public Statistic getActivityStatistics(int projectId, LocalDate from, LocalDate to) {
		return getStatistics(from, to, StatisticType.ALL, projectId, 0, 0);
	}

	/**
	 * Gets statistics of the total minutes put in a project between two dates based on 
	 * a specific user in the project.
	 * @param projectId Is the unique identifier for Project.
	 * @param userId Is the unique identifier for User.
	 * @param from Is the start date to gather the data from.
	 * @param to Is the end date to gather the data from.
	 * @return This returns statistics for a specific user in a project.
	 */
	public Statistic getActivityStatistics(int projectId, int userId, LocalDate from, LocalDate to) {
		return getStatistics(from, to, StatisticType.USER, projectId, userId, 0);
	}

	/**
	 * Gets statistics of the total minutes put in a project between two dates based on 
	 * a specific role in the project.
	 * @param projectId Is the unique identifier for Project.
	 * @param roleId Is the unique identifier for Role.
	 * @param from Is the start date to gather the data from.
	 * @param to Is the end date to gather the data from.
	 * @return This returns statistics for a specific role in a project.
	 */
	public Statistic getRoleStatistics(int projectId, int roleId, LocalDate from, LocalDate to) {
		return getStatistics(from, to, StatisticType.ROLE, projectId, 0, roleId);
	}
}
