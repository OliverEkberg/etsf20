package database;

import java.time.LocalDateTime;

/**
* A model class for the TimeReports table in the database.
* 
* @author Jesper Annefors
* @author Oliver Ekberg
* @version 0.1
* @since 2020-03-14
*/
public class TimeReport {
	private int timeReportId;
	private int projectUserId;
	private int signedById;
	private LocalDateTime signedAt;
	private int year;
	private int week;
	private LocalDateTime updatedAt;
	private boolean finished;
	
	/**
	 * Constructor for the model class TimeReport.
	 * @param projectUserId Is the unique identifier for ProjectUser.
	 * @param year Is the year the time report was created.
	 * @param week Is the week the time report was created.
	 */
	public TimeReport(int projectUserId, int year, int week) {
		timeReportId = 0;
		this.projectUserId = projectUserId;
		signedById = 0;
		signedAt = null;
		this.year = year;
		this.week = week;
		updatedAt = LocalDateTime.now();
		finished = false;
	}

	/**
	 * Constructor for the model class TimeReport.
	 * @param timeReportId Is the unique identifier for TimeReport.
	 * @param projectUserId Is the unique identifier for ProjectUser.
	 * @param signedById Is the unique identifier of the user that has signed the time report.
	 * @param signedAt Is the time and date when the time report was signed.
	 * @param year Is the year the time report was created.
	 * @param week Is the week the time report was created.
	 * @param updatedAt Is the time and date when the time report was most recently updated at.
	 * @param finished Is whether the time report is ready to be signed or not. True means the time report is ready to be signed.
	 */
	public TimeReport(int timeReportId, int projectUserId, int signedById, LocalDateTime signedAt, int year, int week,
			LocalDateTime updatedAt, boolean finished) {
		this.timeReportId = timeReportId;
		this.projectUserId = projectUserId;
		this.signedById = signedById;
		this.signedAt = signedAt;
		this.year = year;
		this.week = week;
		this.updatedAt = updatedAt;
		this.finished = finished;
	}

	/**
	 * Gets the time and date when the time report was most recently updated at.
	 * @return This returns the time and date when the time report was most recently updated at.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	/**
	 * Sets the time and date when the time report was most recently updated at.
	 * @param updatedAt Is the time and date when the time report was updated.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	/**
	 * Checks whether the time report is ready to be signed or not.
	 * @return This returns true if the time report is ready to be signed. Otherwise, it returns false.
	 */
	public boolean isFinished() {
		return finished;
	}
	
	/**
	 * Sets whether the time report is ready to be signed or not.
	 * @param finished Is true if the time report is ready to be signed and false if not.
	 */
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	/**
	 * Gets the unique identifier for the time report.
	 * @return This returns the timeReportId.
	 */
	public int getTimeReportId() {
		return timeReportId;
	}
	
	/**
	 * Gets the unique identifier for ProjectUser used in the time report.
	 * @return This returns the projectUserId.
	 */
	public int getProjectUserId() {
		return projectUserId;
	}
	
	/**
	 * Gets the unique identifier for the ProjectUser that has signed the time report.
	 * @return This returns the signedById. The signedById is 0 if the time report has not been signed yet.
	 */
	public int getSignedById() {
		return signedById;
	}
	
	/**
	 * Checks whether the time report has been signed or not. Returns true if it has been signed. Otherwise, it returns false.
	 * @return This returns true if the time report has been signed. Otherwise, it returns false.
	 */
	public boolean isSigned() {
		return signedById != 0;
	}
	
	/**
	 * Signs a time report with a unique identifier for ProjectUser.
	 * @param projectUserId Is the unique identifier of the ProjectUser that signs the time report.
	 */
	public void sign(int projectUserId) {
		signedById = projectUserId;
		signedAt = LocalDateTime.now();
	}
	
	/**
	 * Makes a time report unsigned.
	 */
	public void unsign() {
		signedById = 0;
		signedAt = null;
	}
	
	/**
	 * Gets the date and time when the time report was signed.
	 * @return This returns the date and time when a time report was signed if it has been signed. Otherwise, it returns null.
	 */
	public LocalDateTime getSignedAt() {
		return signedAt;
	}
	
	/**
	 * Gets the year when a time report was created.
	 * @return This returns the year when a TimeReport was created.
	 */
	public int getYear() {
		return year;
	}
	
	/**
	 * Gets the week when a time report was created.
	 * @return This returns the week when a TimeReport was created.
	 */
	public int getWeek() {
		return week;
	}
}
