package database;

import java.time.LocalDate;

/**
 * A model class for the ActivityReports table in the database.
 * 
 * @author Jesper Annefors
 * @author Oliver Ekberg
 * @version 0.1
 * @since 2020-03-14
 */
public class ActivityReport {
	private int activityReportId;
	private int activityTypeId;
	private int activitySubTypeId;
	private int timeReportId;
	private LocalDate reportDate;
	private int minutes;
	
	/**
	 * Constructor for model class ActivityReport.
	 * @param activityTypeId Is a unique identifier for ActivityType.
	 * @param activitySubTypeId Is a unique identifier for ActivitySubType.
	 * @param timeReportId Is a unique identifier for TimeReport.
	 * @param reportDate Is the date when the activity report was created.
	 * @param minutes Is the total amount of minutes to report for the specific activity.
	 */
	public ActivityReport(int activityTypeId, int activitySubTypeId, int timeReportId, LocalDate reportDate,
			int minutes) {
		this.activityReportId = 0;
		this.activityTypeId = activityTypeId;
		this.activitySubTypeId = activitySubTypeId;
		this.timeReportId = timeReportId;
		this.reportDate = reportDate;
		this.minutes = minutes;
	}
	
	/**
	 * Constructor for model class ActivityReport.
	 * @param activityReportId Is a unique identifier for ActivityReport.
	 * @param activityTypeId Is a unique identifier for ActivityType.
	 * @param activitySubTypeId Is a unique identifier for ActivitySubType.
	 * @param timeReportId Is a unique identifier for TimeReport.
	 * @param reportDate Is the date when the activity report was created.
	 * @param minutes Is the total amount of minutes to report for the specific activity.
	 */
	public ActivityReport(int activityReportId, int activityTypeId, int activitySubTypeId, int timeReportId,
			LocalDate reportDate, int minutes) {
		this.activityReportId = activityReportId;
		this.activityTypeId = activityTypeId;
		this.activitySubTypeId = activitySubTypeId;
		this.timeReportId = timeReportId;
		this.reportDate = reportDate;
		this.minutes = minutes;
	}
	
	/**
	 * Gets the date the ActivityReport was created.
	 * @return This returns the creation date.
	 */
	public LocalDate getReportDate() {
		return reportDate;
	}
	
	/**
	 * Sets the creation date for the activity report.
	 * @param date Is the date to be set.
	 */
	public void setReportDate(LocalDate date) {
		this.reportDate = date;
	}
	
	/**
	 * Gets the total amount of minutes reported in a specific activity report.
	 * @return This returns the minutes reported in the activity report.
	 */
	public int getMinutes() {
		return minutes;
	}
	
	/**
	 * Sets the total amount of minutes to report in a activity report.
	 * @param minutes Is the total amount of minutes to report.
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	
	/**
	 * Gets the unique identifier for the ActivityReport.
	 * @return This returns the unique identifier activityReportId.
	 */
	public int getActivityReportId() {
		return activityReportId;
	}
	
	/**
	 * Gets the unique identifier for the ActivityType used in the activity report.
	 * @return This returns the unique identifier activityTypeId.
	 */
	public int getActivityTypeId() {
		return activityTypeId;
	}
	
	/**
	 * Gets the unique identifier for the ActivitySubType used in the activity report.
	 * @return This returns the unique identifier activitySubTypeId.
	 */
	public int getActivitySubTypeId() {
		return activitySubTypeId;
	}
	
	/**
	 * Gets the unique identifier for the TimeReport used in the activity report.
	 * @return This returns the unique identifier timeReportId.
	 */
	public int getTimeReportId() {
		return timeReportId;
	}
	
}
