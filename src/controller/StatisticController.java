package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import baseblocksystem.servletBase;
import database.ActivityType;
import database.DatabaseService;
import database.Project;
import database.Role;
import database.Statistic;
import database.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


/**
 * Servlet implementation class StatisticController
 * 
 * A xx page. 
 * 
 * Description of the class.
 * 
 * @author Ferit Blezek ( Enter name if you've messed around with this file ;) )
 * @version 1.0
 * 
 */

@WebServlet("/statistics")
public class StatisticController extends servletBase {
	List<Project> activeProjects;
	List<User> projectUsers;
	
	public StatisticController() {
		super();
	}
	
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
	

		String username = req.getParameter("username");
		String from = req.getParameter("from");
		String to = req.getParameter("to");
		String activity = req.getParameter("activity");
		String role = req.getParameter("role");
		String projectId = req.getParameter("projectId");
				
		System.out.println(projectId);
		
		if (from == null || to == null) {
			out.println(statisticsPageForm(null,req));
		} else {
			try {
				LocalDate fromDate = LocalDate.parse(from);
				LocalDate toDate = LocalDate.parse(to);
				System.out.println(fromDate.toString());
				int weeks = (int) ChronoUnit.WEEKS.between(fromDate, toDate);				
				
				
				if (actionIsAllowed(req, getIdForProject(projectId)) || getLoggedInUser(req) != null && username != null && username.equals(getLoggedInUser(req).getUsername()))
					out.println(statisticsPageForm(getStats(username, fromDate, toDate, activity, role, projectId, weeks),req));
				else {
					out.println("<p style=\"background-color:#c0392b;color:white;padding:16px;\">ACTION NOT ALLOWED: You are not admin or project leader for this project."  + "</p>");
					out.println(statisticsPageForm(null,req));
				}
				
			} catch (DateTimeParseException e) {
				out.println("<p style=\"background-color:#c0392b;color:white;padding:16px;\">Incorrect date format, please enter in this format: yyyy-mm-dd, Ex. 2020-03-29"  + "</p>");
				out.println(statisticsPageForm(null,req));
			} catch (Exception e) {
				out.println(statisticsPageForm(null,req));
				e.printStackTrace();
			}
		}
		
    }
    
    private List<Statistic> getStats(String username, LocalDate fromDate, LocalDate toDate, String activity, String role, String projectName, int weeks) throws Exception {
    	List<Statistic> stats = new ArrayList<Statistic>();
    	List<Role> roles = dbService.getAllRoles();
    	projectUsers = dbService.getAllUsers(getIdForProject(projectName));
    	
		while(weeks > 0) {
			Statistic statistic = null;
			
			if (weeks > 10) {
			switch (statsToGet(username, activity, role)) {
			case 1:
				statistic = dbService.getActivityStatistics(getIdForProject(projectName),getIdForUser(username), fromDate, fromDate.plusWeeks(10));
				break;
			case 2:
				statistic = dbService.getActivityStatistics(getIdForProject(projectName), fromDate, toDate);
				break;
			case 3:
				statistic = dbService.getRoleStatistics(getIdForProject(projectName), getRoleIdFor(role, roles), fromDate, toDate);
				break;
			case -1:
				return null;

			}
			stats.add(statistic);
			weeks = weeks - 10;
			fromDate = fromDate.plusWeeks(11);
			} else {
				switch (statsToGet(username, activity, role)) {
				case 1:
					statistic = dbService.getActivityStatistics(getIdForProject(projectName), getIdForUser(username), fromDate, toDate);
					break;
				case 2:
					statistic = dbService.getActivityStatistics(getIdForProject(projectName), fromDate, toDate);
					break;
				case 3:
					statistic = dbService.getRoleStatistics(getIdForProject(projectName), getRoleIdFor(role, roles), fromDate, toDate);
					break;
				case -1:
					return null;

				}
				stats.add(statistic);
				weeks = 0;
			}
			
		}
		return stats;
    }
    
	private boolean actionIsAllowed(HttpServletRequest req, int projectId) {
		try {
			User user = getLoggedInUser(req);
			
			if (user == null)
				return false;
			
			if (user.isAdmin())
				return true;
			else {
				int val = dbService.getProjectUserIdByUserIdAndProjectId(user.getUserId(), projectId);
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
    
    
    private int getRoleIdFor(String name, List<Role> roles) {
    	for (Role role : roles) {
			if (role.getRole().equals(name))
				return role.getRoleId();
		}
    	
    	return 1;
    }
    
    
    
    private int getIdForUser(String username) {
    	for (User user : projectUsers) {
			if (user.getUsername().equals(username))
				return user.getUserId();
		}
    	return -1;
    }
    
    
    private int statsToGet(String username, String activity, String role) {
    	if (username != null && !isBlank(username))
    		return 1;
    	else if(activity != null && activity.equals("yes"))
    		return 2;
    	else if (role != null && !isBlank(role))
    		return 3;
    	else
    		return -1;
    }
    
    private Boolean isBlank(String s) {
    	if (s.length() == 0) {
    		return true;
    	}
    	return false;
    }

		
	private String statisticsPageForm(List<Statistic> statistics,HttpServletRequest req) {
			
		StringBuilder sb = new StringBuilder();
		
		sb.append("<body>");
		sb.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/StatisticsController.css\">\r\n");
		sb.append(getHeader(req));
		sb.append(
						"        <div id=\"wrapper\">\r\n" + 
						getNav(req) +
						"            <div id=\"bodyContent\">" +
				"    <div class=\"wrapper\">\r\n" + 
				"        <div class=\"\">\r\n" + 
				"            <form id=\"filter_form\" >\r\n" + 
				"                <div id=\"stat_title\">\r\n" + 
				"                    <p id=\"stat_title_text\">STATISTICS</p>\r\n" + 
				"                </div>\r\n" + 
				"\r\n" + 
				"                <div class=\"filter_row\">\r\n" + 
				"                    <div>\r\n" + 
				"                    <p class=\"descriptors\">Username</p>\r\n" + 
				"                    <input class=\"credentials_rect\" type=\"text\" id=\"username\" name=\"username\" onkeydown=\"disableAllBoxes();\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Search for user\"><br>\r\n" + 
				"                    </div>\r\n" + 
				"                    <div>\r\n" + 
				"                        <div>\r\n" + 
				"                        <p class=\"descriptors\">From</p>\r\n" + 
				"                        <p class=\"descriptors\" style=\"margin-left: 140px;\">To</p>\r\n" + 
				"                    </div>\r\n" + 
				"                    <div id=\"stat_date_picker\">\r\n" + 
				"                    <input type=\"date\" id=\"from\" name=\"from\">\r\n" + 
				"                    <input type=\"date\" id=\"to\" name=\"to\">\r\n" + 
				"                </div>\r\n" + 
				"            </div>\r\n" + 
				"            <div>\r\n" + 
				"                <p class=\"descriptors\">Show data for entire project (within dates)</p>\r\n" + 
				"                <div id=\"activity_picker\">\r\n" + 
				"                    <select id=\"act_picker\" name=\"activity\" form=\"filter_form\" onclick=\"if (this.selectedIndex) disableBoxes(this);\">");
		sb.append("<option value=\"no\">NO</option>\n");
		sb.append("<option value=\"yes\">YES</option>\n");
		sb.append("</select>");
		sb.append("                </div>\r\n" + 
				"            </div>\r\n" + 
				"            <div>\r\n" + 
				"                <p class=\"descriptors\">Role</p>\r\n" + 
				"                <div id=\"activity_picker\">\r\n" + 
				"                    <select id=\"rol_picker\" name=\"role\" form=\"filter_form\" onclick=\"if (this.selectedIndex) disableBoxes(this);\">");
		sb.append(getRoleSelectOptions());
		sb.append("</select>");
		sb.append("                </div>\r\n" + 
				"            </div>\r\n" +
				"            <div>\r\n" + 
				"                <p class=\"descriptors\">Project</p>\r\n" + 
				"                <div id=\"activity_picker\">\r\n" + 
				"                    <select id=\"rol_picker\" name=\"projectId\" form=\"filter_form\" >");
		sb.append(getProjectSelectOptions(req));
		sb.append("</select>");
		sb.append("                </div>\r\n" + 
				"            </div>\r\n" + 
				"                <input class=\"submitBtn\" type=\"submit\" value=\"Search\">\r\n" + 
				"                </div>\r\n" + 
				"              </form> \r\n" + 
				"        </div>\r\n");
		
		sb.append("<div>"); // Table goes here or nothingness goes here :(
		if (statistics == null) {
			sb.append("<p id=\"nothing\">There seems to be nothing here :(</p><br>");
			sb.append("<p id=\"nothingSub\"> That could be because filter options are empty/incorrect or your filter options has yielded no results.</p>");
		} else {
			
			
			for (int i = 0; i < statistics.size(); i++) {
				sb.append(getStatisticsDataTable(statistics.get(i)));
			}
			
		}
		sb.append("</div>");
		
		
		
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("    <script>\r\n" + 
				"      function disableBoxes(event) {\r\n" + 
				"        switch (event.name) {\r\n" + 
				"          case \"activity\":\r\n" + 
				"            document.getElementById(\"rol_picker\").value = \"\";\r\n" + 
				"document.getElementById(\"username\").value = \"\";\n" +
				"            break;\r\n" + 
				"            case \"role\":\r\n" + 
				"            document.getElementById(\"act_picker\").value = \"\";\r\n" + 
				"document.getElementById(\"username\").value = \"\";\n" +
				"            break;\r\n" + 
				"        }\r\n" + 
				"      }\r\n" + 
				"\r\n" +
				"      function disableAllBoxes() {\r\n" + 
				"        document.getElementById(\"rol_picker\").value = \"\";\r\n" + 
				"        document.getElementById(\"act_picker\").value = \"\";\r\n" + 
				"      }" +
				"      document.addEventListener(\"DOMContentLoaded\", function() {\r\n" + 
				"        document.getElementById(\"rol_picker\").value = \"\";\r\n" + 
				"        document.getElementById(\"act_picker\").value = \"\";\r\n" + 
				"      });\r\n" + 
				"    </script>");
		sb.append("</body>");
		return sb.toString();
	}
	
	/**
	 * Gets the options in HTML format for the activities.
	 * @return the HTML code for select options.
	 */
	private String getActivitySelectOptions() {
		StringBuilder sbBuilder = new StringBuilder();
		try {
			List<ActivityType> activityTypes = dbService.getActivityTypes();
			for (ActivityType activityType : activityTypes) {
				sbBuilder.append("<option value=\"");
				sbBuilder.append(activityType.getType());
				sbBuilder.append("\">");
				sbBuilder.append(activityType.getType());
				sbBuilder.append("</option>\n");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sbBuilder.toString();
		
	}
	
	private String getProjectSelectOptions(HttpServletRequest req) {
		StringBuilder sbBuilder = new StringBuilder();
		
		try {
			//if(getLoggedInUser(req) == null)
				//return sbBuilder.toString();
			
			//List<Project> projects = dbService.getAllProjects(getLoggedInUser(req).getUserId()); // TODO: TEST FOR ID 1
			activeProjects = dbService.getAllProjects(1);
			for (Project project : activeProjects) {
				sbBuilder.append("<option value=\"");
				sbBuilder.append(project.getName());
				sbBuilder.append("\">");
				sbBuilder.append(project.getName());
				sbBuilder.append("</option>\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return sbBuilder.toString();
	}
	
	/**
	 * Gets the options in HTML format for the roles.
	 * @return the HTML code for select options.
	 */
	private String getRoleSelectOptions() {
		StringBuilder sbBuilder = new StringBuilder();
		try {
			List<Role> roles = dbService.getAllRoles();
			for (Role role : roles) {
				sbBuilder.append("<option value=\"");
				sbBuilder.append(role.getRole());
				sbBuilder.append("\">");
				sbBuilder.append(role.getRole());
				sbBuilder.append("</option>\n");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return sbBuilder.toString();
	}
	
	private int getIdForProject(String projectName) {
		for (Project project : activeProjects) {
			if (project.getName().equals(projectName))
				return project.getProjectId();
		}
		return -1;
	}

	private String getStatisticsDataTable(Statistic statistic) {
		StringBuilder sbBuilder = new StringBuilder();
		
		String[] rowLabel = statistic.getRowLabels();
		
		sbBuilder.append("<table style=\"margin-bottom:36px\" id=\"stats\">\n");
		sbBuilder.append("<tr>\n");
		sbBuilder.append("<th>Total</th>");
		for (String lbl : statistic.getColumnLabels()) {
			sbBuilder.append("<th>");
			sbBuilder.append(lbl);
			sbBuilder.append("</th>\n");
		}
		sbBuilder.append("<th>");
		sbBuilder.append("Total");
		sbBuilder.append("</th>\n");
		sbBuilder.append("</tr>\n");
		
		int[][] data = statistic.getData();
		int totalSum = 0;
		
		for (int i = 0; i < data.length; i++) {
			int sum = 0;
			
			sbBuilder.append("<tr>\n");
			
				sbBuilder.append("<td>");
				sbBuilder.append(rowLabel[i]);
				sbBuilder.append("</td>\n");
			
			for (int j = 0; j < data[i].length; j++) {
				sbBuilder.append("<td>");
				sbBuilder.append(String.valueOf(data[i][j]));
				sbBuilder.append("</td>\n");
				sum += data[i][j];
			}
			sbBuilder.append("<td>");
			sbBuilder.append(sum);
			sbBuilder.append("</td>\n");
			sbBuilder.append("</tr>\n");
			totalSum += sum;
		}
		sbBuilder.append("<tr>");
		sbBuilder.append("<td>");
		sbBuilder.append("Total");
		sbBuilder.append("</td>\n");
		
		int[] colSum = new int[data[0].length];
		for (int i = 0; i < data.length; i++) {
			 
			for (int j = 0; j < data[i].length; j++) {
				colSum[j] += data[i][j];
			}
		}
		
		for (int i : colSum) {
			sbBuilder.append("<td>");
			sbBuilder.append(String.valueOf(i));
			sbBuilder.append("</td>\n");
		}
		
		sbBuilder.append("<td>");
		sbBuilder.append(String.valueOf(totalSum));
		sbBuilder.append("</td>\n");
		
		sbBuilder.append("</tr>\n");
		sbBuilder.append("</table>");
		return sbBuilder.toString();
	}
	
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	doGet(req, resp);
    }
	

}