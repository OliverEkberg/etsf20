package controller;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 * @author Ferit B�lezek ( Enter name if you've messed around with this file ;)
 *         )
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	  
	  private void createActivityReport(int activityTypeId, int
	  activitySubTypeId, LocalDate date, int week, int minutes, int userId, int projectId ) throws SQLException {
	  
		TimeReport timereport = null;
		  
		if(dbService.hasTimeReport(week, date.getYear(), userId, projectId)) {// Does timereport this week exist?
			
			List<TimeReport> allReports = dbService.getTimeReportsByUser(userId);	//Find timereport for this week			
			for(TimeReport tr : allReports) { 
				if(tr.getWeek() == week) {
					timereport = tr;
				}
			}
		}
		else {
			timereport = dbService.createTimeReport(new TimeReport(0, 1/*ska bytas*/, 0, LocalDateTime.now(), date.getYear(), week, LocalDateTime.now(), false)); //TODO: ProjectuserID get samt signedAt (och signedBy?)
			}
	  
	 
		if(minutes > 1440) {
			new Exception("För många minuter rapporterade under ett dygn"); 
		}
		else {
			dbService.createActivityReport(new ActivityReport(0, activityTypeId, activitySubTypeId, timereport.getTimeReportId(), date, minutes));
		}
 
	  }
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			
		//TODO: Fixa vecka när man skapar rapport
		
		//TODO: Datum picker när man skapar rapporter, finns HTML standard grej <input type= "date" >
		//TRY/CATCH RUNT HELA
				
		PrintWriter out = resp.getWriter();
		setUserId(req,19); //USER ID 19 = PROJECTLEADER
		this.setIsLoggedIn(req, true);
		setProjectId(req, 1);		
		
	/*	TimeReport tr = dbService.getTimeReportById(4);
		tr.sign(1);												FOR TESTING TO SIGN TIMEREPORT
		dbService.updateTimeReport(tr);*/ 
		
		User loggedInUser = dbService.getUserById(19); // SKA VARA SEN this.getLoggedInUser(req);

		String activityType = req.getParameter("activity");
		String subType = req.getParameter("subType");
		String timeSpent = req.getParameter("timeSpent");
		String timeReportSignId = req.getParameter("timeReportIdToSign");
		String deleteActivityReportId = req.getParameter("deleteActivityReportId");
		String timeReportUnsignId = req.getParameter("timeReportIdToUnsign");
		String timeReportId = req.getParameter("timeReportId");
		String addReportWeek = req.getParameter("addReportWeek");
		String timeReportFinishedId = req.getParameter("timeReportFinishedId");
		String deleteTimeReportId = req.getParameter("deleteTimeReportId");
		String showAllUnsignedReports = req.getParameter("showAllUnsignedReports");
		
		if(activityType != null && subType != null && timeSpent != null && addReportWeek != null && timeReportId != null) {
			
			int activityTypeId = 0;
			int activitySubTypeId = 0;
			
			List<ActivityType> typeList = dbService.getActivityTypes();			//get activity typeID
			for(ActivityType at : typeList) {
				
				if(at.getType().equals(activityType))
				{
					activityTypeId = at.getActivityTypeId();
				}
			}
			
			List<ActivitySubType> subTypeList = dbService.getActivitySubTypes();		//get activity subtypeID
			for(ActivitySubType ast : subTypeList) {
				
				if(ast.getSubType().equals(subType))
				{
					activitySubTypeId = ast.getActivitySubTypeId();
				}
			}
			
			
			
			activitySubTypeId = 1; //TODO: Denna är konstig
			
			createActivityReport( activityTypeId, activitySubTypeId, LocalDate.now(),  Integer.parseInt(addReportWeek),  
					Integer.parseInt(timeSpent),  this.getLoggedInUser(req).getUserId(),  this.getProjectId(req) ); //TODO: LocalDate.now() ska bytas mot en datepicker som skickas med från activityreport meotden
			
			TimeReport timereport = dbService.getTimeReportById(Integer.parseInt(timeReportId)); //get timereport		
			out.print(getActivityReports(timereport.getTimeReportId(), req)); //Returns to the view of all activityreports for that timereport
			
			return;
		}
		
		if(addReportWeek != null && timeReportId != null && activityType == null && subType == null) {
			out.print(activityReportForm(Integer.parseInt(addReportWeek), timeReportId));
			return;
		}
		
		if(deleteActivityReportId != null && timeReportId != null) {
			
			dbService.deleteActivityReport(Integer.parseInt(deleteActivityReportId));
			out.print(getActivityReports(Integer.parseInt(timeReportId), req));
			
			return;
			
		}
		
		if(showAllUnsignedReports != null) {
			
			out.println(getUnsignedTimeReports(req));
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
			
			dbService.deleteTimeReport(Integer.parseInt(deleteTimeReportId));
			out.print(getUserTimeReports(loggedInUser, req));
			return;
		}
		
		if(addReportWeek != null) {
			out.print(activityReportForm(Integer.parseInt(addReportWeek), ""));
			return;
		}
		
		if(timeReportId != null) {
		
			out.print(getActivityReports(Integer.parseInt(timeReportId), req));			
			return;
		}
		//TODO: Visa egna tidsrapporter - Om url finns, om projektledare visa alla rapporter
		//knapp -> ny sida för att visa alla users -> en knapp till varje user för att selecta deras rapporter
		
			out.println(getUserTimeReports(loggedInUser, req)); //Standard case
			
			
		}
		
		catch (NumberFormatException e) {
			e.printStackTrace(); 
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private String getActivityReports(int timeReportId, HttpServletRequest req) throws Exception {

		TimeReport timeReport = dbService.getTimeReportById(timeReportId);
		boolean isProjectLeader = isProjectLeader(req);
		boolean reportIsSigned = timeReport.isSigned();
		boolean reportIsFinished = timeReport.isFinished();
		
		String html = "<table width=\"600\" border=\"2\">\r\n" + "<tr>\r\n" + "<td> Datum </td>\r\n"
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
			
			
			if(!reportIsSigned) { //If timereport isn't signed, show button for deleting timeReport, else dont show it.
				html += "<td> <form action=\"TimeReportPage?deleteActivityReportId=\""+aReport.getActivityReportId()+"&timeReportId=\"" + timeReportId + "\" method=\"get\">\r\n" + 
						"		<input name=\"deleteActivityReportId\" type=\"hidden\" value=\""+aReport.getActivityReportId()+"\"></input>\r\n" + 
						" <input name=\"timeReportId\" type=\"hidden\" value=\""+timeReportId+"\"></input>\r\n" + 
						"		<input type=\"submit\" value=\"Ta bort\"></input>\r\n" + 
						"	</td> \r\n"
					  + "</form>";//Add activity report button ;	//TODO: ska gå att tabort aktivitetsrapport om den inte är signerad
				
			}
			
			
		}
		
		html += "</tr>\r\n" + "</table>"; //Ends the HTML table
			if(isProjectLeader)	{ //If projectleader is looking at timereports, show sign/unsign buttons
				
				if(reportIsSigned) {
					html += "<td> <form method=\"get\"> <button name=\"timeReportIdToUnsign\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
					+ "\"> Avsignera </button>  </form> \r\n" ;
				}
				
				else if(!reportIsSigned && reportIsFinished) {
					html += "<td> <form method=\"get\"> <button name=\"timeReportIdToSign\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
					+ "\"> Signera </button>  </form> \r\n";
				}
			}
			
			
			if(timeReport.getProjectUserId() == this.getLoggedInUser(req).getUserId() && !timeReport.isFinished()) { //If timereport owner is the one logged in and looking at this screen AND timereport already exists and isnt marked as finished
				html += "<form action=\"TimeReportPage?week=\""+timeReport.getWeek()+"&timeReportId=\"" + timeReportId + "\" method=\"get\">\r\n" + 
						"		<input name=\"addReportWeek\" type=\"hidden\" value=\""+timeReport.getWeek()+"\"></input>\r\n" + 
						" <input name=\"timeReportId\" type=\"hidden\" value=\""+timeReportId+"\"></input>\r\n" + 
						"		<input type=\"submit\" value=\"Lägg till ny aktivitet.\"></input>\r\n" + 
						"	</form>";//Add activity report button 
				
				html +=	"<td> <form action = \"TimeReportPage?timeReportFinishedId=\""+timeReport.getTimeReportId()+"\" method=\"get\"> <button name=\"timeReportFinishedId\" type=\"submit\" value=\"" + timeReport.getTimeReportId() 
				+ "\"> Markera tidrapport som färdig. </button>  </form> \r\n";										//Mark activity report as finished
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
		return ""; //TODO: Det mesta som har med subtypes verkar gå sönder --
		//throw new Exception("Kunde inte hitta aktivitetssubtyp");
	}
	

	private String getUserTimeReports(User user, HttpServletRequest req) throws SQLException {
		
		String html = "";

		List<TimeReport> userTimeReports = dbService.getTimeReportsByUser(user.getUserId()); //TODO:  Get all timereports for logged in user

		if(userTimeReports.isEmpty()) {
			html += "<body> Inga tidsrapporter finns för den valda användaren </body>";
		}
		
		else {
			
			 html = "<table width=\"400\" border=\"2\">\r\n" 
					+ "<tr>\r\n" 
					+ "<td> Week </td>\r\n"
					+ "<td> Timespent(minutes) </td>\r\n" 
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

			html += "<tr>\r\n" + "<td>" + tr.getWeek() + "</td>\r\n" + // set values into HTML
					"<td>" + timeReportTotalTime + "</td>\r\n" + "<td>" + signed + "</td>\r\n"
					+ "<td> <form action=\"TimeReportPage?timeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
					+ "<button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() 
					+ "\"> Välj </button>  </form> </td> \r\n";
			
			if(!tr.isSigned()) { //If timereport isn't signed, a button for deleting it should be visible
					html += "<td> <form action=\"TimeReportPage?deleteTimeReportId="+tr.getTimeReportId()+"\" method=\"get\"> "
					+ "<button name=\"deleteTimeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() + "\"> Ta bort </button> </form> </td> \r\n";
			}
			
			html += "</tr>\r\n";

			}
		}
		//END OF TABLE
		html += "</tr>\r\n" + "</table>";
		
		try {
			if(user.getUserId() == this.getLoggedInUser(req).getUserId()) { // IF the logged in user is the one browsing this page, give the option to create a new timereport.
				html += "<form action=\"TimeReportPage?addReportWeek\" metod=\"get\">\r\n" + 
						"  <label for=\"week\">Week number:</label>\r\n" + 
						"  <input type=\"text\" id=\"addReportWeek\" name=\"addReportWeek\" pattern= \"[0-9]+\" maxlength=\"2\" title=\"Skriv endast siffror\" ><br><br>\r\n" + 
						"  <label for=\"lname\">Skapa tidrapport:</label>\r\n" + 
						"  <input type=\"submit\" value=\"Skicka in\" >\r\n" + 
						"</form>";//Add activity report button 
				}
			
			if(isProjectLeader(req)) {
				
				html += "<form action=\"TimeReportPage?showAllUnsignedReports\" metod=\"get\">\r\n" + 
						"  <input name=\"showAllUnsignedReports\" type=\"submit\" value=\"Visa alla osignerade tidrapporter\" >\r\n" + 
						"</form>";//Add activity report button 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			if(isProjectLeader(req)) {
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
			
			User trOwner = dbService.getUserById(tr.getProjectUserId()); //denna är fel

			html += "<tr>\r\n" + "<td>" + tr.getWeek() + "</td>\r\n" + // set values into HTML
					"<td>" + trOwner.getUsername() + "</td>\r\n" +
					"<td>" + timeReportTotalTime + "</td>\r\n"
					+ "<td>" + signed + "</td>\r\n" //Should be "Ej signerad" for all reports
					+ "<td> <form method=\"get\"> <button name=\"timeReportId\" type=\"submit\" value=\"" + tr.getTimeReportId() + "\"> Välj </button> </form> \r\n"
					+ "</td>\r\n" + "</tr>\r\n";

		}

		html += "</tr>\r\n" + "</table>"; // END HTML	
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
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

	private String activityReportForm(int week, String timeReportId) {
		return "<!--square.html-->\r\n" + 
				"<!DOCTYPE html>\r\n" + 
				"<html>\r\n"
				+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/SessionController.css\">"
				+ " <form id=\"filter_form\" onsubmit=\"checkInput()\">\r\n" + "                 Aktivitetstyp\r\n"
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"act_picker\" name=\"activity\" form=\"filter_form\">\r\n"
				+ "                        <option value=\"SDP\">SDP</option>\r\n"
				+ "                        <option value=\"SRS\">SRS</option>\r\n"
				+ "                        <option value=\"SVVS\">SVVS</option>\r\n"
				+ "                        <option value=\"STLDD\">STLDD</option>\r\n"
				+ "                        <option value=\"SVVI\">SVVI</option>\r\n"
				+ "                        <option value=\"SDDD\">SDDD</option>\r\n"
				+ "                        <option value=\"SVVR\">SVVR</option>\r\n"
				+ "                        <option value=\"SSD\">SSD</option>\r\n"
				+ "                        <option value=\"Slutrapport\">Slutrapport</option>\r\n"
				+ "                        <option value=\"Funktionstest\">Funktionstest</option>\r\n"
				+ "                        <option value=\"Systemtest\">Systemtest</option>\r\n"
				+ "                        <option value=\"Regressionstest\">Regressionstest</option>\r\n"
				+ "                        <option value=\"Mote\">Möte</option>\r\n"
				+ "                        <option value=\"Foreläsning\">Föreläsning</option>\r\n"
				+ "                        <option value=\"Ovning\">Övning</option>\r\n"
				+ "                        <option value=\"Terminalovning\">Terminalövning</option>\r\n"
				+ "                        <option value=\"Sjalvstudier\">Självstudier</option>\r\n"
				+ "                        <option value=\"Ovrigt\">Övrigt</option>\r\n"
				+ "                      </select>\r\n" + "                </div>\r\n" + "            </div>\r\n"
				+ "            <div>\r\n" + "                <p class=\"descriptors\">Aktivitet subtyp</p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"act_picker\" name=\"subType\" form=\"filter_form\">\r\n"
				+ "					     <option value=\"\"></option>\r\n"
				+ "                        <option value=\"Utveckling\">Utveckling</option>\r\n"
				+ "                        <option value=\"Omarbete\">Omarbete</option>\r\n"
				+ "                        <option value=\"Informellgranskning\">Informellgranskning</option>\r\n"
				+ "                        <option value=\"Formellgranskning\">Formellgranskning</option>\r\n"
				+ "                      </select>\r\n" + "                </div>\r\n" + "            </div>\r\n"
				+ "                <p class=\"descriptors\">Tid spenderad (i minuter) </p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n" + "				</div>"
				+ "              <input class=\"credentials_rect\" type=\"text\" id=\"timeSpent\" name=\"timeSpent\" pattern=\"^[0-9]*$\" title=\"Please enter numbers only.\" maxlength=\"4\" placeholder=\"Tid Spenderad\" required><br>\r\n"
				+ "		<input name=\"addReportWeek\" type=\"hidden\" value=\""+ week + "\"></input>\r\n"
				+ " <input name=\"timeReportId\" type=\"hidden\" value=\""+ timeReportId + "\"></input>\r\n"
				+ "              <input class=\"submitBtn\" type=\"submit\" value=\"Skicka in\">\r\n" 				
				+ "                </div>\r\n"
				+ "              </form>"
				+ "              </html>";

		// html += activitysubtype.getId...
	}
	
	private boolean isProjectLeader(HttpServletRequest req) throws Exception {
		return this.isProjectLeader(req, this.getProjectId(req));
	}
	
}


