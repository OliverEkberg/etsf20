package database;

public class Role {
	private int roleId;
	private String role;
	
	public Role(int roleId, String role) {
		this.roleId = roleId;
		this.role = role;
	}

	public int getRoleId() {
		return roleId;
	}
	
	public String getRole() {
		return role;
	}
}
