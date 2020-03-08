package database;

public class ActivitySubType {
	private int activitySubTypeId;
	private int activityTypeId;
	private String subType;
	
	public ActivitySubType(int activitySubTypeId, int activityTypeId, String subType) {
		this.activitySubTypeId = activitySubTypeId;
		this.activityTypeId = activityTypeId;
		this.subType = subType;
	}

	public int getActivitySubTypeId() {
		return activitySubTypeId;
	}
	
	public int getActivityTypeId() {
		return activityTypeId;
	}
	
	public String getSubType() {
		return subType;
	}
}
