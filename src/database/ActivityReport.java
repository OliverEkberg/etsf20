package database;

import java.time.LocalDate;

public class ActivityReport {
	private int activityReportId;
	private int activityTypeId;
	private int activitySubTypeId;
	private int timeReportId;
	private LocalDate reportDate;
	private int minutes;
	
	public LocalDate getReportDate() {
		return reportDate;
	}
	
	public void setReportDate(LocalDate date) {
		this.reportDate = date;
	}
	
	public int getMinutes() {
		return minutes;
	}
	
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	
	public int getActivityReportId() {
		return activityReportId;
	}
	
	public int getActivityTypeId() {
		return activityTypeId;
	}
	
	public int getActivitySubTypeId() {
		return activitySubTypeId;
	}
	
	public int getTimeReportId() {
		return timeReportId;
	}
	
}
