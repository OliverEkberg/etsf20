package controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;
import database.Project;
import database.Role;
import database.Statistic;
import database.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Servlet implementation class StatisticController
 * 
 * 
 * @author Ferit Bolezek
 * @author Oliver Ekberg
 * @version 0.1
 * 
 */

@WebServlet("/" + Constants.STATISTICS_PATH) 
public class StatisticController extends servletBase {
	private static final long serialVersionUID = 1L;
	
	enum StatisticType {
		ALL,
		USER,
		ROLE
	}
	
	List<Project> activeProjects;
	List<User> projectUsers;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setSessionTimeout(req);

		PrintWriter out = resp.getWriter();

		if (getLoggedInUser(req) == null) {
			resp.sendRedirect("/BaseBlockSystem/" + Constants.SESSION_PATH);
			return;
		}

		String from = req.getParameter("from");
		String to = req.getParameter("to");
		String query = req.getParameter("statType");

		if (from == null || to == null) {
			out.println(statisticsPageForm(null, req, null, null));
		} else {
			try {
				LocalDate fromDate = LocalDate.parse(from);
				LocalDate toDate = LocalDate.parse(to);

				if (actionIsAllowed(req, getProjectId(req)) || getLoggedInUser(req) != null)
					out.println(statisticsPageForm(
							getStats(fromDate, toDate, getProjectId(req), query, req), req, fromDate, toDate));
				else {
					out.println(
							"<p style=\"background-color:#c0392b;color:white;padding:16px;\">ACTION NOT ALLOWED: You are not admin or project leader for this project."
									+ "</p>");
					out.println(statisticsPageForm(null, req, null, null));
				}

			} catch (DateTimeParseException e) {
				out.println(
						"<p style=\"background-color:#c0392b;color:white;padding:16px;\">Incorrect date format, please enter in this format: yyyy-mm-dd, Ex. 2020-03-29"
								+ "</p>");
				out.println(statisticsPageForm(null, req, null, null));
			} catch (Exception e) {
				out.println(statisticsPageForm(null, req, null, null));
				e.printStackTrace();
			}
		}

	}

	/**
	 * Given two dates, splits them into 10 week intervals.
	 * @param from query option fromDate (start date) received from the HTML datePicker.
	 * @param to query option toDate (end date) received from the HTML datePicker.
	 * @return list of dates.
	 */
	private List<LocalDate> getStatDates(LocalDate from, LocalDate to) {
		Set<LocalDate> datesT = new TreeSet<>();
		datesT.add(from);

		for (LocalDate date = from.plusWeeks(9); date.isBefore(to); date = date.plusWeeks(9)) {
			if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
				date = date.plusWeeks(1);
			}

			while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
				date = date.plusDays(1);
			}

			datesT.add(date);
		}

		datesT.add(to.plusDays(1));
		return new ArrayList<LocalDate>(datesT);
	}

	
	/**
	 * Gets a list of statistics filtered by the parameters.
	 * @param fromDate query option fromDate (start date) received from the HTML datePicker.
	 * @param toDate query option toDate (end date) received from the HTML datePicker.
	 * @param projectId the project's id.
	 * @param query query string, depending on what type of stats to get, received from URL query.
	 * @param req the current httpRequest.
	 * @return A list of the requested statistics.
	 * @throws Exception if statistics could not be received.
	 */
	private List<Statistic> getStats(LocalDate fromDate, LocalDate toDate, int projectId, String query, HttpServletRequest req) throws Exception {
		List<Statistic> stats = new ArrayList<Statistic>();
		List<Role> roles = dbService.getAllRoles();
		projectUsers = dbService.getAllUsers(projectId);

		List<LocalDate> ds = getStatDates(fromDate, toDate);

		for (int i = 1; i < ds.size(); i++) {
			LocalDate from = ds.get(i - 1);
			LocalDate to = ds.get(i);

			Statistic statistic = null;
			switch (statsToGet(query, req)) {
			case USER:
				statistic = dbService.getActivityStatistics(projectId, getIdForUser(query), from, to);
				break;
			case ALL:
				statistic = dbService.getActivityStatistics(projectId, from, to);
				break;
			case ROLE:
				statistic = dbService.getRoleStatistics(projectId, getRoleIdFor(query, roles), from, to);
				break;
			default:
				return null;

			}
			stats.add(statistic);
		}

		return stats;
	}
	
	/**
	 *  Checks if the current user is allowed to perform the action.
	 * @param req the current httpRequest.
	 * @param projectId the project's id.
	 * @return whether this action is allowed or not.
	 */
	private boolean actionIsAllowed(HttpServletRequest req, int projectId) {
		try {
			User user = getLoggedInUser(req);

			if (user == null)
				return false;

			if (user.isAdmin())
				return true;
			else {
				Role r = dbService.getRole(user.getUserId(), projectId);
				if (r.getRoleId() == 1) {
					return true;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	
	/**
	 * Gets the roleId given the roles name.
	 * @param name the name of the role to get id for.
	 * @param roles all available roles.
	 * @return the role's Id or 0.
	 */
	private int getRoleIdFor(String name, List<Role> roles) {
		for (Role role : roles) {
			if (role.getRole().equals(name))
				return role.getRoleId();
		}
		return 0;
	}

	/**
	 * Gets the userId given the username.
	 * @param username the username to get userId for.
	 * @return the userId linked to the username or -1 if not found.
	 */
	private int getIdForUser(String username) {
		for (User user : projectUsers) {
			if (user.getUsername().equals(username))
				return user.getUserId();
		}
		return -1;
	}

	/**
	 * Gets a StatisticType given parameter options.
	 * @param query query string, depending on what type of stats to get, received from URL query.
	 * @param req the current httpRequest.
	 * @return a StatisticType.
	 */
	private StatisticType statsToGet(String query, HttpServletRequest req) {
		try {
			if (query != null) {
				if (query.equals("*")) {
					return StatisticType.ALL;
				}
				for (Role r : dbService.getAllRoles()) {
					if (r.getRole().equals(query)) {
						return StatisticType.ROLE;
					}
				}
				for (User u : dbService.getAllUsers(getProjectId(req))) {
					if (u.getUsername().equals(query)) {
						return StatisticType.USER;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StatisticType.ALL;		
	}
	
	/**
	 * Gets the HTML to display on this page.
	 * @param statistics list of statistics.
	 * @param req the current httpRequest.
	 * @param from the date to check from. Will use first day of week if null provided.
	 * @param to the date to check to. Will use last day of week if null provided.
	 * @return the HTML for the site.
	 */
	private String statisticsPageForm(List<Statistic> statistics, HttpServletRequest req, LocalDate from, LocalDate to) {
		LocalDate start, end;
		
		if (from != null && to != null) {
			start = from;
			end = to;
		} else {
			LocalDate now = LocalDate.now();
			start = Helpers.getFirstDayOfWeek(now);
			end = Helpers.getLastDayOfWeek(now);
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<body>");
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/StatisticsController.css\">");
		sb.append(getHeader(req));
		sb.append("<div id=\"wrapper\">");
		sb.append(getNav(req));
		sb.append("<div id=\"bodyContent\">");
		sb.append("<div class=\"wrapper\">");
		sb.append("<div class=\"\">");
		sb.append("<form id=\"filter_form\" >");
		sb.append("<p id=\"head_text\">STATISTICS</p>");
		sb.append("<div class=\"filter_row\">");
		sb.append("<div>");
		sb.append("</div>");
		sb.append("<div>");
		sb.append("<div>");
		sb.append("<p class=\"descriptors\">From</p>"); // DONE
		sb.append("<p class=\"descriptors\" style=\"margin-left: 140px;\">To</p>\r\n");
		sb.append("</div>");
		sb.append("<div id=\"stat_date_picker\">\r\n");
		sb.append("<input type=\"date\" id=\"from\" value=\""+ start +"\" name=\"from\">");
		sb.append("         <input type=\"date\" id=\"to\" value=\""+ end +"\" name=\"to\">");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("<div>");
		sb.append("</div>");
		sb.append("<div>");
		sb.append("<p class=\"descriptors\">Statistic Type</p>");
		sb.append("<div id=\"activity_picker\">");
		sb.append("<select id=\"rol_picker\" name=\"statType\" form=\"filter_form\" onclick=\"if (this.selectedIndex) disableBoxes(this);\">");
		sb.append(getSelectOptions(req));
		sb.append("</select>");		
		sb.append("</div>");
		sb.append("</div>");
		sb.append("<div>");
		sb.append("</div>");
		sb.append("<input class=\"submitBtn\" type=\"submit\" value=\"Search\">");
		sb.append("</div>");
		sb.append("</form>");
		sb.append("</div>");

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
		sb.append(getFooter());
		sb.append("</body>");
		return sb.toString();
	}

	/**
	 * Gets the options in HTML format for the different statistic groups.
	 * @param req the current httpRequest.
	 * @return the HTML code for select options.
	 */
	private String getSelectOptions(HttpServletRequest req) {
		String divider = "<option disabled>-----------</option>";
		StringBuilder sb = new StringBuilder();
		
		if (isProjectLeader(req, getProjectId(req))) {
			sb.append("<option value=\"");
			sb.append("*");
			sb.append("\">");
			sb.append("Whole project");
			sb.append("</option>\n");
			
			sb.append(divider);
			List<Role> roles = dbService.getAllRoles();
			for (Role role : roles) {
				sb.append("<option value=\"");
				sb.append(role.getRole());
				sb.append("\">");
				sb.append(role.getRole());
				sb.append("</option>\n");
			}
			
			sb.append(divider);
			List<User> users = dbService.getAllUsers(getProjectId(req));
			for (User user : users) {
				if (!user.isAdmin()) { // Should not be possible to view stats for admins since they can not report time.
					sb.append("<option value=\"");
					sb.append(user.getUsername());
					sb.append("\">");
					sb.append(user.getUsername());
					sb.append("</option>\n");
				}
			}
		} else { // Regular users should only see themselves
			User loggedInUser = getLoggedInUser(req);
			sb.append("<option value=\"");
			sb.append(loggedInUser.getUsername());
			sb.append("\">");
			sb.append(loggedInUser.getUsername());
			sb.append("</option>\n");
		}

		return sb.toString();
	}


	/**
	 * Makes HTML of statistics data.
	 * @param statistic The statistic to get in HTML format.
	 * @return HTML for the statistics in table format.
	 */
	private String getStatisticsDataTable(Statistic statistic) {
		StringBuilder sbBuilder = new StringBuilder();

		String[] rowLabel = statistic.getRowLabels();

		sbBuilder.append("<table style=\"margin-bottom:36px\" id=\"stats\">\n");
		sbBuilder.append("<tr>\n");
		sbBuilder.append("<th></th>");
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
}
