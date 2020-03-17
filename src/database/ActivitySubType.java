package database;

/**
* A model class for the ActivitySubTypes table in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class ActivitySubType {
	private int activitySubTypeId;
	private int activityTypeId;
	private String subType;
	
	/**
	 * Constructor for model class ActivitySubType
	 * @param activitySubTypeId Is a unique identifier for ActivitySubType.
	 * @param activityTypeId Is a unique identifier for ActivityType.
	 * @param subType Is the name of the activity subtype.
	 */
	public ActivitySubType(int activitySubTypeId, int activityTypeId, String subType) {
		this.activitySubTypeId = activitySubTypeId;
		this.activityTypeId = activityTypeId;
		this.subType = subType;
	}
	
	/**
	 * Gets the unique identifier for the ActivitySubType.
	 * @return This returns the unique identifier activitySubTypeId.
	 */
	public int getActivitySubTypeId() {
		return activitySubTypeId;
	}
	
	/**
	 * Gets the unique identifier of the ActivityType that has this activity subtype.
	 * @return This returns the unique identifier activityTypeId.
	 */
	public int getActivityTypeId() {
		return activityTypeId;
	}
	
	/**
	 * Gets the name of the specific ActivitySubType.
	 * @return This returns the name of the activity subtype.
	 */
	public String getSubType() {
		return subType;
	}
}
