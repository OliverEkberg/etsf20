package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;
import database.ActivityType;
import database.DatabaseService;
import database.Role;
import database.Statistic;

import java.time.LocalDate;
import java.time.ZoneId;
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
	
	
	private DatabaseService dbService; // Temporary, will be replaced later.
	
	
	public StatisticController() {
		super();
		try {
			dbService = new DatabaseService();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private Statistic getUserStats(int userID, int projectID, LocalDate from, LocalDate to) throws Exception {
		return dbService.getActivityStatistics(userID, projectID, from, to); // FIXME: how do we get projectId? Some hardcoded values for now.
	}
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		out.println(getHeader(req));
		

		String username = req.getParameter("username");
		String from = req.getParameter("from");
		String to = req.getParameter("to");
		String activity = req.getParameter("activity");
		String role = req.getParameter("role");
		
		
		out.println("<body>");
		
		if (username == null || from == null || to == null || activity == null || role == null) {
			out.println(statisticsPageForm(null));
		} else {
			try {
				LocalDate fromDate = new SimpleDateFormat("dd-MMM-yyy").parse(from).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate toDate = new SimpleDateFormat("dd-MMM-yyy").parse(to).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				
				int weeks = (int) ChronoUnit.WEEKS.between(fromDate, toDate);
				List<Statistic> stats = new ArrayList<Statistic>();
				

					while(weeks > 0) {
						
						if (weeks > 10) {
						Statistic statistic = getUserStats(1, 1, fromDate, fromDate.plusWeeks(10));
						stats.add(statistic);
						weeks = weeks - 10;
						fromDate = fromDate.plusWeeks(11);
						} else {
							Statistic statistic = getUserStats(1, 1, fromDate, toDate);
							stats.add(statistic);
							weeks = 0;
						}
						
					}
				
				out.println(statisticsPageForm(stats));
			} catch (Exception e) {
				out.println(statisticsPageForm(null));
				e.printStackTrace();
			}
		}
		
    }

		
	private String statisticsPageForm(List<Statistic> statistics) {
			
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<body>");
		sb.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/StatisticsController.css\">\r\n" + 
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
				"                    <input class=\"credentials_rect\" type=\"text\" id=\"username\" name=\"username\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Search for user\" required><br>\r\n" + 
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
				"                <p class=\"descriptors\">Activity</p>\r\n" + 
				"                <div id=\"activity_picker\">\r\n" + 
				"                    <select id=\"act_picker\" name=\"activity\" form=\"filter_form\">");
		sb.append(getActivitySelectOptions());
		sb.append("</select>");
		sb.append("                </div>\r\n" + 
				"            </div>\r\n" + 
				"            <div>\r\n" + 
				"                <p class=\"descriptors\">Role</p>\r\n" + 
				"                <div id=\"activity_picker\">\r\n" + 
				"                    <select id=\"act_picker\" name=\"role\" form=\"filter_form\">");
		sb.append(getRoleSelectOptions());
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