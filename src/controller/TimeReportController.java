package controller;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import baseblocksystem.servletBase;
import database.ActivityReport;
import database.ActivitySubType;
import database.ActivityType;
import database.TimeReport;
import database.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TimeReportController
 * 
 * A timereporting page.
 * 
 * Handles timereporting and all that it includes. 
 * 
 * @author Linus, Sebastian, Andre
 * 
 * @version 1.0
 * 
 */

@WebServlet("/" + Constants.TIMEREPORTS_PATH)
public class TimeReportController extends servletBase {
	private static final long serialVersionUID = 1L;

	@Override
	/**
	 * Handles all logic for sending the user between different timereporting pages.
	 * *
	 * 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setSessionTimeout(req);

		try {
			PrintWriter out = resp.getWriter();
			User loggedInUser = this.getLoggedInUser(req);

			String activityType = req.getParameter("activityTypeId");
			String addReportWeek = req.getParameter("addReportWeek");
			String addReportYear = req.getParameter("addReportYear");
			String dateOfReport = req.getParameter("dateOfReport");
			String deleteActivityReportId = req.getParameter("deleteActivityReportId");
			String deleteTimeReportId = req.getParameter("deleteTimeReportId");
			String error = req.getParameter("error");
			String week = req.getParameter("weekFilter");
			String year = req.getParameter("yearFilter");
			String status = req.getParameter("status");
			String showUserPage = req.getParameter("showUserPage");
			String subType = req.getParameter("activitySubType");
			String timeReportFinishedId = req.getParameter("timeReportFinishedId");
			String timeReportId = req.getParameter("timeReportId");
			String timeReportNotFinishedId = req.getParameter("timeReportNotFinishedId");
			String timeReportSignId = req.getParameter("timeReportIdToSign");
			String timeReportUnsignId = req.getParameter("timeReportIdToUnsign");
			String timeSpent = req.getParameter("timeSpent");
			String userQuery = req.getParameter("user");

			Integer userQueryInteger = (userQuery == null || "*".equals(userQuery)) ? null
					: Integer.parseInt(userQuery);
			
			if (!isProjectLeader(req)) {
				userQueryInteger = getLoggedInUser(req).getUserId();
			}
			
			Integer weekInteger = (week == null || "*".equals(week)) ? null
					: Integer.parseInt(week);
			
			Integer yearInteger = (year == null || "*".equals(year)) ? null
					: Integer.parseInt(year);

			out.println(getHeader(req));
			out.println("<body>");
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/TimeReportController.css\">\n");
			out.println(getNav(req));
			out.println("<p id=\"report_title_text\">Reports</p>");

			if (loggedInUser == null) {
				resp.sendRedirect("/BaseBlockSystem/" + Constants.SESSION_PATH);
			}

			else if (loggedInUser.isAdmin()) {
				resp.sendRedirect("/BaseBlockSystem/" + Constants.SESSION_PATH);
			}

			if (getProjectId(req) == 0) {
				out.print("<p>Please choose a project first!</p>");
				return;
			}

			int weekNumber = Helpers.getWeekNbr(LocalDate.now());

			if (error != null) { // If user gets an error
				out.println("<script> " + "		alert('" + error + " ')" + "</script>");
			}

			if (addReportWeek != null && addReportYear != null
					&& Integer.parseInt(addReportYear) == LocalDate.now().getYear()
					&& Integer.parseInt(addReportWeek) > weekNumber) {

				resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH
						+ "?error=cant-create-timereport-in-the-future");
				return;
			}

			// Parameters for creating a new activityreport
			if (activityType != null && subType != null && timeSpent != null && addReportWeek != null
					&& addReportYear != null && timeReportId != null && dateOfReport != null) {

				if (Integer.parseInt(timeSpent) == 0 || Integer.parseInt(timeSpent) > Constants.MAX_MINUTES_PER_DAY) {

					resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH
							+ "?time-can-only-be-a-number-between-1-and-" + Constants.MAX_MINUTES_PER_DAY);
					return;

				}

				int activityTypeId = 0;
				int activitySubTypeId = 0;
				ActivityReport activityReport;
				LocalDate date = LocalDate.parse(dateOfReport);
				activityTypeId = Integer.parseInt(activityType);

				List<ActivitySubType> subTypeList = dbService.getActivitySubTypes(activityTypeId);
				for (ActivitySubType ast : subTypeList) {

					// find subtype of activity
					if (ast.getSubType().equals(subType)) {
						activitySubTypeId = ast.getActivitySubTypeId();
					}
				}

				activityReport = createActivityReport(activityTypeId, activitySubTypeId, date,
						Integer.parseInt(addReportYear), Integer.parseInt(addReportWeek), Integer.parseInt(timeSpent),
						loggedInUser.getUserId(), this.getProjectId(req), resp);

				if (activityReport == null) {
					resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH
							+ "?error=activity-report-could-not-be-created");
					return;
				}

				TimeReport timereport = dbService.getTimeReportById(activityReport.getTimeReportId()); // get timereport
				out.print(getActivityReports(timereport.getTimeReportId(), req)); // Returns to the view of all
																					// activityreports for that
																					// timereport

				return;

			}

			// Parameters for showing activity report form
			if (addReportYear != null && addReportWeek != null && timeReportId != null && activityType == null
					&& subType == null) {
				out.print(activityReportForm(Integer.parseInt(addReportWeek), Integer.parseInt(addReportYear),
						timeReportId, req));
				return;
			}

			// Parameters for deleting an activityreport
			if (deleteActivityReportId != null && timeReportId != null) {

				try {
					dbService.deleteActivityReport(Integer.parseInt(deleteActivityReportId));
				} catch (Exception e) {
				}
				out.print(getActivityReports(Integer.parseInt(timeReportId), req));
				return;

			}

			/*// Parameters for retriving all timereports for a specific week and year
			if (getReportsWeek != null && getReportsYear != null) {

				out.print(this.getTimereportsByWeekAndYear(Integer.parseInt(getReportsWeek),
						Integer.parseInt(getReportsYear), req));
				return;
			}
			
