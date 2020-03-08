package database;

public class Project {
	private int projectId;
	private String name;
	
	public Project(int projectId, String name) {
		this.projectId = projectId;
		this.name = name;
	}

	public int getProjectId() {
		return this.projectId;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
