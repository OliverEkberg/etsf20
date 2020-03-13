package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	private static final long serialVersionUID = 1L;
	List<Project> activeProjects;
	List<User> projectUsers;

	public StatisticController() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();

		try {
			if (getLoggedInUser(req) == null) {
				resp.sendRedirect("/BaseBlockSystem/SessionPage");
				return;
			}
		} catch (Exception e) {
			resp.sendRedirect("/BaseBlockSystem/SessionPage");
			return;
		}

		String from = req.getParameter("from");
		String to = req.getParameter("to");
		String query = req.getParameter("statType");

		if (from == null || to == null) {
			out.println(statisticsPageForm(null, req));
		} else {
			try {
				LocalDate fromDate = LocalDate.parse(from);
				LocalDate toDate = LocalDate.parse(to);
				System.out.println(fromDate.toString());

				if (actionIsAllowed(req, getProjectId(req)) || getLoggedInUser(req) != null)
					out.println(statisticsPageForm(
							getStats(fromDate, toDate, getProjectId(req), query, req), req));
				else {
					out.println(
							"<p style=\"background-color:#c0392b;color:white;padding:16px;\">ACTION NOT ALLOWED: You are not admin or project leader for this project."
									+ "</p>");
					out.println(statisticsPageForm(null, req));
				}

			} catch (DateTimeParseException e) {
				out.println(
						"<p style=\"background-color:#c0392b;color:white;padding:16px;\">Incorrect date format, please enter in this format: yyyy-mm-dd, Ex. 2020-03-29"
								+ "</p>");
				out.println(statisticsPageForm(null, req));
			} catch (Exception e) {
				out.println(statisticsPageForm(null, req));
				e.printStackTrace();
			}
		}

	}

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
			case 1:
				statistic = dbService.getActivityStatistics(projectId, getIdForUser(query), from, to);
				break;
			case 2:
				statistic = dbService.getActivityStatistics(projectId, from, to);
				break;
			case 3:
				statistic = dbService.getRoleStatistics(projectId, getRoleIdFor(query, roles), from, to);
				break;
			case -1:
				return null;

			}
			stats.add(statistic);
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

	private int statsToGet(String query, HttpServletRequest req) {
		try {
			if (query != null) {
				if (query.equals("*")) {
					return 2;
				}
				for (Role r : dbService.getAllRoles()) {
					if (r.getRole().equals(query)) {
						return 1;
					}
				}
				for (User u : dbService.getAllUsers(getProjectId(req))) {
					if (u.getUsername().equals(query)) {
						return 3;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;		
	}

	private String statisticsPageForm(List<Statistic> statistics, HttpServletRequest req) {

		StringBuilder sb = new StringBuilder();

		sb.append("<body>");
		sb.append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/StatisticsController.css\">\r\n");
		sb.append(getHeader(req));
		sb.append("        <div id=\"wrapper\">\r\n" + getNav(req) + "            <div id=\"bodyContent\">"
				+ "    <div class=\"wrapper\">\r\n" + "        <div class=\"\">\r\n"
				+ "            <form id=\"filter_form\" >\r\n" + "                <div id=\"stat_title\">\r\n"
				+ "                    <p id=\"stat_title_text\">STATISTICS</p>\r\n" + "                </div>\r\n"
				+ "\r\n" + "                <div class=\"filter_row\">\r\n" + "                    <div>\r\n"
				+ "                    </div>\r\n" + "                    <div>\r\n"
				+ "                        <div>\r\n" + "                        <p class=\"descriptors\">From</p>\r\n"
				+ "                        <p class=\"descriptors\" style=\"margin-left: 140px;\">To</p>\r\n"
				+ "                    </div>\r\n" + "                    <div id=\"stat_date_picker\">\r\n"
				+ "                    <input type=\"date\" id=\"from\" name=\"from\">\r\n"
				+ "                    <input type=\"date\" id=\"to\" name=\"to\">\r\n" + "                </div>\r\n"
				+ "            </div>\r\n" + "            <div>\r\n");
		sb.append("            </div>\r\n" + "            <div>\r\n"
				+ "                <p class=\"descriptors\">Statistic Type</p>\r\n"
				+ "                <div id=\"activity_picker\">\r\n"
				+ "                    <select id=\"rol_picker\" name=\"statType\" form=\"filter_form\" onclick=\"if (this.selectedIndex) disableBoxes(this);\">");
		sb.append(getSelectOptions(req));
		sb.append("</select>");
		sb.append("                </div>\r\n" + "            </div>\r\n" + "            <div>\r\n");
		sb.append("            </div>\r\n"
				+ "                <input class=\"submitBtn\" type=\"submit\" value=\"Search\">\r\n"
				+ "                </div>\r\n" + "              </form> \r\n" + "        </div>\r\n");

		sb.append("<div>"); // Table goes here or nothingness goes here :(
		if (statistics == null) {
			sb.append("<p id=\"nothing\">There seems to be nothing here :(</p><br>");
			sb.append(
					"<p id=\"nothingSub\"> That could be because filter options are empty/incorrect or your filter options has yielded no results.</p>");
		} else {

			for (int i = 0; i < statistics.size(); i++) {
				sb.append(getStatisticsDataTable(statistics.get(i)));
			}

		}
		sb.append("</div>");

		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</body>");
		return sb.toString();
	}

	private String getProjectSelectOptions(HttpServletRequest req) {
		StringBuilder sbBuilder = new StringBuilder();

		try {
			if (getLoggedInUser(req) == null)
				return sbBuilder.toString();

			activeProjects = dbService.getAllProjects(getLoggedInUser(req).getUserId());

			for (Project project : activeProjects) {
				sbBuilder.append("<option value=\"");
				sbBuilder.append(project.getName());
				sbBuilder.append("\">");
				sbBuilder.append(project.getName());
				sbBuilder.append("</option>\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sbBuilder.toString();
	}

	/**
	 * Gets the options in HTML format for the different statistic groups.
	 * 
	 * @return the HTML code for select options.
	 */
	private String getSelectOptions(HttpServletRequest req) {
		String divider = "<option disabled>-----------</option>";
		StringBuilder sb = new StringBuilder();
		try {
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
				sb.append("<option value=\"");
				sb.append(user.getUsername());
				sb.append("\">");
				sb.append(user.getUsername());
				sb.append("</option>\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

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