			*/

			// Shows the timereportingpage of a specific user
			if (showUserPage != null) {
				out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger));
				return;
			}

			// Projectleaders signs a timereport
			if (timeReportSignId != null) {

				if (this.isProjectLeader(req, this.getProjectId(req))) {
					TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportSignId));
					int projectUserId = dbService.getProjectUserIdByUserIdAndProjectId(loggedInUser.getUserId(),
							this.getProjectId(req));
					timeReport.sign(projectUserId);
					dbService.updateTimeReport(timeReport);
					out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger));
					return;
				}

				else {
					resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH
							+ "?error=only-a-projectleader-can-sign-a-timereport");
				}
			}

			// Proect leader unsigns a timereport
			if (timeReportUnsignId != null) {
				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportUnsignId));
				timeReport.unsign();

				dbService.updateTimeReport(timeReport);
				out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger));
				return;
			}

			// User marks timereport as not finished
			if (timeReportNotFinishedId != null) {

				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportNotFinishedId));
				timeReport.setFinished(false);
				dbService.updateTimeReport(timeReport);
				out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger));
				return;
			}

			// user marks timereport as finished
			if (timeReportFinishedId != null) {

				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportFinishedId));
				timeReport.setFinished(true);
				dbService.updateTimeReport(timeReport);
				out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger));
				return;
			}

			// User deletes one of their own timereports
			if (deleteTimeReportId != null) {

				try {
					dbService.deleteTimeReport(Integer.parseInt(deleteTimeReportId));
				} catch (Exception e) {
				}
				out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger));
				return;
			}

			// user adds report
			if (addReportWeek != null && addReportYear != null) {

				int addReportWeekInt = Integer.parseInt(addReportWeek);

				if (addReportWeekInt > 0 && addReportWeekInt <= 53) {
					out.print(activityReportForm(Integer.parseInt(addReportWeek), Integer.parseInt(addReportYear), "",
							req));
					return;
				} else {
					resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH
							+ "?error=cant-create-timereport-in-the-future-or-before-week-0");
				}

			}

			// Get activityreports for a specific timereport
			if (timeReportId != null) {

				out.print(getActivityReports(Integer.parseInt(timeReportId), req));
				return;
			}

			out.print(getUserTimeReports(req, userQueryInteger, status, yearInteger, weekInteger)); // Standard case, if nothing else works this
																			// is called

		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH + "?error=unexpected-error");
		}

	}

	/**
	 * Creates a new Activityreport and links it to an existing Timereport for the
	 * same week. If no Timereport exists for the given week, one is created
	 * 
	 * @param activityTypeId    - The int value of the activity type
	 * @param activitySubTypeId - The int value of the activity subtype
	 * @param date              - The date the activityreport was created
	 * @param year              - The year of the activity
	 * @param week              - The week of the activity
	 * @param minutes           - The amount of minutes spent on the activity
	 * @param userId            - The id of the user who creates the activityreport
	 * @param projectId         - The id of the project in which the activityreport
	 *                          was created.
	 * @param resp              - HttpServletResponse
	 * @return - The newly created activityreport
	 * @throws Exception
	 */
	private ActivityReport createActivityReport(int activityTypeId, int activitySubTypeId, LocalDate date, int year,
			int week, int minutes, int userId, int projectId, HttpServletResponse resp) throws Exception {

		TimeReport timereport = null;
		ActivityReport activityReport = null;
		int projectUserId = dbService.getProjectUserIdByUserIdAndProjectId(userId, projectId);

		if (dbService.hasTimeReport(week, year, userId, projectId)) {// Does timereport this week and year exist?

			List<TimeReport> allReports = dbService.getTimeReportsByUserAndProject(userId, projectId);
			for (TimeReport tr : allReports) {
				if (tr.getWeek() == week && tr.getYear() == year) { // Find timereport for this week and year amongst
																	// all timereports
					timereport = tr;

					int totalTime = dbService.getActivityReports(tr.getTimeReportId()).stream()
							.filter(ar -> ar.getReportDate().equals(date)).mapToInt(ar -> ar.getMinutes()).sum();

					if (totalTime + minutes > Constants.MAX_MINUTES_PER_DAY) {
//						resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH + "?error=total-amount-of-minutes-surpasses-maximum-daily-limit");
						return null;
					}

					if (tr.isFinished() || tr.isSigned()) {
						resp.sendRedirect("/BaseBlockSystem/" + Constants.TIMEREPORTS_PATH
								+ "?error=timereport-is-signed-or-marked-as-ready-for-signing-and-cant-be-edited.");
						return null;
					}
				}
			}
		} else { // Else - timereport this week and year didnt exist, create one!
			timereport = dbService.createTimeReport(
					new TimeReport(0, projectUserId, 0, null, year, week, LocalDateTime.now(), false));
		}

		activityReport = dbService.createActivityReport(
				new ActivityReport(0, activityTypeId, activitySubTypeId, timereport.getTimeReportId(), date, minutes));

		return activityReport;

	}

	/**
	 * Builds a String containing a HTML page showing all Activityreports within the
	 * specified timereport.
	 * 
	 * @param timeReportId - The id of the timereport which you want to access.
	 * @param req          - HttpServletRequest
	 * @return A String containing HTML to show the page described above.
	 * @throws Exception
	 */
	private String getActivityReports(int timeReportId, HttpServletRequest req) throws Exception {

		String html = "";

		TimeReport timeReport = dbService.getTimeReportById(timeReportId);
		User reportOwner = dbService.getUserByTimeReportId(timeReportId);
		boolean isProjectLeader = isProjectLeader(req);
		boolean reportIsSigned = timeReport.isSigned();
		boolean reportIsFinished = timeReport.isFinished();
		boolean isActivityReportsDeletable = !reportIsSigned && !reportIsFinished
				&& isUserLoggedInUser(req, reportOwner); // If timereport isn't signed, and the report owner is the one
															// accessing it, show button for deleting activity, else do
															// not show it.

		// If projectleader is looking within a report, and it isn't their own. Display
		// the name of the report owner.
		if (this.isProjectLeader(req) && !isUserLoggedInUser(req, reportOwner)) {
			html += "<body> <b> " + reportOwner.getUsername() + "</b> <br> </body>\r\n";
		}

		// HTML table start and header.
		html += "<table width=\"600\" border=\"1\">\r\n" + "<tr>\r\n" + "<th> Date </th>\r\n"
				+ "<th> Activitytype</th>\r\n" + "<th> Subtype </th>\r\n" + "<th> Minutes </th>\r\n";

		if (isActivityReportsDeletable) {
			html += "<th> Remove activity report </th>\r\n";
		}

		List<ActivityReport> activityReports = dbService.getActivityReports(timeReportId);
		List<ActivityType> activityTypes = dbService.getActivityTypes();
		List<ActivitySubType> activitySubTypes = dbService.getActivitySubTypes();

		String activityType;
		String activitySubType;

		// For every activityreport, add its info and required buttons to the HTML table
		for (ActivityReport aReport : activityReports) {

			activityType = getActivityType(aReport, activityTypes);
			activitySubType = getActivitySubType(aReport, activitySubTypes);

			html += "<tr>\r\n" + "<td>" + aReport.getReportDate().toString() + "</td>\r\n" + "<td>" + activityType
					+ "</td>\r\n" + "<td>" + activitySubType + "</td>\r\n" + "<td>" + aReport.getMinutes()
					+ "</td>\r\n";

			if (isActivityReportsDeletable) {
				html += "<td> <form action=\"" + Constants.TIMEREPORTS_PATH + "?deleteActivityReportId=\""
						+ aReport.getActivityReportId() + "&timeReportId=\"" + timeReportId + "\" method=\"get\">\r\n"
						+ "		<input name=\"deleteActivityReportId\" type=\"hidden\" value=\""
						+ aReport.getActivityReportId() + "\"></input>\r\n"
						+ " <input name=\"timeReportId\" type=\"hidden\" value=\"" + timeReportId + "\"></input>\r\n"
						+ "		<input type=\"submit\" value=\"Remove\"></input>\r\n" + "	</td> \r\n" + "</form>";

			}

		}

		// Ends the HTML table
		html += "</tr>\r\n" + "</table>";

		// If projectleader is looking at timereports, show sign/unsign buttons or that
		// the report isn't ready to be signed
		if (isProjectLeader) {

			if (reportIsSigned) {
				html += "<form method=\"get\"> <button name=\"timeReportIdToUnsign\" type=\"submit\" value=\""
						+ timeReport.getTimeReportId() + "\"> Unsign </button>  </form> \r\n";
			}

			else if (!reportIsSigned && reportIsFinished) {
				html += "<form method=\"get\"> <button name=\"timeReportIdToSign\" type=\"submit\" value=\""
						+ timeReport.getTimeReportId() + "\"> Sign </button>  </form> \r\n";
			}

			else if (!reportIsSigned && !reportIsFinished) {
				html += "<body> Report is not marked as ready for signing </body> \r\n";
			}
		}

		// If timereport owner is the one logged in and looking at this screen AND isnt
		// marked as finished
		if (isUserLoggedInUser(req, reportOwner) && !timeReport.isFinished() && !timeReport.isSigned()) {

			// Show button for adding activity
			html += "<form action=\"" + Constants.TIMEREPORTS_PATH + "?week=\"" + timeReport.getWeek()
					+ "&timeReportId=\"" + timeReportId + "\"&addReportYear=\"" + timeReport.getYear()
					+ "\" method=\"get\">\r\n" + "		<input name=\"addReportWeek\" type=\"hidden\" value=\""
					+ timeReport.getWeek() + "\"></input>\r\n"
					+ " <input name=\"timeReportId\" type=\"hidden\" value=\"" + timeReportId + "\"></input>\r\n"
					+ " <input name=\"addReportYear\" type=\"hidden\" value=\"" + timeReport.getYear()
					+ "\"></input>\r\n" + "		<input type=\"submit\" value=\"Add new activity\"></input>\r\n"
					+ "	</form>";

			// Button - Mark activity report as finished
			html += "<td> <form action = \"" + Constants.TIMEREPORTS_PATH + "?timeReportFinishedId=\""
					+ timeReport.getTimeReportId()
					+ "\" method=\"get\"> <button name=\"timeReportFinishedId\" type=\"submit\" value=\""
					+ timeReport.getTimeReportId() + "\"> Mark timereport as ready for signing </button>  </form> \r\n";
		}

		// If timerport owner is the one logged in and looking at this screen AND
		// timereport IS! marked and finished and not signed.
		else if (isUserLoggedInUser(req, reportOwner) && timeReport.isFinished() && !timeReport.isSigned()) {

			// Button - Unmark activity report as finished
			html += "<td> <form action = \"" + Constants.TIMEREPORTS_PATH + "?timeReportNotFinishedId=\""
					+ timeReport.getTimeReportId()
					+ "\" method=\"get\"> <button name=\"timeReportNotFinishedId\" type=\"submit\" value=\""
					+ timeReport.getTimeReportId() + "\"> Unmark </button>  </form> \r\n";
		}

		return html;
	}

	/**
	 * Retrives a String representation of a activity from a specific activity
	 * report.
	 * 
	 * @param activityReport - The specified activityreport.
	 * @param typeList       - A list containing all possible activity types
	 * @return a String representation of the activity.
	 * @throws Exception
	 */
	private String getActivityType(ActivityReport activityReport, List<ActivityType> typeList) throws Exception {

		// Get activity type for current activity report
		for (ActivityType aType : typeList) {

			if (aType.getActivityTypeId() == activityReport.getActivityTypeId()) {
				return aType.getType();
			}
		}

		return "";
	}

	/**
	 * Retrives a String representation of a subactivity from a specific activity
	 * report.
	 * 
	 * @param activityReport - The specified activityreport.
	 * @param subTypeList    - A list containing all possible activity subtypes
	 * @return a String representation of the activity subtype.
	 * @throws Exception
	 */
	private String getActivitySubType(ActivityReport activityReport, List<ActivitySubType> subTypeList)
			throws Exception {

		for (ActivitySubType aSubType : subTypeList) { // Get activity type for current activity report

			if (aSubType.getActivitySubTypeId() == activityReport.getActivitySubTypeId()) {

				return aSubType.getSubType();
			}
		}
		return "";
	}

	/**
	 * Builds a String containing a HTML page showing all timereports for a specific
	 * user inside the selected project.
	 * 
	 * @param user - The user of which you want to retrieve timereports from.
	 * @param req  - HttpServletRequest
	 * @return A String containing HTML to show the page described above.
	 * @throws Exception
	 */
	private String getUserTimeReports(HttpServletRequest req, Integer userId, String status, Integer year, Integer week) throws Exception {

		String html = "<!--square.html-->\r\n" + "<!DOCTYPE html>\r\n";
		LocalDate d = LocalDate.now();

		// Adds all users with timereports in this project into user list
		List<User> userList = dbService.getAllUsers(this.getProjectId(req));
		userList = sortUserList(userList);

		if (userId != null) {
			html += "<body> <b> " + dbService.getUserById(userId).getUsername() + "</b> <br> </body>\r\n";
		}

		if (isProjectLeader(req)) {

			// Timereport filtering
			html += "<html>\r\n" + "<div id=\"form\">" + " <form id=\"userFilter\" method=\"get\">\r\n"
					+ "          Get all timereports for this project for: \r\n"
					+ "                <div id=\"user\">\r\n"
					+ "                    <select id=\"user\" name=\"user\" form=\"userFilter\">\r\n"
					+ "                      			  <option value=\"*\" "+ (userId == null ? "" : "selected ") +">All users</option>\r\n";

			// User dropdown list
			for (User u : userList) {
				boolean selectedUser = userId != null && u.getUserId() == userId;
				html += "<option value=" + u.getUserId() + " " + (selectedUser ? "selected " : "") + "> " + u.getUsername() + " </option>\r\n";
			}

			html += "</select>\r\n " + "</div>\r\n" + " <div id=\"status\">\r\n"
					+ "                    <select id=\"status\" name=\"status\" form=\"userFilter\">\r\n"
					+ "                        <option value=\"*\""+ (status == null || status.equals("*") ? "" : "selected ") +">All</option>\r\n"
					+ "                        <option value=\"signed\" " + ("signed".equals(status) ? "selected " : "") + ">Signed</option>\r\n"
					+ "                        <option value=\"unsigned\" " + ("unsigned".equals(status) ? "selected " : "") + ">Unsigned</option>\r\n"
					+ "                        <option value=\"readyForSign\" " + ("readyForSign".equals(status) ? "selected " : "") + ">Ready for signing</option>\r\n"
					+ "						   </select>";
			
			// Get reports for week and year
			html +=	  "                <div id=\"weekFilter\">\r\n"
					+ "                    <select id=\"weekFilter\" name=\"weekFilter\" form=\"userFilter\">\r\n"
					+ "<option value=\"*\" "+ (week == null ? "" : "selected ") +">All</option>\r\n";

			// Week dropdown list
			for (int i = 1; i < 54; i++) {
				boolean selectedWeek = week != null && i == week;
				html += "<option value=" + i + " " + (selectedWeek ? "selected " : "") + ">Week: " + i + " </option>\r\n";
			}

			html += "</select>\r\n  </div>\r\n" + "<div id=\"yearFilter\">\r\n"
					+ "                    <select id=\"yearFilter\" name=\"yearFilter\" form=\"userFilter\">\r\n"
					+ "<option value=\"*\" "+ (year == null ? "" : "selected ") +">All</option>\r\n";
			// Year dropdown list
			for (int i = 2020; i <= d.getYear(); i++) {
				boolean selectedYear = year != null && i == year;
				html += "    <option value=" + i + " " + (selectedYear ? "selected " : "") + ">Year: " + i + "</option>\r\n";
			}
			
			// Button for retrieving timereports.
			html += "       </select>\r\n" 
					+ "             </div>\r\n"
					+ "			   <input type=\"submit\" value=\"Get timereports\" >\r\n" + "           </form>"
					+ "			 </div>" + "         </html>";
		}

		html += getTimereports(req, userId, status, year, week);

		// Adds more buttons and options underneath the table containing timereports
		try {
			// If the logged in user is the one browsing this page, give the option to
			// create a new timereport
			if (this.isUserIdLoggedInUser(userId, req)) {
				html += "<div id=\"form\">" + " <form id=\"filter_form\" method=\"get\">\r\n"
						+ "             Create timereport for: \r\n" 
						+ "                <div id=\"selectWeek\">\r\n"
						+ "                    <select id=\"addReportWeek\" name=\"addReportWeek\" form=\"filter_form\">\r\n";
				// Week selection dropdown list
				for (int i = 1; i < 54; i++) {

					html += "<option value=" + i + ">Week: " + i + " </option>\r\n";
				}

				html += "</select>\r\n  </div>\r\n" + "<div id=\"selectYear\">\r\n"
						+ "                    <select id=\"addReportYear\" name=\"addReportYear\" form=\"filter_form\">\r\n";
				// Year selection dropdown list
				for (int i = 2020; i <= d.getYear(); i++) {
					html += "                        <option value=" + i + ">Year: " + i + "</option>\r\n";
				}

				// Adds "Create" button
				html += "            </select>\r\n" + "              </div>\r\n" + "            </div>\r\n"
						+ "			  <input type=\"submit\" value=\"Create timereport\" >\r\n" + "           </form>"
						+ "</div>" + "          </html>";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return html;

	}
	
	/**
	 *  Builds a String containing a HTML page showing all timereports that conform to the specified filter parameters
	 * 
	 * @param req - HttpServletRequest
	 * @param userId - The id of user (or * for all users) whos timereports you wish to see.
	 * @param status - The status of the timereport(s) that you wish to see.
	 * @param year - The year of the the timereport(s) that you wish to see.
	 * @param week - The week of the timereport(s) that you wish to see.
	 * @return A String containing HTML to show the page described above.
	 * @throws Exception
	 */
	private String getTimereports(HttpServletRequest req, Integer userId, String status, Integer year, Integer week)
			throws Exception {

		String html = "<!--square.html-->\r\n" + "<!DOCTYPE html>\r\n";

		List<TimeReport> userTimeReportsTemp;
		if (userId != null) {
			userTimeReportsTemp = dbService.getTimeReportsByUserAndProject(userId == null ? 0 : userId,
					this.getProjectId(req));
		} else {
			userTimeReportsTemp = dbService.getTimeReportsByProject(this.getProjectId(req));
		}

		List<TimeReport> userTimeReports = new ArrayList<TimeReport>();
		for (TimeReport tr : userTimeReportsTemp) {

			if (sameYear(tr, year) && sameWeek(tr, week)) {

				if (status == null || status.equals("*")) {
					userTimeReports.add(tr);
				} else {

					switch (status) {
					case "signed":
						if (tr.getSignedAt() != null) {
							userTimeReports.add(tr);
						}
						break;

					case "unsigned":
						if (tr.getSignedAt() == null) {
							userTimeReports.add(tr);
						}
						break;

					case "readyForSign":
						if (tr.isFinished()) {
							userTimeReports.add(tr);
						}
						break;
					}
				}
			}
		}

		userTimeReports = sortTimeReports(userTimeReports);

		// Html table start and header
		html += "<table width=\"600\" border=\"1\">\r\n" + "<tr>\r\n" + "<th> Year </th>\r\n" + "<th> Week </th><th> Username </th>\r\n"
				+ "<th> Timespent (minutes) </th>\r\n" + "<th> Status </th>\r\n" + "<th> Ready for signing </th>\r\n"
				+ "<th> Select timereport </th>\r\n" + "<th> Remove timereport </th>\r\n";

		// Adds all timereports into the table
		for (TimeReport tr : userTimeReports) {

			int timeReportTotalTime = getTotalTimeReportTime(tr);
			String signed;
			String markedFinished;

			// get String representation of signed/unsigned
			if (tr.isSigned()) {
				signed = "Signed";
			} else {
				signed = "Unsigned";
			}

			if (tr.isFinished()) { // get isFinished or not
				if (tr.isSigned()) {
					markedFinished = "";
				} 
				else {
					markedFinished = "Yes";
				}
			} 
			else {
				markedFinished = "No";
			}

			// Add timereport information into table
			html += "<tr>\r\n" 
			+ "<td>" + tr.getYear() + "</td>\r\n" 
			+ "<td>" + tr.getWeek() + "</td>\r\n"
			+ "<td>" + dbService.getUserByTimeReportId(tr.getTimeReportId()).getUsername() + "</td>\r\n"
			+ "<td>" + timeReportTotalTime + "</td>\r\n" 
			+ "<td>" + signed + "</td>\r\n"
			+ "<td>" + markedFinished + "</td>\r\n" 
			+ "<td> <form action=\"" + Constants.TIMEREPORTS_PATH + "?timeReportId="
					+ tr.getTimeReportId() + "\" method=\"get\"> "
					+ "<button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId()
					+ "\"> Select </button>  </form> </td> \r\n";

			// If timereport isn't signed, and the report owner is the one browsing, a
			// button for deleting it should be visible
			if (!tr.isSigned() && this.isUserIdLoggedInUser(userId, req)) {
				html += "<td> <form action=\"" + Constants.TIMEREPORTS_PATH + "?deleteTimeReportId="
						+ tr.getTimeReportId() + "\" method=\"get\"> "
						+ "<button name=\"deleteTimeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId()
						+ "\"> Remove </button> </form> </td> \r\n";
			} else {
				html += "<td>";
			}

			html += "</tr>\r\n";

		}

		// END OF TABLE
		html += "</tr>\r\n" + "</table>";

		return html;

	}

	/**
	 *  Sees if the sent in TimeReport has the same year as the sent in year.
	 * 
	 * @param tr - The Timereport to check the year off.
	 * @param year - The year to check the timereport for.
	 * @return true if the years matches, else false.
	 */
	private boolean sameYear(TimeReport tr, Integer year) {

		if(year == null || tr.getYear() == year) {
			return true;
		}
		
		return false;
	}

	/**
	 *  Sees if the sent in TimeReport has the same week as the sent in week.
	 * 
	 * @param tr - The Timereport to check the week off.
	 * @param week - The week to check the timereport for.
	 * @return true if the years matches, else false.
	 */
	private boolean sameWeek(TimeReport tr, Integer week) {

		if(week == null || tr.getWeek() == week) {
			return true;
		}

		return false;
	}

	/**
	 * Sorts a list of Users alphabeticaly.
	 *  
	 * @param userList - The list to be sorted
	 * @return a sorted list.
	 */
	private List<User> sortUserList(List<User> userList) {

		List<User> temp = userList;

		Comparator<User> comparator = (u1, u2) -> u2.getUsername().compareTo(u1.getUsername());

		temp.sort(comparator);

		return temp;
	}

	/**
	 * Sorts a list of TimeReports by year, and then week. .
	 *  
	 * @param userList - The list to be sorted
	 * @return a sorted list.
	 */
	private List<TimeReport> sortTimeReports(List<TimeReport> userTimeReports) {

		List<TimeReport> temp = userTimeReports;

		Comparator<TimeReport> comparator = (tr1, tr2) -> tr2.getYear() - tr1.getYear();
		comparator = comparator.thenComparing((tr1, tr2) -> tr2.getWeek() - tr1.getWeek());

		temp.sort(comparator);

		return temp;

	}
	
	/**
	 * Calculates the total amount of time spent within a single timereport.
	 * 
	 * @param tr - The timereport to calculate time of
	 * @return an int containing total time spent
	 * @throws SQLException
	 */
	private int getTotalTimeReportTime(TimeReport tr) throws SQLException {

		int totalTime = 0;

		List<ActivityReport> activitiesInTimeReport = dbService.getActivityReports(tr.getTimeReportId());

		// calculate total time from all activity reports inside this timeReport
		for (ActivityReport ar : activitiesInTimeReport) {
			totalTime += ar.getMinutes();
		}
		return totalTime;
	}

	/**
	 * Builds a String containing HTML for showing a form for creating a
	 * activityreport.
	 * 
	 * @param week         - The week of the reported activity
	 * @param year         - The year of the reported activity
	 * @param timeReportId - The timereport in which the activityreport will lie
	 * @return A String containing the HTML described above
	 */
	private String activityReportForm(int week, int year, String timeReportId, HttpServletRequest req) {

		// If a timereport already exists, use that year
		try {
			TimeReport tr = dbService.getTimeReportById(Integer.parseInt(timeReportId));
			year = tr.getYear();
		} catch (Exception e) {
		}

		// Code for controlling so the user only can input valid information into the
		// date picker(same week and year as they selected)
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		LocalDate d = LocalDate.now().withYear(year).with(weekFields.weekOfYear(), week);
		LocalDate s = Helpers.getFirstDayOfWeek(d);
		LocalDate e = Helpers.getLastDayOfWeek(d);
		LocalDate p = LocalDate.now();

		if (e.compareTo(LocalDate.now()) > 0) {
			e = LocalDate.now();
		}

		if (e.compareTo(LocalDate.now()) < 0) {
			p = e;
		}

		List<ActivityType> activityTypes = dbService.getActivityTypes();
		List<ActivitySubType> activitySubTypes = dbService.getActivitySubTypes();
		Set<String> uniqueSubTypes = new TreeSet<String>();
		Set<Integer> activityTypesWithSubTypes = new HashSet<Integer>();

		for (ActivitySubType ast : activitySubTypes) {
			uniqueSubTypes.add(ast.getSubType());
			activityTypesWithSubTypes.add(ast.getActivityTypeId());
		}

		// Construct an JS array containing the ids of activity types which have sub
		// types
		String jsArray = "[";
		for (int i : activityTypesWithSubTypes) {
			jsArray += i + ",";
		}
		jsArray = jsArray.substring(0, jsArray.length() - 1);
		jsArray += "]";

		// Builds the HTML String
		String html = "<!--square.html-->\r\n" + "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<div id= \"form\">"
				+ " <form id=\"filter_form\" method=\"get\">\r\n" + "                 Activity type\r\n"
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"act_picker_1\" name=\"activityTypeId\" form=\"filter_form\">\r\n"; // Activity
																													// picker

		for (ActivityType at : activityTypes) {
			html += "<option value=" + at.getActivityTypeId() + ">" + at.getType() + "</option>";
		}

		html += "                      </select>\r\n" + "                </div>\r\n"
				+ "            <div id=\"subTypes\">\r\n"
				+ "                <p class=\"descriptors\">Activity Subtype</p>\r\n" // Activity subtype picker
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"act_picker_2\" name=\"activitySubType\" form=\"filter_form\">\r\n";

		for (String subType : uniqueSubTypes) {
			html += "<option value=" + subType + ">" + subType + "</option>";
		}

		html += "                      </select>\r\n" + "                </div>\r\n" + "            </div>\r\n"
				+ "<script>"
				+ "const one = document.querySelector('#act_picker_1');const two = document.querySelector('#subTypes');" // Javascript
																															// for
																															// making
																															// subtype
																															// picker
																															// invisible
																															// if
																															// the
																															// picked
																															// Activity
																															// has
																															// no
																															// subtype
				+ "one.addEventListener('change', (event) => {" + "const pickedValue = event.target.value;" + "if (!"
				+ jsArray + ".includes((+pickedValue))) {" + "two.style.visibility = 'hidden';" + "} else {"
				+ "two.style.visibility = 'visible';" + "}" + "});" + "</script>"

				// Hidden values, submit button and time input field
				+ "                <p class=\"descriptors\">Timespent(minutes) </p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n" + "				</div>"
				+ "              <input class=\"credentials_rect\" type=\"number\" id=\"timeSpent\" name=\"timeSpent\" min=\"1\" max="
				+ addQuotes(Constants.MAX_MINUTES_PER_DAY + "")
				+ " pattern=\"^[0-9]*$\" title=\"Please enter numbers only.\" maxlength=\"4\" placeholder=\"Timespent\" required><br>\r\n"
				+ "		<input name=\"addReportWeek\" type=\"hidden\" value=\"" + week + "\"></input>\r\n" // Hidden
																											// values
																											// that get
																											// sent into
																											// URL
				+ "<input name=\"addReportYear\" type=\"hidden\" value=\"" + year + "\"> </input>\r\n"
				+ "  <label for=\"dateInfo\">Enter date for activity: </label>\r\n"
				+ "<input type=\"date\" id=\"dateOfReport\" name=\"dateOfReport\" value=\"" + p + "\" min=\"" + s
				+ "\" max=\"" + e + "\">\r\n" + " <input name=\"timeReportId\" type=\"hidden\" value=\"" + timeReportId
				+ "\"></input>\r\n" + "              <input class=\"submitBtn\" type=\"submit\" value=\"Send\">\r\n"
				+ "                </div>\r\n" + "              </form>" + "</div>" + "              </html>";

		return html;

	}

	/**
	 * Checks if the logged in user is a projectleader in the selected project.
	 * 
	 * @param req - HttpServletRequest.
	 * @return true if the user is a projectleader, else false.
	 */
	private boolean isProjectLeader(HttpServletRequest req) {
		return this.isProjectLeader(req, this.getProjectId(req));
	}

	/**
	 * Checks if the user sent in is the logged in user.
	 * 
	 * @param req - HttpServletRequest.
	 * @param user - User to check .
	 * @return true if the user is the logged in user, else false.
	 */
	private boolean isUserLoggedInUser(HttpServletRequest req, User user) {
		User loggedInUser = getLoggedInUser(req);
		return loggedInUser != null && loggedInUser.getUserId() == user.getUserId();
	}

	/**
	 * Checks if the userId sent in is the logged in user.
	 * 
	 * @param req - HttpServletRequest.
	 * @param userId - UserId to check.
	 * @return true if the user is the logged in user, else false.
	 */
	private boolean isUserIdLoggedInUser(Integer userId, HttpServletRequest req) {
		User loggedInUser = getLoggedInUser(req);
		return loggedInUser != null && userId != null && loggedInUser.getUserId() == userId;
	}
}
