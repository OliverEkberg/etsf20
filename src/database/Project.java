package database;

/**
* A model class for the Projects table in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class Project {
	private int projectId;
	private String name;
	
	/**
	 * Constructor for the model class Project.
	 * @param name Is the name of the project.
	 */
	public Project(String name) {
		this.projectId = 0;
		this.name = name;
	}
	
	/**
	 * Constructor for the model class Project
	 * @param projectId Is a unique identifier for Project.
	 * @param name Is the name of the project.
	 */
	public Project(int projectId, String name) {
		this.projectId = projectId;
		this.name = name;
	}

	/**
	 * Gets the unique identifier for the project.
	 * @return This returns the projectId.
	 */
	public int getProjectId() {
		return this.projectId;
	}
	
	/**
	 * Gets the name of the project.
	 * @return This returns the project name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the name of the project.
	 * @param name Is the project name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Overridden equals method. Returns true if the objects are equal.
	 */
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
