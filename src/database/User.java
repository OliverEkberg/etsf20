package database;

/**
* A model class for the Users table in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class User {
	private int userId;
	private String username;
	private String password;
	private boolean isAdmin;
	
	/**
	 * Constructor of the model class User.
	 * @param username Is the username of the User.
	 * @param password Is the password of the User.
	 * @param isAdmin Is true if the user is an admin and false if not.
	 */
	public User(String username, String password, boolean isAdmin) {
		userId = 0;
		this.username = username;
		this.password = password;
		this.isAdmin = isAdmin;
	}

	/**
	 * Constructor of the model class User.
	 * @param userId Is the unique identifier for User.
	 * @param username Is the username of the User.
	 * @param password Is the password of the User.
	 * @param isAdmin Is true if the user is an admin and false if not.
	 */
	public User(int userId, String username, String password, boolean isAdmin) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.isAdmin = isAdmin;
	}

	/**
	 * Gets the username of the User.
	 * @return This returns the username.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Sets the username for the User.
	 * @param username Is the username to login with.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Gets the password of the User.
	 * @return This returns the password.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Sets the password for the User.
	 * @param password Is the password to login with.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Checks if the user is admin or not.
	 * @return This returns true if the user is an admin. Otherwise, it returns false.
	 */
	public boolean isAdmin() {
		return isAdmin;
	}
	
	/**
	 * Sets whether the user is an admin or not.
	 * @param isAdmin Is true if the user is and adming. Otherwise, it is set to false.
	 */
	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	/**
	 * Gets the unique identifier for the User.
	 * @return This returns the userId.
	 */
	public int getUserId() {
		return userId;
	}
}
