package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;
import database.*;

/**
 * Servlet implementation class UserController
 * 
 * A xx page.
 * 
 * Description of the class.
 * 
 * @author Ferit B�lezek ( Enter name if you've messed around with this file
 *         ;) )
 * @version 1.0
 * 
 */
//typklar
@WebServlet("/UserPage")
public class UserController extends servletBase {
	private static final int PASSWORD_LENGTH = 6;

	public UserController() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// TODO Auto-generated method stub
		setProjectId(req, 1);
		PrintWriter out = resp.getWriter();
		out.println(getHeader());

		String myName = "";

		User loggedInUser = null;
		int selectedProject = 0;
		boolean isLeader = false;
		try {
			loggedInUser = getLoggedInUser(req);
			if (loggedInUser != null)
				myName = (String) loggedInUser.getUsername(); // if the name exists typecast the name to a string
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		selectedProject = getProjectId(req);

		try {
			isLeader = isProjectLeader(req, selectedProject);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// check that the user is logged in
		if (!isLoggedIn(req))
			resp.sendRedirect("SessionPage");
		else {

			if (loggedInUser.isAdmin()) {
				out.println("<h1>User Page - Admin" + "</h1>");

				// check if the administrator wants to add a new user in the form
				String newName = req.getParameter("addname");
				if (newName != null) {
					if (checkNewName(newName)) {
						boolean addPossible = addUser(newName);
						if (!addPossible)
							out.println("<p>Error: Suggested user name not possible to add</p>");
					} else
						out.println("<p>Error: Suggested name not allowed</p>");
				}

				// check if the administrator wants to delete a user by clicking the URL in the
				// list

				String deleteName = req.getParameter("deletename");
				if (deleteName != null) {
					if (checkNewName(deleteName)) {
						deleteUser(deleteName);
					} else
						out.println("<p>Error: URL wrong</p>");
				}

				try {
					List<User> users = dbService.getAllUsers();
					List<Project> projects = dbService.getAllProjects();
					List<Role> roles = dbService.getAllRoles();
					out.println("<p>Registered users:</p>");
					out.println("<table border=" + addQuotes("1") + ">");
					out.println("<tr><td>NAME</td><td></td><td></td></tr>");
					for (User u : users) {
						String name = u.getUsername();
						String deleteURL = "UserPage?deletename=" + name;
						String deleteCode = "<a href=" + addQuotes(deleteURL) + " onclick="
								+ addQuotes("return confirm('Are you sure you want to delete " + name + "?')")
								+ "> delete </a>";
						String resetURL = "UserPage?resetName=" + u.getUserId();
						String resetCode = "<a href=" + addQuotes(resetURL) + " onclick="
								+ addQuotes(
										"return confirm('Are you sure you want to reset password for: " + name + "?')")
								+ "> reset password </a>";

						if (u.isAdmin()) {
							deleteCode = "";
							resetCode = "";

						}

						out.println("<tr>");
						out.println("<td>" + name + "</td>");
						out.println("<td>" + deleteCode + "</td>");
						out.println("<td>" + resetCode + "</td>");
						out.println("</tr>");
					}
					out.println("</table>");
				} catch (SQLException ex) {
					System.out.println("SQLException: " + ex.getMessage());
					System.out.println("SQLState: " + ex.getSQLState());
					System.out.println("VendorError: " + ex.getErrorCode());
				}
				out.println(addUserForm());
				// out.println(assignUserForm());

				out.println("<p><a href =" + addQuotes("functionality.html") + "> Functionality selection page </p>");
				out.println("<p><a href =" + addQuotes("SessionPage") + "> Log out </p>");
				out.println("</body></html>");

				String resetName = req.getParameter("resetName");
				if (resetName != null) {
					int reset = Integer.parseInt(resetName);
					try {
						out.print(resetPassword(reset));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (isLeader) {
				// projektledarsida

				out.println("<h1>User Page - Project Leader" + "</h1>");

				try {
					List<User> users = dbService.getAllUsers(getProjectId(req));
					out.println("<p>Registered users:</p>");
					out.println("<table border=" + addQuotes("1") + ">");
					out.println("<tr><td>NAME</td><td>ROLE</td></tr>");
					for (User u : users) {
						out.println("<tr>");
						out.println("<td>" + u.getUsername() + "</td>");
						out.println("<td>" + dbService.getRole(u.getUserId(), selectedProject).getRole() + "</td>");
						out.println("</tr>");
					}
					out.println("</table>");
				} catch (SQLException ex) {
					System.out.println("SQLException: " + ex.getMessage());
					System.out.println("SQLState: " + ex.getSQLState());
					System.out.println("VendorError: " + ex.getErrorCode());
				}
				out.println(changePasswordForm());
				// out.println(assignUserForm());

				out.println("<p><a href =" + addQuotes("functionality.html") + "> Functionality selection page </p>");
				out.println("<p><a href =" + addQuotes("SessionPage") + "> Log out </p>");
				out.println("</body></html>");

				String newPassword = req.getParameter("password");
				if (newPassword != null) {
					if (checkPassword(newPassword)) {
						try {
							changePassword(newPassword, loggedInUser);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						out.println("<!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n" + "<script>\n"
								+ "  alert(\"Invalid password\");\n" + "</script>\n" + "\n" + "</body>\n"
								+ "</html>\n" + "");
					}
				}

			} else {
				// vanlig anv�ndare
				out.println("<h1>User Page" + "</h1>");
				out.println(changePasswordForm());
				String newPassword = req.getParameter("password");
				if (newPassword != null) {
					if (checkPassword(newPassword)) {
						try {
							changePassword(newPassword, loggedInUser);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						out.println("<!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n" + "<script>\n"
								+ "  alert(\"Invalid password\");\n" + "</script>\n" + "\n" + "</body>\n"
								+ "</html>\n" + "");
					}
				}
			}
		}

	}

	private String resetPassword(int reset) throws Exception {
		User user = dbService.getUserById(reset);
		String newPassword = generatePassword();
		user.setPassword(newPassword);
		String html = "<!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n" + "<script>\n"
				+ "  alert(\"Password changed to: " + newPassword + " \");\n" + "</script>\n" + "\n" + "</body>\n"
				+ "</html>\n" + "";
		dbService.updateUser(user);
		return html;

	}

	public String addUserForm() {

		String html;
		html = "<p> <form name=" + addQuotes("input");
		html += " method=" + addQuotes("get");
		html += "<p> Add user name: <input type=" + addQuotes("text") + " name=" + addQuotes("addname") + '>';
		html += "<input type=" + addQuotes("submit") + "value=" + addQuotes("Add user") + '>';
		html += "</form>";
		return html;
	}

	public String changePasswordForm() {

		String html;
		html = "<p> <form name=" + addQuotes("input");
		html += " method=" + addQuotes("get");
		html += "<p> Enter new password: <input type=" + addQuotes("text") + " name=" + addQuotes("password") + '>';
		html += "<input type=" + addQuotes("submit") + "value=" + addQuotes("Change Password") + '>';
		html += "</form>";
		return html;
	}

//	public String assignUserForm() throws SQLException {
//		List<User> users = dbService.getAllUsers();
//		List<Project> projects = dbService.getAllProjects();
//		List<Role> roles = dbService.getAllRoles();
//		String html;
//		html = "<p> Assign user to project. <form name="+addQuotes("assign")+" method ="+addQuotes("get")+
//				"<p> Name: <select name="+addQuotes("selname")+">";
//		for(User u:users) {
//			html += "<option value ="+ u.getUserId() + ">"+u.getUsername()+"</option>";
//		}
//		html += "</select> Project: <select name="+addQuotes("selproject")+">";
//		for(Project p:projects) {
//			html += "<option value =" + p.getProjectId() + ">"+p.getName()+"</option>";
//		}
//		html += "</select> Role: <select name="+addQuotes("selrole")+">";
//		for(Role r:roles) {
//			html += "<option value =" + r.getRoleId() + ">"+r.getRole()+"</option>";
//		}
//		html += "<input type="+ addQuotes("submit") + "value=" +addQuotes("Assign")+"> </form>";
//		return html;
//	}

	private boolean addUser(String name) {
		boolean resultOk = true;
		try {
			String newPassword = generatePassword();
			User u = new User(0, name, newPassword, false);
			dbService.createUser(u);
		} catch (Exception err) {
			resultOk = false;
			err.printStackTrace();
		}
		return resultOk;
	}

	private boolean addUserToProject(int userId, int projectId, int roleId) {
		boolean resultOk = true;
		try {
			dbService.addUserToProject(userId, projectId, roleId);
		} catch (Exception err) {
			resultOk = false;
			err.printStackTrace();
		}
		return resultOk;
	}

	private void changePassword(String newPassword, User u) throws Exception {
		u.setPassword(newPassword);
		dbService.updateUser(u);
		System.out.println(newPassword);
	}

	private String generatePassword() {
		String result = "";
		Random r = new Random();
		for (int i = 0; i < PASSWORD_LENGTH; i++)
			result += (char) (r.nextInt(26) + 97); // 122-97+1=26
		return result;
	}

	private void deleteUser(String name) {
		try {
			dbService.deleteUserByUsername(name);
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	private boolean checkNewName(String name) {
		int length = name.length();
		boolean isOk = (length >= 5 && length <= 10);
		if (isOk)
			for (int i = 0; i < length; i++) {
				int ci = (int) name.charAt(i);
				boolean thisOk = ((ci >= 48 && ci <= 57) || (ci >= 65 && ci <= 90) || (ci >= 97 && ci <= 122));
				isOk = isOk && thisOk;
			}
		return isOk;
	}

	private boolean checkPassword(String password) {
		int length = password.length();
		boolean isOk = (length >= 5 && length <= 10);
		if (isOk)
			for (int i = 0; i < length; i++) {
				int ci = (int) password.charAt(i);
				boolean thisOk = ((ci >= 48 && ci <= 57) || (ci >= 65 && ci <= 90) || (ci >= 97 && ci <= 122));
				isOk = isOk && thisOk;
			}
		return isOk;
	}

}
