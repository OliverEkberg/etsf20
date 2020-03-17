package database;

/**
* A model class for the Roles table in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class Role {
	private int roleId;
	private String role;
	
	
	/**
	 * Constructor for the model class Role.
	 * @param roleId Is a unique identifier for Role.
	 * @param role Is the name of the role.
	 */
	public Role(int roleId, String role) {
		this.roleId = roleId;
		this.role = role;
	}

	/**
	 * Gets the unique identifier for the role.
	 * @return This returns the roleId.
	 */
	public int getRoleId() {
		return roleId;
	}
	
	/**
	 * Gets the name of the role.
	 * @return This returns the role name.
	 */
	public String getRole() {
		return role;
	}
}
