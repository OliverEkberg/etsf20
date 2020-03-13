package database;

public class Project {
	private int projectId;
	private String name;
	
	public Project(String name) {
		this.name = name;
	}
	
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Project other = (Project) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (projectId != other.projectId)
			return false;
		return true;
	}
	
	
}
