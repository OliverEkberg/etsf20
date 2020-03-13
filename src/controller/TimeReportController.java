package controller;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import baseblocksystem.servletBase;
import database.ActivityReport;
import database.ActivitySubType;
import database.ActivityType;
import database.DatabaseService;
import database.TimeReport;
import database.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TimeReportController
 * 
 * A xx page.
 * 
 * Description of the class.
 * 
 * @author Linus, Sebastian, Andre
 *         
 * @version 1.0
 * 
 */

// wtf is this i try to fix but it never work

@WebServlet("/TimeReportPage")
public class TimeReportController extends servletBase {

	DatabaseService dbService;

	public TimeReportController() {
		super();
		try {
			this.dbService = new DatabaseService();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	@Override
	/**
	 * Handles all logic for sending the user between different timereporting pages.	 * 
	 * 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			PrintWriter out = resp.getWriter();
			User loggedInUser = this.getLoggedInUser(req);	
			
			

			String activityType = req.getParameter("activity");
			String addReportWeek = req.getParameter("addReportWeek");
			String addReportYear = req.getParameter("addReportYear");
			String dateOfReport = req.getParameter("dateOfReport");
			String deleteActivityReportId = req.getParameter("deleteActivityReportId");
			String deleteTimeReportId = req.getParameter("deleteTimeReportId");
			String error = req.getParameter("error");
			String getReportsWeek = req.getParameter("getReportsWeek");	
			String getReportsYear = req.getParameter("getReportsYear");
			String showAllUnsignedReports = req.getParameter("showAllUnsignedReports");
			String showAllUsers = req.getParameter("showAllUsers");
			String showUserPage = req.getParameter("showUserPage");
			String subType = req.getParameter("subType");
			String timeReportFinishedId = req.getParameter("timeReportFinishedId");
			String timeReportId = req.getParameter("timeReportId");
			String timeReportNotFinishedId = req.getParameter("timeReportNotFinishedId");
			String timeReportSignId = req.getParameter("timeReportIdToSign");
			String timeReportUnsignId = req.getParameter("timeReportIdToUnsign");
			String timeSpent = req.getParameter("timeSpent");

			out.println(getHeader(req));
			out.println("<body>");
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/TimeReportController.css\">\n");
			out.println(getNav(req));
			out.println("<p id=\"report_title_text\">Reports</p>");
			
			if (loggedInUser == null) {
				resp.sendRedirect("/BaseBlockSystem/SessionPage");
			}
			
			else if(loggedInUser.isAdmin()) {
				resp.sendRedirect("/BaseBlockSystem/SessionPage");
			}
			
			if (getProjectId(req) == 0) {
				out.print("<p>Please choose a project first!</p>");
				return;	
			}
			
			LocalDate d = LocalDate.now();
			TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(); 
			int weekNumber = d.get(woy);

			if(error != null) { //If user gets an error
				out.println("<script> "
						+ "		alert('"+ error + " ')"
						+ "</script>");
			}

			if(addReportWeek != null && addReportYear != null && Integer.parseInt(addReportYear) == LocalDate.now().getYear() && Integer.parseInt(addReportWeek) > weekNumber) {

				resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=cant-create-timereport-in-the-future");
				return;
			}

			//Parameters for creating a new activityreport 
			if(activityType != null && subType != null && timeSpent != null && addReportWeek != null && addReportYear != null && timeReportId != null && dateOfReport != null) {			

				if(Integer.parseInt(timeSpent) == 0 || Integer.parseInt(timeSpent) > 1440) { 

					resp.sendRedirect("/BaseBlockSystem/TimeReportPage?time-can-only-be-a-number-between-1-and-1440");
					return;

				}

				int activityTypeId = 0;
				int activitySubTypeId = 0;
				ActivityReport activityReport;
				LocalDate date = LocalDate.parse(dateOfReport);
				activityTypeId = Integer.parseInt(activityType);

				List<ActivitySubType> subTypeList = dbService.getActivitySubTypes(activityTypeId);
				for(ActivitySubType ast : subTypeList) {

					//find subtype of activity
					if(ast.getSubType().equals(subType))
					{
						activitySubTypeId = ast.getActivitySubTypeId();
					}
				}			

				activityReport = createActivityReport( activityTypeId, activitySubTypeId, date, Integer.parseInt(addReportYear),  Integer.parseInt(addReportWeek),  
						Integer.parseInt(timeSpent),  loggedInUser.getUserId(),  this.getProjectId(req), resp); 

				if(activityReport == null) {
					resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=activity-report-could-not-be-created");
					return;
				}


				TimeReport timereport = dbService.getTimeReportById(activityReport.getTimeReportId()); //get timereport		
				out.print(getActivityReports(timereport.getTimeReportId(), req)); //Returns to the view of all activityreports for that timereport

				return;
			}

			//Parameters for showing activity report form
			if(addReportYear != null && addReportWeek != null && timeReportId != null && activityType == null && subType == null) {
				out.print(activityReportForm(Integer.parseInt(addReportWeek), Integer.parseInt(addReportYear), timeReportId));
				return;
			}

			//Parameters for deleting an activityreport
			if(deleteActivityReportId != null && timeReportId != null) {

				try {
					dbService.deleteActivityReport(Integer.parseInt(deleteActivityReportId));
				}
				catch(Exception e) {}
				out.print(getActivityReports(Integer.parseInt(timeReportId), req));
				return;


			}

			//Parameters for retriving all timereports for a specific week and year
			if(getReportsWeek != null && getReportsYear != null) {

				out.print(this.getTimereportsByWeekAndYear(Integer.parseInt(getReportsWeek), Integer.parseInt(getReportsYear), req));
				return;
			}

			//Shows the timereportingpage of a specific user
			if(showUserPage != null) {
				User user = dbService.getUserById(Integer.parseInt(showUserPage));
				out.print(getUserTimeReports(user, req));
				return;
			}

			//Shows a page of all users, with links to their timereport pages
			if(showAllUsers != null) {
				out.print(showAllUsers(req));
				return;
			}

			//Projectleaders signs a timereport
			if(timeReportSignId != null) {

				if(this.isProjectLeader(req, this.getProjectId(req))) {
					TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportSignId));
					int projectUserId = dbService.getProjectUserIdByUserIdAndProjectId(loggedInUser.getUserId(), this.getProjectId(req));
					timeReport.sign(projectUserId);
					dbService.updateTimeReport(timeReport);
					out.println(getUserTimeReports(loggedInUser, req));
					return;
				}

				else {
					resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=only-a-projectleader-can-sign-a-timereport");
				}
			}

			//Proect leader unsigns a timereport
			if(timeReportUnsignId != null) {
				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportUnsignId));
				timeReport.unsign();

				dbService.updateTimeReport(timeReport);
				out.println(getUserTimeReports(loggedInUser, req));
				return;
			}


			if(showAllUnsignedReports != null) {

				out.println(getUnsignedTimeReports(req, resp));
				return;
			}

			//User marks timereport as not finished
			if(timeReportNotFinishedId != null) {

				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportNotFinishedId));
				timeReport.setFinished(false);
				dbService.updateTimeReport(timeReport);
				out.println(getUserTimeReports(loggedInUser, req));
				return;			
			}

			//user marks timereport as finished
			if(timeReportFinishedId != null) {

				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportFinishedId));
				timeReport.setFinished(true);
				dbService.updateTimeReport(timeReport);
				out.println(getUserTimeReports(loggedInUser, req));
				return;
			}

			//User deletes one of their own timereports
			if(deleteTimeReportId != null) {

				try{
					dbService.deleteTimeReport(Integer.parseInt(deleteTimeReportId));
				}
				catch(Exception e) {}
				out.print(getUserTimeReports(loggedInUser, req));
				return;
			}

			//user adds report
			if(addReportWeek != null && addReportYear != null) {

				int addReportWeekInt = Integer.parseInt(addReportWeek);

				if(addReportWeekInt > 0 && addReportWeekInt <= 53) {
					out.print(activityReportForm(Integer.parseInt(addReportWeek), Integer.parseInt(addReportYear), ""));
					return;
				}
				else {
					resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=cant-create-timereport-in-the-future-or-before-week-0");
				}

			}


			//Get activityreports for a specific timereport
			if(timeReportId != null) {

				out.print(getActivityReports(Integer.parseInt(timeReportId), req));			
				return;
			}

			out.println(getUserTimeReports(loggedInUser, req)); //Standard case, if nothing else works this is called


		}

		catch (NumberFormatException e) {
			resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=unexpected-error");
			e.printStackTrace(); 
		} catch (Exception e) {
			resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=unexpected-error");
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Creates a new Activityreport and links it to an existing Timereport for the same week. If no Timereport exists for the given week, one is created
	 * 
	 * @param activityTypeId - The int value of the activity type
	 * @param activitySubTypeId - The int value of the activity subtype
	 * @param date - The date the activityreport was created
	 * @param year - The year of the activity
	 * @param week - The week of the activity
	 * @param minutes - The amount of minutes spent on the activity
	 * @param userId - The id of the user who creates the activityreport
	 * @param projectId - The id of the project in which the activityreport was created.
	 * @param resp - HttpServletResponse
	 * @return - The newly created activityreport
	 * @throws Exception
	 */
	private ActivityReport createActivityReport(int activityTypeId, int
			activitySubTypeId, LocalDate date, int year, int week, int minutes, int userId, int projectId, HttpServletResponse resp) throws Exception {

		TimeReport timereport = null;
		ActivityReport activityReport = null;
		int projectUserId = dbService.getProjectUserIdByUserIdAndProjectId(userId, projectId);

		if(dbService.hasTimeReport(week, year, userId, projectId)) {// Does timereport this week and year exist?

			List<TimeReport> allReports = dbService.getTimeReportsByUserAndProject(userId, projectId);	
			for(TimeReport tr : allReports) { 
				if(tr.getWeek() == week && tr.getYear() == year) { //Find timereport for this week and year amongst all timereports
					timereport = tr;

					List<ActivityReport> activityReports = dbService.getActivityReports(tr.getTimeReportId());	//TODO: Anvand smidigare databasfunktion 				
					int totalDateTime = 0;

					for(ActivityReport ar : activityReports) { // Calculate if the activity will overstep the amount of minutes in a day. 

						if(ar.getReportDate().equals(date)) {
							totalDateTime += ar.getMinutes();
						}
					}

					if(totalDateTime + minutes > 1440) {
						resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=total-amount-of-minutes-surpasses-maximum-daily-limit");
						return null;
					}

					if(tr.isFinished() || tr.isSigned()) {
						resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=timereport-is-signed-or-marked-as-ready-for-signing,-and-cant-be-edited.");
						return null;
					}
				}
			}
		}
		else { //Else - timereport this week and year didnt exist, create one!
			timereport = dbService.createTimeReport(new TimeReport(0, projectUserId, 0, null, year, week, LocalDateTime.now(), false)); 
		}

		activityReport = dbService.createActivityReport(new ActivityReport(0, activityTypeId, activitySubTypeId, timereport.getTimeReportId(), date, minutes));

		return activityReport;

	}
	
	/**
	 * Creates a String containing a HTML page with all users with a link to their corresponding timereport lists
	 * 
	 * @param req - HttpServletRequest
	 * @return A String containing HTML to show the page described above.
	 * @throws SQLException
	 */
	private String showAllUsers(HttpServletRequest req) throws SQLException {

		List<User> userList = dbService.getAllUsers(this.getProjectId(req));

		String html = "<table id=\"report-table\" width=\"600\" border=\"2\">\r\n" + "<tr>\r\n" + "<td> Username </td>\r\n"
				+ "<td> View users timereports </td>\r\n"+ 
				"</td>\r\n";

		for(User u : userList) {

			html +=   "<tr>\r\n" 
					+ "<td>" + u.getUsername()+ "</td>\r\n"+
					"<td> <form action=\"TimeReportPage?showUserPage="+u.getUserId()+"\" method=\"get\"> "
					+ "<button name=\"showUserPage\" type=\"submit\" value=\"" +u.getUserId() + "\"> Select </button> </form> </td> \r\n";
		}

		html += "</tr>\r\n" + "</table>"; //Ends the HTML table

		return html;

	}
	/**
	 * Builds a String containing a HTML page showing all timreports within a specific project, week and year.
	 * 
	 * @param week - Specified week to get reports from
	 * @param year - Specified year to get reports from
	 * @param req - HttpServletRequest
	 * @return A String containing HTML to show the page described above.
	 * @throws SQLException
	 */
	private String getTimereportsByWeekAndYear(int week, int year, HttpServletRequest req) throws SQLException {

		String html = "";

		List<TimeReport> timeReportList = dbService.getTimeReportsByProject(this.getProjectId(req));

		//Table start
		html += "<table id=\"report-table\" width=\"400\" border=\"2\">\r\n" 
				+ "<tr>\r\n" 
				+ "<th> Year </th>\r\n"
				+ "<th> Week </th>\r\n"
				+ "<th> Username </th>\r\n"
				+ "<th> Timespent(minutes) </th>\r\n" 
				+ "<th> Status </th>\r\n" 
				+ "<th> Select Timereport </th>\r\n"
				+ "<th> Remove Timereport </th>\r\n";

		for (TimeReport tr : timeReportList) {

			//Check if timereport is from the correct week and year.
			if(tr.getWeek() == week && tr.getYear() == year) { 

				int timeReportTotalTime = getTotalTimeReportTime(tr);
				String signed;
				String reportOwner = dbService.getUserByTimeReportId(tr.getTimeReportId()).getUsername();

				if (tr.isSigned()) { // get isSigned or not
					signed = "Signed";
				} else {
					signed = "Unsigned";
				}


				//Fills HTML table with user info
				html += "<tr>\r\n" + "<td>" + tr.getYear() + "</td>\r\n" +
						"<td>" + tr.getWeek() + "</td>\r\n"+
						"<td>" + reportOwner + "</td>\r\n"+
						"<td>" + timeReportTotalTime + "</td>\r\n" + "<td>" + signed + "</td>\r\n"
						+ "<td> <form action=\"TimeReportPage?timeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
						+ "<button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() 
						+ "\"> Select </button>  </form> </td> \r\n";

				html += "</tr>\r\n";
			}

		}

		//END OF TABLE
		html += "</tr>\r\n" + "</table>";

		return html;

	}

	/**
	 * Builds a String containing a HTML page showing all Activityreports within the specified timereport.
	 * 
	 * @param timeReportId - The id of the timereport which you want to access.
	 * @param req - HttpServletRequest
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

		//If projectleader is looking within a report, and it isn't their own. Display the name of the report owner.
		if(this.isProjectLeader(req) && !isUserLoggedInUser(reportOwner, req)) {
			html += "<body> <b> "+ reportOwner.getUsername() + "</b> <br> </body>\r\n";
		}

		//HTML table start and header.
		html +=  "<table width=\"600\" border=\"2\">\r\n" 
				+ "<tr>\r\n" 
				+ "<td> Date </td>\r\n"
				+ "<td> Activitytype</td>\r\n" + "<td> Subtype </td>\r\n" + "<td> Minutes </td>\r\n"
				+ "<td> Remove activity report </td>\r\n";

		List<ActivityReport> activityReports = dbService.getActivityReports(timeReportId);
		List<ActivityType> activityTypes = dbService.getActivityTypes();
		List<ActivitySubType> activitySubTypes = dbService.getActivitySubTypes();

		String activityType;
		String activitySubType;

		//For every activityreport, add its info and required buttons to the HTML table
		for (ActivityReport aReport : activityReports) {

			activityType = getActivityType(aReport, activityTypes);
			activitySubType = getActivitySubType(aReport, activitySubTypes);


			html +=   "<tr>\r\n" 
					+ "<td>" + aReport.getReportDate().toString() + "</td>\r\n"
					+ "<td>" + activityType + "</td>\r\n" 
					+ "<td>" + activitySubType + "</td>\r\n" 
					+ "<td>" + aReport.getMinutes() + "</td>\r\n";

			//If timereport isn't signed, and the report owner is the one accessing it, show button for deleting activity, else dont show it.			
			if(!reportIsSigned && !reportIsFinished && isUserLoggedInUser(reportOwner, req)) {
				html += "<td> <form action=\"TimeReportPage?deleteActivityReportId=\""+aReport.getActivityReportId()+"&timeReportId=\"" + timeReportId + "\" method=\"get\">\r\n" + 
						"		<input name=\"deleteActivityReportId\" type=\"hidden\" value=\""+aReport.getActivityReportId()+"\"></input>\r\n" + 
						" <input name=\"timeReportId\" type=\"hidden\" value=\""+timeReportId+"\"></input>\r\n" + 
						"		<input type=\"submit\" value=\"Remove\"></input>\r\n" + 
						"	</td> \r\n"
						+ "</form>";

			}


		}

		//Ends the HTML table
		html += "</tr>\r\n" + "</table>"; 

		//If projectleader is looking at timereports, show sign/unsign buttons or that the report isn't ready to be signed
		if(isProjectLeader)	{ 

			if(reportIsSigned) {
				html += "<form method=\"get\"> <button name=\"timeReportIdToUnsign\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
				+ "\"> Unsign </button>  </form> \r\n" ;
			}

			else if(!reportIsSigned && reportIsFinished) {
				html += "<form method=\"get\"> <button name=\"timeReportIdToSign\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
				+ "\"> Sign </button>  </form> \r\n";
			}

			else if(!reportIsSigned && !reportIsFinished) {
				html += "<body> Report is not marked as ready for signing </body> \r\n";
			}
		}

		//If timereport owner is the one logged in and looking at this screen AND isnt marked as finished			
		if(isUserLoggedInUser(reportOwner, req) && !timeReport.isFinished() && !timeReport.isSigned()) {

			//Show button for adding activity	
			html += "<form action=\"TimeReportPage?week=\""+timeReport.getWeek()+"&timeReportId=\"" + timeReportId + "\"&addReportYear=\"" + timeReport.getYear() + "\" method=\"get\">\r\n" +  
					"		<input name=\"addReportWeek\" type=\"hidden\" value=\""+timeReport.getWeek()+"\"></input>\r\n" + 
					" <input name=\"timeReportId\" type=\"hidden\" value=\""+timeReportId+"\"></input>\r\n" + 
					" <input name=\"addReportYear\" type=\"hidden\" value=\""+timeReport.getYear()+"\"></input>\r\n" + 
					"		<input type=\"submit\" value=\"Add new activity\"></input>\r\n" + 
					"	</form>";

			//Button - Mark activity report as finished
			html +=	"<td> <form action = \"TimeReportPage?timeReportFinishedId=\""+timeReport.getTimeReportId()+"\" method=\"get\"> <button name=\"timeReportFinishedId\" type=\"submit\" value=\"" 
					+ timeReport.getTimeReportId() 
					+ "\"> Mark timereport as ready for signing </button>  </form> \r\n";										
		}

		//If timerport owner is the one logged in and looking at this screen AND timereport IS! marked and finished and not signed.
		else if(isUserLoggedInUser(reportOwner, req) && timeReport.isFinished() && !timeReport.isSigned()) { 

			//Button - Unmark activity report as finished
			html +=	"<td> <form action = \"TimeReportPage?timeReportNotFinishedId=\""+timeReport.getTimeReportId()+"\" method=\"get\"> <button name=\"timeReportNotFinishedId\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
			+ "\"> Unmark </button>  </form> \r\n";		
		}

		return html;
	}


	/**
	 * Retrives a String representation of a activity from a specific activity report.
	 * 
	 * @param activityReport - The specified activityreport.
	 * @param typeList - A list containing all possible activity types
	 * @return a String representation of the activity.
	 * @throws Exception
	 */
	private String getActivityType(ActivityReport activityReport, List<ActivityType> typeList) throws Exception {

		//Get activity type for current activity report
		for(ActivityType aType: typeList) { 

			if(aType.getActivityTypeId() == activityReport.getActivityTypeId()) {
				return aType.getType();
			}
		}

		return"";
	}

	/**
	 * Retrives a String representation of a subactivity from a specific activity report.
	 * 
	 * @param activityReport - The specified activityreport.
	 * @param subTypeList - A list containing all possible activity subtypes
	 * @return a String representation of the activity subtype.
	 * @throws Exception
	 */
	private String getActivitySubType(ActivityReport activityReport, List<ActivitySubType> subTypeList) throws Exception{

		for (ActivitySubType aSubType : subTypeList) { // Get activity type for current activity report

			if (aSubType.getActivitySubTypeId() == activityReport.getActivitySubTypeId()) { 

				return aSubType.getSubType();
			}
		}
		return "";
	}

	/**
	 * Builds a String containing a HTML page showing all timereports for a specific user inside the selected project.
	 * 
	 * @param user - The user of which you want to retrieve timereports from.
	 * @param req - HttpServletRequest
	 * @return A String containing HTML to show the page described above. 
	 * @throws Exception
	 */
	private String getUserTimeReports(User user, HttpServletRequest req) throws Exception {

		String html = "";

		List<TimeReport> userTimeReports = dbService.getTimeReportsByUserAndProject(user.getUserId(), this.getProjectId(req));

		//If the projectleader is looking at this page, and its not his own timeport. Show the name of the report owner!
		if(this.isProjectLeader(req) && !isUserLoggedInUser(user, req)) {
			html += "<body> <b> "+ user.getUsername() + "</b> <br> </body>\r\n";
		}

		//if there are no timereports for the specified user.
		if(userTimeReports.isEmpty()) {
			html += "<body> No timereports exist for the selected user </body>\r\n";
		}

		//Else start building the HTML table
		else {

			//Html table start and header
			html += "<table width=\"600\" border=\"2\">\r\n" 
					+ "<tr>\r\n" 
					+ "<td> Year </td>\r\n"
					+ "<td> Week </td>\r\n"
					+ "<td> Timespent (minuter) </td>\r\n" 
					+ "<td> Status </td>\r\n" 
					+ "<td> Select timereport </td>\r\n"
					+ "<td> Remove timereport </td>\r\n";

			//Adds all timereports into the table
			for (TimeReport tr : userTimeReports) {

				int timeReportTotalTime = getTotalTimeReportTime(tr);
				String signed;

				//get String representation of signed/unsigned
				if (tr.isSigned()) {
					signed = "Signed";
				} else {
					signed = "Unsigned";
				}

				//Add timereport information into table
				html += "<tr>\r\n" + "<td>" + tr.getYear() + "</td>\r\n" +
						"<td>" + tr.getWeek() + "</td>\r\n"+
						"<td>" + timeReportTotalTime + "</td>\r\n" + 
						"<td>" + signed + "</td>\r\n"
						+ "<td> <form action=\"TimeReportPage?timeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
						+ "<button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() 
						+ "\"> Select </button>  </form> </td> \r\n";

				//If timereport isn't signed, and the report owner is the one browsing, a button for deleting it should be visible
				if(!tr.isSigned() && isUserLoggedInUser(user, req)) { 
					html += "<td> <form action=\"TimeReportPage?deleteTimeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
							+ "<button name=\"deleteTimeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() + "\"> Remove </button> </form> </td> \r\n";
				}

				html += "</tr>\r\n";

			}
		}
		//END OF TABLE
		html += "</tr>\r\n" + "</table>";

		//Adds more buttons and options underneath the table containing timereports
		try {

			LocalDate d = LocalDate.now();

			//If the logged in user is the one browsing this page, give the option to create a new timereport
			if(isUserLoggedInUser(user, req)) { 
				html += "<!--square.html-->\r\n" + 
						"<!DOCTYPE html>\r\n" + 
						"<html>\r\n" +
						"<div id=\"form\">"
						+ " <form id=\"filter_form\" method=\"get\">\r\n" 
						+ "             Create timereport for: \r\n"
						+ "                <div id=\"selectWeek\">\r\n"
						+ "                    <select id=\"addReportWeek\" name=\"addReportWeek\" form=\"filter_form\">\r\n";
				//Week selection dropdown list
				for(int i = 1; i < 54; i++) {

					html += "<option value=" + i + ">Week: " + i + " </option>\r\n";
				}

				html += "</select>\r\n  </div>\r\n"
						+ "<div id=\"selectYear\">\r\n"
						+ "                    <select id=\"addReportYear\" name=\"addReportYear\" form=\"filter_form\">\r\n";
				//Year selection dropdown list
				for(int i = 2020; i <= d.getYear(); i++) {	
					html += "                        <option value="+i+">Year: "+i+"</option>\r\n";
				}

				//Adds "Create" button
				html += "            </select>\r\n" 
						+ "              </div>\r\n"						
						+ "            </div>\r\n"
						+ "			  <input type=\"submit\" value=\"Create timereport\" >\r\n"
						+ "           </form>" +
						"</div>"
						+ "          </html>";
			}

			/*If projectleader is the one browsing, add buttons for seeing all users, all unsigned timereports 
				and browsing all timereports for a specific week and year
			 */
			if(isProjectLeader(req)) {			

				//Get reports for week and year
				html += "<!--square.html-->\r\n" + 
						"<!DOCTYPE html>\r\n" + 
						"<html>\r\n" +
						"<div id=\"form\">"
						+ " <form id=\"getAllReports\" method=\"get\">\r\n" 
						+ "          Get all timereports for this project for: \r\n"
						+ "                <div id=\"selectWeek\">\r\n"
						+ "                    <select id=\"getReportsWeek\" name=\"getReportsWeek\" form=\"getAllReports\">\r\n";

				//Week dropdown list
				for(int i = 1; i < 54; i++) {

					html += "<option value=" + i + ">Week: " + i + " </option>\r\n";
				}

				html += "</select>\r\n  </div>\r\n"
						+ "<div id=\"selectYear\">\r\n"
						+ "                    <select id=\"getReportsYear\" name=\"getReportsYear\" form=\"getAllReports\">\r\n";
				//Year dropdown list
				for(int i = 2020; i <= d.getYear(); i++) {	
					html += "                        <option value="+i+">Year: "+i+"</option>\r\n";
				}

				//Button for retrieving timereports.
				html += "            </select>\r\n" 
						+ "              </div>\r\n"						
						+ "            </div>\r\n"
						+ "			  <input type=\"submit\" value=\"Get timereports\" >\r\n"
						+ "           </form>" +
						"</div>"
						+ "          </html>";

				//Button for showing all unsigned reports and showing all users.
				html += "<form action=\"TimeReportPage?showAllUnsignedReports\" metod=\"get\">\r\n" + 
						"  <input name=\"showAllUnsignedReports\" type=\"submit\" value=\"Show all unsigned timereports\" >\r\n" + 
						"</form>\r\n"+
						"<form action=\"TimeReportPage?showAllUsers\" metod=\"get\">\r\n" + 
						"  <input name=\"showAllUsers\" type=\"submit\" value=\"Show all users\" >\r\n" + 
						"</form>"
						+ "<br>" +
						"</div>";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return html;

	}
	/**
	 * Builds a String containing a HTML page showing all timereports for a specific user inside the selected project.
	 * 
	 * @return A String containing HTML to show the page described above. 	
	 * @throws SQLException
	 */
	private String getUnsignedTimeReports(HttpServletRequest req, HttpServletResponse resp) throws SQLException { 

		String html= "";

		try {
			if(!isProjectLeader(req)) {
				resp.sendRedirect("/BaseBlockSystem/TimeReportPage?only-a-projectleader-should-be-able-to-access-this-view");
			}



			List <TimeReport> allTimeReports = dbService.getTimeReportsByProject(this.getProjectId(req)); 
			List <TimeReport> unsignedTimeReports = new ArrayList<TimeReport>();

			//Sorts out so we get a list with only unsigned timereports
			for(TimeReport tr : allTimeReports) {

				if(!tr.isSigned()) {
					unsignedTimeReports.add(tr);
				}
			}

			//if there are none, write it out
			if(unsignedTimeReports.isEmpty()) {

				html += "<body> There are no unsigned timereports in the project!</body>";
			}

			//HTML table init and head
			html = "<table id=\"report-table\" width=\"400\" border=\"2\">\r\n" 
					+ "<tr>\r\n" 
					+ "<th> Week </th>\r\n"
					+ "<th> Username </th>\r\n"
					+ "<th> Timespent(minutes) </th>\r\n" 
					+ "<th> Status </th>\r\n" 
					+ "<th> Select timereport </th>\r\n";

			//For all unsigned timereports, add info to table
			for (TimeReport tr : unsignedTimeReports) {

				int timeReportTotalTime = getTotalTimeReportTime(tr);
				String signed;

				if (tr.isSigned()) {
					signed = "Signed";
				} else {
					signed = "Unsigned";
				}

				User trOwner = dbService.getUserByTimeReportId(tr.getTimeReportId());


				html += "<tr>\r\n" + "<td>" + tr.getWeek() + "</td>\r\n" +
						"<td>" + trOwner.getUsername() + "</td>\r\n" +
						"<td>" + timeReportTotalTime + "</td>\r\n"
						+ "<td>" + signed + "</td>\r\n" //Should be "Ej signerad" for all reports
						+ "<td> <form method=\"get\"> <button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() + "\"> Select </button> </form> \r\n"
						+ "</td>\r\n" + "</tr>\r\n";

			}

			html += "</tr>\r\n" + "</table>"; // END HTML	


		} catch (Exception e) {
			e.printStackTrace();
		}

		return html;	
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

		//calculate total time from all activity reports inside this timeReport
		for (ActivityReport ar : activitiesInTimeReport) { 
			totalTime += ar.getMinutes();
		}
		return totalTime;
	}

	/**
	 * Builds a String containing HTML for showing a form for creating a activityreport.
	 * 
	 * @param week - The week of the reported activity
	 * @param year - The year of the reported activity
	 * @param timeReportId - The timereport in which the activityreport will lie
	 * @return A String containing the HTML described above
	 */
	private String activityReportForm(int week, int year, String timeReportId) {

		//If a timereport already exists, use that year
		try {
			TimeReport tr = dbService.getTimeReportById(Integer.parseInt(timeReportId));
			year = tr.getYear();
		} catch (Exception e) {}

		//Code for controlling so the user only can input valid information into the date picker(same week and year as they selected)
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		LocalDate d = LocalDate.now().withYear(year).with(weekFields.weekOfYear(), week);
		LocalDate s = d.minusDays(d.getDayOfWeek().getValue() - 1);
		LocalDate e = d.plusDays(7 - d.getDayOfWeek().getValue());
		LocalDate p = LocalDate.now();

		if (e.compareTo(LocalDate.now()) > 0) {
			e = LocalDate.now();
		}

		if (e.compareTo(LocalDate.now()) < 0) {
			p = e;
		}


		//Builds the HTML String
		return "<!--square.html-->\r\n" + 
		"<!DOCTYPE html>\r\n" + 
		"<html>\r\n" +
		"<div id= \"form\">"
		+ " <form id=\"filter_form\" method=\"get\">\r\n" + "                 Activity type\r\n"
		+ "                <div id=\"activity_picker\">\r\n"
		+ "                    <select id=\"act_picker_1\" name=\"activity\" form=\"filter_form\">\r\n" //Activity picker
		+ "                        <option value=11>SDP</option>\r\n"
		+ "                        <option value=12>SRS</option>\r\n"
		+ "                        <option value=13>SVVS</option>\r\n"
		+ "                        <option value=14>STLDD</option>\r\n"
		+ "                        <option value=15>SVVI</option>\r\n"
		+ "                        <option value=16>SDDD</option>\r\n"
		+ "                        <option value=17>SVVR</option>\r\n"
		+ "                        <option value=18>SSD</option>\r\n"
		+ " 					   <option value=19>Slutrapport</option>\r\n"
		+ "                        <option value=21>Funktionstest</option>\r\n"
		+ "                        <option value=22>Systemtest</option>\r\n"
		+ "                        <option value=23>Regressionstest</option>\r\n"
		+ "                        <option value=30>Mote</option>\r\n"
		+ "                        <option value=41>F�rel�sning</option>\r\n"
		+ "                        <option value=42>�vning</option>\r\n"
		+ "                        <option value=43>Terminal�vning</option>\r\n"
		+ "                        <option value=44>Sj�lvstudier</option>\r\n"
		+ "                        <option value=100>�vrigt</option>\r\n"
		+ "                      </select>\r\n" + "                </div>\r\n"
		+ "            <div id=\"subTypes\">\r\n" + "                <p class=\"descriptors\">Activity Subtype</p>\r\n" //Activity subtype picker
		+ "                <div id=\"activity_picker\">\r\n"
		+ "                    <select id=\"act_picker_2\" name=\"subType\" form=\"filter_form\">\r\n"
		+ "                        <option value=\"U\">U</option>\r\n"
		+ "                        <option value=\"O\">O</option>\r\n"
		+ "                        <option value=\"I\">I</option>\r\n"
		+ "                        <option value=\"F\">F</option>\r\n"
		+ "                      </select>\r\n" + "                </div>\r\n" + "            </div>\r\n"
		+ "<script>"
		+ "const one = document.querySelector('#act_picker_1');const two = document.querySelector('#subTypes');" //Javascript for making subtype picker invisible if the picked Activity has no subtype
		+ "one.addEventListener('change', (event) => {"
		+ "const pickedValue = event.target.value;"
		+ "if (pickedValue > 19) {"
		+ "two.style.visibility = 'hidden';"
		+ "} else {"
		+ "two.style.visibility = 'visible';"
		+"}"
		+ "});"
		+ "</script>"

				//Hidden values, submit button and time input field
				+ "                <p class=\"descriptors\">Timespent(minutes) </p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n" + "				</div>"
				+ "              <input class=\"credentials_rect\" type=\"number\" id=\"timeSpent\" name=\"timeSpent\" min=\"1\" max=\"1440\" pattern=\"^[0-9]*$\" title=\"Please enter numbers only.\" maxlength=\"4\" placeholder=\"Timespent\" required><br>\r\n"
				+ "		<input name=\"addReportWeek\" type=\"hidden\" value=\""+ week + "\"></input>\r\n" //Hidden values that get sent into URL
				+"<input name=\"addReportYear\" type=\"hidden\" value=\""+ year + "\"> </input>\r\n"
				+ "  <label for=\"dateInfo\">Enter date for activity: </label>\r\n"  
				+ "<input type=\"date\" id=\"dateOfReport\" name=\"dateOfReport\" value=\"" + p + "\" min=\""+ s +"\" max=\""+ e+ "\">\r\n"	
				+ " <input name=\"timeReportId\" type=\"hidden\" value=\""+ timeReportId + "\"></input>\r\n"
				+ "              <input class=\"submitBtn\" type=\"submit\" value=\"Send\">\r\n" 				
				+ "                </div>\r\n"
				+ "              </form>" +
				"</div>"
				+ "              </html>";


	}		
	/**
	 * Checks if the logged in user is a projectleader in the selected project.
	 * 
	 * @param req - HttpServletRequest
	 * @return true if the user is a projectleader, else false.
	 * @throws Exception
	 */
	private boolean isProjectLeader(HttpServletRequest req) throws Exception {
		return this.isProjectLeader(req, this.getProjectId(req));
	}

	private boolean isUserLoggedInUser(User user, HttpServletRequest req) throws Exception {
		return user.getUserId() == this.getLoggedInUser(req).getUserId();
	}

}