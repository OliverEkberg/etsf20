package database;

/**
* A model class for the ActivityTypes table in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class ActivityType {
	private int activityTypeId;
	private String type;
	
	/**
	 * Constructor for the model class ActivityType.
	 * @param activityTypeId Is a unique identifier for ActivityType.
	 * @param type Is the name of the activity type.
	 */
	public ActivityType(int activityTypeId, String type) {
		this.activityTypeId = activityTypeId;
		this.type = type;
	}
	
	/**
	 * Gets the unique identifier for the ActivityType.
	 * @return This returns the unique identifier activityTypeId.
	 */
	public int getActivityTypeId() {
		return activityTypeId;
	}
	
	/**
	 * Gets the name of the specific ActivityType.
	 */
	public String getType() {
		return type;
	}
}
