package database;

public class ActivityType {
	private int activityTypeId;
	private String type;
	
	public ActivityType(int activityTypeId, String type) {
		this.activityTypeId = activityTypeId;
		this.type = type;
	}

	public int getActivityTypeId() {
		return activityTypeId;
	}
	
	public String getType() {
		return type;
	}
}
