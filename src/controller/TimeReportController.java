package controller;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

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
 * @author Linus, Sebastian, André
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

	
	  
	  private ActivityReport createActivityReport(int activityTypeId, int
	  activitySubTypeId, LocalDate date, int year, int week, int minutes, int userId, int projectId, HttpServletResponse resp) throws Exception {
	  
		TimeReport timereport = null;
		ActivityReport activityReport = null;
		int projectUserId = dbService.getProjectUserIdByUserIdAndProjectId(userId, projectId);
		  
		if(dbService.hasTimeReport(week, year, userId, projectId)) {// Does timereport this week exist?
			
			List<TimeReport> allReports = dbService.getTimeReportsByUserAndProject(userId, projectId);	
			for(TimeReport tr : allReports) { 
				if(tr.getWeek() == week && tr.getYear() == year) {
					timereport = tr;
					
					List<ActivityReport> activityReports = dbService.getActivityReports(tr.getTimeReportId());	//TODO: Använd smidigare databasfunktion 				
					int totalDateTime = 0;
					
					for(ActivityReport ar : activityReports) {
						
						if(ar.getReportDate().equals(date)) {
							totalDateTime += ar.getMinutes();
						}
					}
					
					if(totalDateTime + minutes > 1440) {
						resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=antal-minuter-passerar-daglig-maximal-inmatning");
						return null;
					}
					
					if(tr.isFinished() || tr.isSigned()) {
						new Exception("Tidrapport är signerad eller markerad som färdig och kan inte ändras!"); 
						return null;
					}
				}
			}
		}
		else {
			timereport = dbService.createTimeReport(new TimeReport(0, projectUserId, 0, null, year, week, LocalDateTime.now(), false)); 
			}

			activityReport = dbService.createActivityReport(new ActivityReport(0, activityTypeId, activitySubTypeId, timereport.getTimeReportId(), date, minutes));
 
		return activityReport;
		
	  }
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
		

			//TODO: ctrl + F "exception" behövs fixas
			//TODO: Ändra så att vi inte sätter den inloggade användaren själva
				
		PrintWriter out = resp.getWriter();
		setUserId(req,15); //USER ID 19 = PROJECTLEADER
		this.setIsLoggedIn(req, true);
		setProjectId(req, 1);	
		User loggedInUser = dbService.getUserById(15); // SKA VARA SEN this.getLoggedInUser(req);		

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
		
		
		LocalDate d = LocalDate.now();
		TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(); 
		int weekNumber = d.get(woy);
		
		if(error != null) {
			out.println("<script> "
					+ "		alert('"+ error + " ')"
					+ "</script>");
		}
		
		if(addReportWeek != null && addReportYear != null && Integer.parseInt(addReportYear) == LocalDate.now().getYear() && Integer.parseInt(addReportWeek) > weekNumber) {
			
			resp.sendRedirect("/BaseBlockSystem/TimeReportPage?error=kan-inte-skapa-raport-i-framtiden");
			return;
		}
		
		if(activityType != null && subType != null && timeSpent != null && addReportWeek != null && addReportYear != null && timeReportId != null && dateOfReport != null) {			
			
			if(Integer.parseInt(timeSpent) == 0 || Integer.parseInt(timeSpent) > 1440) { //Time spent måste vara värde mellan 1 och 1440 
				
				new Exception("Otillåtet värde för antal minuter");
				out.println(getUserTimeReports(loggedInUser, req));
				return;
				
			}
			
			int activityTypeId = 0;
			int activitySubTypeId = 0;
			ActivityReport activityReport;
			LocalDate date = LocalDate.parse(dateOfReport);
			

			activityTypeId = Integer.parseInt(activityType);
			
			List<ActivitySubType> subTypeList = dbService.getActivitySubTypes(activityTypeId);		//get activity subtypeID
			for(ActivitySubType ast : subTypeList) {
				
				if(ast.getSubType().equals(subType))
				{
					activitySubTypeId = ast.getActivitySubTypeId();
				}
			}			
			
			activityReport = createActivityReport( activityTypeId, activitySubTypeId, date, Integer.parseInt(addReportYear),  Integer.parseInt(addReportWeek),  
					Integer.parseInt(timeSpent),  this.getLoggedInUser(req).getUserId(),  this.getProjectId(req), resp); 
																													
			if(activityReport == null) {
				new Exception("Aktivitetrapport kunde inte skapas! (Signerad tidsrapport)");
				out.println(getUserTimeReports(loggedInUser, req)); //Standard case
				return;
			}
			
			
			TimeReport timereport = dbService.getTimeReportById(activityReport.getTimeReportId()); //get timereport		
			out.print(getActivityReports(timereport.getTimeReportId(), req)); //Returns to the view of all activityreports for that timereport
			
			return;
		}
		
		if(addReportYear != null && addReportWeek != null && timeReportId != null && activityType == null && subType == null) {
			out.print(activityReportForm(Integer.parseInt(addReportWeek), Integer.parseInt(addReportYear), timeReportId));
			return;
		}
		
		if(deleteActivityReportId != null && timeReportId != null) {
			
			try {
				dbService.deleteActivityReport(Integer.parseInt(deleteActivityReportId));
				}
			catch(Exception e) {
				
			}
			out.print(getActivityReports(Integer.parseInt(timeReportId), req));
			return;

			
		}
		
		if(getReportsWeek != null && getReportsYear != null) {
			
			out.print(this.getTimereportsByWeekAndYear(Integer.parseInt(getReportsWeek), Integer.parseInt(getReportsYear), req));
			return;
		}
		
		if(showUserPage != null) {
			User user = dbService.getUserById(Integer.parseInt(showUserPage));
			out.print(getUserTimeReports(user, req));
			return;
		}
		
		if(showAllUsers != null) {
			out.print(showAllUsers(req));
			return;
		}
		
		if(timeReportSignId != null) {
			
			if(this.isProjectLeader(req, this.getProjectId(req))) {
				TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportSignId));
				int projectUserId = dbService.getProjectUserIdByUserIdAndProjectId(this.getLoggedInUser(req).getUserId(), this.getProjectId(req));
				timeReport.sign(projectUserId);
				dbService.updateTimeReport(timeReport);
				out.println(getUserTimeReports(loggedInUser, req));
				return;
			}
			
			else {
			 new Exception("Endast användare med rollen projektledare kan signera en tidrapport");
			}
		}
		
		if(timeReportUnsignId != null) {
			TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportUnsignId));
			timeReport.unsign();
			
			dbService.updateTimeReport(timeReport);
			out.println(getUserTimeReports(loggedInUser, req));
			return;
		}
		
		if(showAllUnsignedReports != null) {
			
			out.println(getUnsignedTimeReports(req));
			return;
		}
		
		if(timeReportNotFinishedId != null) {
			
			TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportNotFinishedId));
			timeReport.setFinished(false);
			dbService.updateTimeReport(timeReport);
			out.println(getUserTimeReports(loggedInUser, req));
			return;			
		}
		
		if(timeReportFinishedId != null) {
			
			TimeReport timeReport = dbService.getTimeReportById(Integer.parseInt(timeReportFinishedId));
			timeReport.setFinished(true);
			dbService.updateTimeReport(timeReport);
			out.println(getUserTimeReports(loggedInUser, req));
			return;
		}
		
		if(deleteTimeReportId != null) {
			
			try{
				dbService.deleteTimeReport(Integer.parseInt(deleteTimeReportId));
			}
			catch(Exception e) {
				
			}
			out.print(getUserTimeReports(loggedInUser, req));
			return;
		}
		
		if(addReportWeek != null && addReportYear != null) {
			
			int addReportWeekInt = Integer.parseInt(addReportWeek);
			
			if(addReportWeekInt > 0 && addReportWeekInt <= 53) {
				out.print(activityReportForm(Integer.parseInt(addReportWeek), Integer.parseInt(addReportYear), ""));
			return;
			}
			
		}
			
		
		
		if(timeReportId != null) {
		
			out.print(getActivityReports(Integer.parseInt(timeReportId), req));			
			return;
		}
		
			out.println(getUserTimeReports(loggedInUser, req)); //Standard case
			
			
		}
		
		catch (NumberFormatException e) {
			e.printStackTrace(); 
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private String showAllUsers(HttpServletRequest req) throws SQLException {
		
		List<User> userList = dbService.getAllUsers(this.getProjectId(req));
		
		String html = "<table width=\"600\" border=\"2\">\r\n" + "<tr>\r\n" + "<td> Användarnamn </td>\r\n"
				+ "<td> Se användares tidrapporter </td>\r\n"+ 
				"</td>\r\n";
		
		for(User u : userList) {
			
			html +=   "<tr>\r\n" 
				+ "<td>" + u.getUsername()+ "</td>\r\n"+
				"<td> <form action=\"TimeReportPage?showUserPage="+u.getUserId()+"\" method=\"get\"> "
				+ "<button name=\"showUserPage\" type=\"submit\" value=\"" +u.getUserId() + "\"> Välj </button> </form> </td> \r\n";
		}
		
		html += "</tr>\r\n" + "</table>"; //Ends the HTML table
		
		return html;
		
	}
	
	private String getTimereportsByWeekAndYear(int week, int year, HttpServletRequest req) throws SQLException {
		
		String html = "";
		
		List<TimeReport> timeReportList = dbService.getTimeReportsByProject(this.getProjectId(req));
		
		html += "<table width=\"400\" border=\"2\">\r\n" 
				+ "<tr>\r\n" 
				+ "<td> År </td>\r\n"
				+ "<td> Vecka </td>\r\n"
				+ "<td> Användarnamn </td>\r\n"
				+ "<td> Timespent(minutes) </td>\r\n" 
				+ "<td> Status </td>\r\n" 
				+ "<td> Välj tidrapport </td>\r\n"
				+ "<td> Ta bort tidrapport </td>\r\n";
		
		for (TimeReport tr : timeReportList) {

			if(tr.getWeek() == week && tr.getYear() == year) {
			
			int timeReportTotalTime = getTotalTimeReportTime(tr);
			String signed;
			String reportOwner = dbService.getUserByTimeReportId(tr.getTimeReportId()).getUsername();
			
			if (tr.isSigned()) { // get isSigned or not
				signed = "Signerad";
			} else {
				signed = "Ej signerad";
			}
			

			html += "<tr>\r\n" + "<td>" + tr.getYear() + "</td>\r\n" +
					"<td>" + tr.getWeek() + "</td>\r\n"+
					"<td>" + reportOwner + "</td>\r\n"+
					"<td>" + timeReportTotalTime + "</td>\r\n" + "<td>" + signed + "</td>\r\n"
					+ "<td> <form action=\"TimeReportPage?timeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
					+ "<button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() 
					+ "\"> Välj </button>  </form> </td> \r\n";
			
			html += "</tr>\r\n";
			}

		}
		
		//END OF TABLE
		html += "</tr>\r\n" + "</table>";
		
		return html;
		
	}

	private String getActivityReports(int timeReportId, HttpServletRequest req) throws Exception {

		String html = "";
		
		TimeReport timeReport = dbService.getTimeReportById(timeReportId);
		User reportOwner = dbService.getUserByTimeReportId(timeReportId);
		boolean isProjectLeader = isProjectLeader(req);
		boolean reportIsSigned = timeReport.isSigned();
		boolean reportIsFinished = timeReport.isFinished();
		
		if(this.isProjectLeader(req) && reportOwner.getUserId() != this.getLoggedInUser(req).getUserId()) {
			html += "<body> <b> "+ reportOwner.getUsername() + "</b> <br> </body>\r\n";
		}
		 html +=  "<table width=\"600\" border=\"2\">\r\n" + "<tr>\r\n" + "<td> Datum </td>\r\n"
				+ "<td> Aktivitetstyp </td>\r\n" + "<td> Subtyp </td>\r\n" + "<td> Minuter </td>\r\n"
				+ "<td> Ta bort aktivitetsrapport </td>\r\n";

		List<ActivityReport> activityReports = dbService.getActivityReports(timeReportId);
		List<ActivityType> activityTypes = dbService.getActivityTypes();
		List<ActivitySubType> activitySubTypes = dbService.getActivitySubTypes();

		String activityType;
		String activitySubType;

		for (ActivityReport aReport : activityReports) {
			
			activityType = getActivityType(aReport, activityTypes);
			activitySubType = getActivitySubType(aReport, activitySubTypes);
			

			html +=   "<tr>\r\n" 
					+ "<td>" + aReport.getReportDate().toString() + "</td>\r\n"
					+ "<td>" + activityType + "</td>\r\n" 
					+ "<td>" + activitySubType + "</td>\r\n" 
					+ "<td>" + aReport.getMinutes() + "</td>\r\n";
			
			if(!reportIsSigned && !reportIsFinished && reportOwner.getUserId() == this.getLoggedInUser(req).getUserId()) { //If timereport isn't signed, show button for deleting activity, else dont show it.
				html += "<td> <form action=\"TimeReportPage?deleteActivityReportId=\""+aReport.getActivityReportId()+"&timeReportId=\"" + timeReportId + "\" method=\"get\">\r\n" + 
						"		<input name=\"deleteActivityReportId\" type=\"hidden\" value=\""+aReport.getActivityReportId()+"\"></input>\r\n" + 
						" <input name=\"timeReportId\" type=\"hidden\" value=\""+timeReportId+"\"></input>\r\n" + 
						"		<input type=\"submit\" value=\"Ta bort\"></input>\r\n" + 
						"	</td> \r\n"
					  + "</form>";
				
			}
			
			
		}
		
		html += "</tr>\r\n" + "</table>"; //Ends the HTML table
		
			if(isProjectLeader)	{ //If projectleader is looking at timereports, show sign/unsign buttons
				
				if(reportIsSigned) {
					html += "<form method=\"get\"> <button name=\"timeReportIdToUnsign\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
					+ "\"> Avsignera </button>  </form> \r\n" ;
				}
				
				else if(!reportIsSigned && reportIsFinished) {
					html += "<form method=\"get\"> <button name=\"timeReportIdToSign\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
					+ "\"> Signera </button>  </form> \r\n";
				}
				
				else if(!reportIsSigned && !reportIsFinished) {
					html += "<body> Rapporten är inte markerad som redo för signering. </body> \r\n";
				}
			}
			
			
			if(reportOwner.getUserId() == this.getLoggedInUser(req).getUserId() && !timeReport.isFinished() && !timeReport.isSigned()) { //If timereport owner is the one logged in and looking at this screen AND isnt marked as finished
				html += "<form action=\"TimeReportPage?week=\""+timeReport.getWeek()+"&timeReportId=\"" + timeReportId + "\"&addReportYear=\"" + timeReport.getYear() + "\" method=\"get\">\r\n" +  //Show button for adding activity
						"		<input name=\"addReportWeek\" type=\"hidden\" value=\""+timeReport.getWeek()+"\"></input>\r\n" + 
						" <input name=\"timeReportId\" type=\"hidden\" value=\""+timeReportId+"\"></input>\r\n" + 
						" <input name=\"addReportYear\" type=\"hidden\" value=\""+timeReport.getYear()+"\"></input>\r\n" + 
						"		<input type=\"submit\" value=\"Lägg till ny aktivitet.\"></input>\r\n" + 
						"	</form>";
				
				html +=	"<td> <form action = \"TimeReportPage?timeReportFinishedId=\""+timeReport.getTimeReportId()+"\" method=\"get\"> <button name=\"timeReportFinishedId\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
				+ "\"> Markera tidrapport som redo för signering. </button>  </form> \r\n";										//Mark activity report as finished
			}
			
			else if(reportOwner.getUserId() == this.getLoggedInUser(req).getUserId() && timeReport.isFinished() && !timeReport.isSigned()) { //unmark activity report as finished
				
				html +=	"<td> <form action = \"TimeReportPage?timeReportNotFinishedId=\""+timeReport.getTimeReportId()+"\" method=\"get\"> <button name=\"timeReportNotFinishedId\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
				+ "\"> Avmarkera tidrapport som redo för signering. </button>  </form> \r\n";		
			}

		
		

		return html;
	}
	
	

	private String getActivityType(ActivityReport activityReport, List<ActivityType> typeList) throws Exception {
		
		   for(ActivityType aType: typeList) { //Get activity type for current activity report
			   
			   if(aType.getActivityTypeId() == activityReport.getActivityTypeId()) {
				   return aType.getType();
			   }
		   }
		   
		   throw new Exception("Kunde inte hitta aktivitetstyp");
			
		}
	
	private String getActivitySubType(ActivityReport activityReport, List<ActivitySubType> subTypeList) throws Exception{
		
		for (ActivitySubType aSubType : subTypeList) { // Get activity type for current activity report
				
			if (aSubType.getActivitySubTypeId() == activityReport.getActivitySubTypeId()) { 
				
				return aSubType.getSubType();
			}
		}
		return "";
		//throw new Exception("Kunde inte hitta aktivitetssubtyp"); //should always find a subType in loop above.
	}
	

	private String getUserTimeReports(User user, HttpServletRequest req) throws Exception {
		
		String html = "";

		List<TimeReport> userTimeReports = dbService.getTimeReportsByUser(user.getUserId());


		if(this.isProjectLeader(req) && user.getUserId() != this.getLoggedInUser(req).getUserId()) {
			html += "<body> <b> "+ user.getUsername() + "</b> <br> </body>\r\n";
		}
			
		if(userTimeReports.isEmpty()) {
			html += "<body> Inga tidsrapporter finns för den valda användaren </body>\r\n";
		}
		
		else {
			
			 html += "<table width=\"600\" border=\"2\">\r\n" 
					+ "<tr>\r\n" 
					+ "<td> År </td>\r\n"
					+ "<td> Vecka </td>\r\n"
					+ "<td> Tidspenderad (minuter) </td>\r\n" 
					+ "<td> Status </td>\r\n" 
					+ "<td> Välj tidrapport </td>\r\n"
					+ "<td> Ta bort tidrapport </td>\r\n";
		
		for (TimeReport tr : userTimeReports) {

			int timeReportTotalTime = getTotalTimeReportTime(tr);
			String signed;

			if (tr.isSigned()) { // get isSigned or not
				signed = "Signerad";
			} else {
				signed = "Ej signerad";
			}

			html += "<tr>\r\n" + "<td>" + tr.getYear() + "</td>\r\n" +
					"<td>" + tr.getWeek() + "</td>\r\n"+
					"<td>" + timeReportTotalTime + "</td>\r\n" + "<td>" + signed + "</td>\r\n"
					+ "<td> <form action=\"TimeReportPage?timeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
					+ "<button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() 
					+ "\"> Välj </button>  </form> </td> \r\n";
			
			if(!tr.isSigned() && user.getUserId() == this.getLoggedInUser(req).getUserId()) { //If timereport isn't signed, a button for deleting it should be visible
					html += "<td> <form action=\"TimeReportPage?deleteTimeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
					+ "<button name=\"deleteTimeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() + "\"> Ta bort </button> </form> </td> \r\n";
			}
			
			html += "</tr>\r\n";

			}
		}
		//END OF TABLE
		html += "</tr>\r\n" + "</table>";
		
		try {
			
			LocalDate d = LocalDate.now();
			
			if(user.getUserId() == this.getLoggedInUser(req).getUserId()) { // IF the logged in user is the one browsing this page, give the option to create a new timereport.
				html += "<!--square.html-->\r\n" + 
						"<!DOCTYPE html>\r\n" + 
						"<html>\r\n"	
						+ " <form id=\"filter_form\" method=\"get\">\r\n" 
						+ "             Skapa tidrapport för: \r\n"
						+ "                <div id=\"selectWeek\">\r\n"
						+ "                    <select id=\"addReportWeek\" name=\"addReportWeek\" form=\"filter_form\">\r\n";
				
				for(int i = 1; i < 54; i++) {
					
					html += "<option value=" + i + ">Vecka: " + i + " </option>\r\n";
				}
				
					   html += "</select>\r\n  </div>\r\n"
					   		+ "<div id=\"selectYear\">\r\n"
						+ "                    <select id=\"addReportYear\" name=\"addReportYear\" form=\"filter_form\">\r\n";
				for(int i = 2020; i <= d.getYear(); i++) {	
					   html += "                        <option value="+i+">År: "+i+"</option>\r\n";
				}
						html += "            </select>\r\n" 
						+ "              </div>\r\n"						
						+ "            </div>\r\n"
						+ "			  <input type=\"submit\" value=\"Skapa tidrapport\" >\r\n"
						+ "           </form>"
						+ "          </html>";
				}
			
			if(isProjectLeader(req)) {//Show all unsigned reports button and all userslist button for ProjectLeader
				
				
				
				html += "<!--square.html-->\r\n" + //Get reports for week and year
						"<!DOCTYPE html>\r\n" + 
						"<html>\r\n"	
						+ " <form id=\"getAllReports\" method=\"get\">\r\n" 
						+ "             Hämta alla tidsrapporter för detta projektet för: \r\n"
						+ "                <div id=\"selectWeek\">\r\n"
						+ "                    <select id=\"getReportsWeek\" name=\"getReportsWeek\" form=\"getAllReports\">\r\n";
				
				for(int i = 1; i < 54; i++) {
					
					html += "<option value=" + i + ">Vecka: " + i + " </option>\r\n";
				}
				
					   html += "</select>\r\n  </div>\r\n"
					   		+ "<div id=\"selectYear\">\r\n"
						+ "                    <select id=\"getReportsYear\" name=\"getReportsYear\" form=\"getAllReports\">\r\n";
				for(int i = 2020; i <= d.getYear(); i++) {	
					   html += "                        <option value="+i+">År: "+i+"</option>\r\n";
				}
						html += "            </select>\r\n" 
						+ "              </div>\r\n"						
						+ "            </div>\r\n"
						+ "			  <input type=\"submit\" value=\"Hämta tidsrapporter\" >\r\n"
						+ "           </form>"
						+ "          </html>";
						
						html += "<form action=\"TimeReportPage?showAllUnsignedReports\" metod=\"get\">\r\n" + 
						"  <input name=\"showAllUnsignedReports\" type=\"submit\" value=\"Visa alla osignerade tidrapporter\" >\r\n" + 
						"</form>\r\n"+
						"<form action=\"TimeReportPage?showAllUsers\" metod=\"get\">\r\n" + 
						"  <input name=\"showAllUsers\" type=\"submit\" value=\"Visa alla användare\" >\r\n" + 
						"</form>"
						+ "<br>";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return html;

	}
	/**
	 * Project leader retrives a table of all unsigned timereports.
	 * @return HTML Page
	 * @throws SQLException
	 */
	private String getUnsignedTimeReports(HttpServletRequest req) throws SQLException { 
		
		String html= "";
		
		try {
			if(!isProjectLeader(req)) {
				new Exception("Endast en projekledare har tillgång till denna vyn");
			}
		
		
		
		List <TimeReport> allTimeReports = dbService.getTimeReportsByProject(this.getProjectId(req)); 
		List <TimeReport> unsignedTimeReports = new ArrayList<TimeReport>();
		
		for(TimeReport tr : allTimeReports) {
			
			if(!tr.isSigned()) {
				unsignedTimeReports.add(tr);
			}
		}
		
		if(unsignedTimeReports.isEmpty()) {
			
			html += "<body> Det finns inga osignerade tidrapporter i systemet!</body>";
		}
		
		
		 html = "<table width=\"400\" border=\"2\">\r\n" 
				+ "<tr>\r\n" 
				+ "<td> Vecka </td>\r\n"
				+ "<td> Användarnamn </td>\r\n"
				+ "<td> Tid spenderad (minuter) </td>\r\n" 
				+ "<td> Status </td>\r\n" 
				+ "<td> Välj tidrapport </td>\r\n";

		for (TimeReport tr : unsignedTimeReports) {

			int timeReportTotalTime = getTotalTimeReportTime(tr);
			String signed;

			if (tr.isSigned()) { // get isSigned or not
				signed = "Signerad";
			} else {
				signed = "Ej signerad";
			}
			
			User trOwner = dbService.getUserByTimeReportId(tr.getTimeReportId());

			html += "<tr>\r\n" + "<td>" + tr.getWeek() + "</td>\r\n" + // set values into HTML
					"<td>" + trOwner.getUsername() + "</td>\r\n" +
					"<td>" + timeReportTotalTime + "</td>\r\n"
					+ "<td>" + signed + "</td>\r\n" //Should be "Ej signerad" for all reports
					+ "<td> <form method=\"get\"> <button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() + "\"> Välj </button> </form> \r\n"
					+ "</td>\r\n" + "</tr>\r\n";

		}

		html += "</tr>\r\n" + "</table>"; // END HTML	
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return html;	
	}

	private int getTotalTimeReportTime(TimeReport tr) throws SQLException {

		int totalTime = 0;

		List<ActivityReport> activitiesInTimeReport = dbService.getActivityReports(tr.getTimeReportId());

		for (ActivityReport ar : activitiesInTimeReport) { // calculate total time from all activity reports inside this
															// timeReport
			totalTime += ar.getMinutes();
		}
		return totalTime;
	}

	private String activityReportForm(int week, int year, String timeReportId) { //Kallas på först -- - onchange =\"this.form.submit()\"
		
		try {
			TimeReport tr = dbService.getTimeReportById(Integer.parseInt(timeReportId));
			year = tr.getYear();
		} catch (Exception e) {}
		
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
		
		return "<!--square.html-->\r\n" + 
				"<!DOCTYPE html>\r\n" + 
				"<html>\r\n"	
				+ " <form id=\"filter_form\" method=\"get\">\r\n" + "                 Aktivitetstyp\r\n"
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"act_picker_1\" name=\"activity\" form=\"filter_form\">\r\n"
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
				+ "                        <option value=30>Möte</option>\r\n"
				+ "                        <option value=41>Föreläsning</option>\r\n"
				+ "                        <option value=42>Övning</option>\r\n"
				+ "                        <option value=43>Terminalövning</option>\r\n"
				+ "                        <option value=44>Självstudier</option>\r\n"
				+ "                        <option value=100>Övrigt</option>\r\n"
				+ "                      </select>\r\n" + "                </div>\r\n" + "            </div>\r\n"
				+ "            <div id=\"subTypes\">\r\n" + "                <p class=\"descriptors\">Aktivitet subtyp</p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"act_picker_2\" name=\"subType\" form=\"filter_form\">\r\n"
				+ "                        <option value=\"U\">U</option>\r\n"
				+ "                        <option value=\"O\">O</option>\r\n"
				+ "                        <option value=\"I\">I</option>\r\n"
				+ "                        <option value=\"F\">F</option>\r\n"
				+ "                      </select>\r\n" + "                </div>\r\n" + "            </div>\r\n"
				+ "<script>"
				+ "const one = document.querySelector('#act_picker_1');const two = document.querySelector('#subTypes');"
				+ "one.addEventListener('change', (event) => {"
				+ "const pickedValue = event.target.value;"
				+ "if (pickedValue > 19) {"
				+ "two.style.visibility = 'hidden';"
				+ "} else {"
				+ "two.style.visibility = 'visible';"
				+"}"
				+ "});"
				+ "</script>"
				+ "                <p class=\"descriptors\">Tid spenderad (i minuter) </p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n" + "				</div>"
				+ "              <input class=\"credentials_rect\" type=\"number\" id=\"timeSpent\" name=\"timeSpent\" min=\"1\" max=\"1440\" pattern=\"^[0-9]*$\" title=\"Please enter numbers only.\" maxlength=\"4\" placeholder=\"Tid Spenderad\" required><br>\r\n"
				+ "		<input name=\"addReportWeek\" type=\"hidden\" value=\""+ week + "\"></input>\r\n"
				+"<input name=\"addReportYear\" type=\"hidden\" value=\""+ year + "\"> </input>\r\n"
				+ "  <label for=\"dateInfo\">Mata in datum för aktivitet: </label>\r\n"  
				+ "<input type=\"date\" id=\"dateOfReport\" name=\"dateOfReport\" value=\"" + p + "\" min=\""+ s +"\" max=\""+ e+ "\">\r\n"	
				+ " <input name=\"timeReportId\" type=\"hidden\" value=\""+ timeReportId + "\"></input>\r\n"
				+ "              <input class=\"submitBtn\" type=\"submit\" value=\"Skicka in\">\r\n" 				
				+ "                </div>\r\n"
				+ "              </form>"
				+ "              </html>";


	}		
	
	private boolean isProjectLeader(HttpServletRequest req) throws Exception {
		return this.isProjectLeader(req, this.getProjectId(req));
	}
	
}


