package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;
import database.*;

/**
 * Servlet implementation class UserController
 * 
 * @author Christoffer Larsson
 * @author Emil Jönsson
 * @author Jonatan Walse
 * @version 0.1
 * 
 */
@WebServlet("/" + Constants.USERS_PATH)
public class UserController extends servletBase {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!isLoggedIn(req)) {
			resp.sendRedirect(Constants.SESSION_PATH);
			return;
		}
		
		PrintWriter out = resp.getWriter();
		out.println(getHeader(req));
		out.println("<body>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/usercontroller.css\">\n");
		out.println(getNav(req));

		User loggedInUser = getLoggedInUser(req);

		// Change password page requested
		if ("yes".equals(req.getParameter("changePassword"))) {
			out.println(changePasswordForm());
			return;
		}
		
		// New password to set
		String newPassword = req.getParameter("newPassword");
		if (newPassword != null) {
			if (checkPassword(newPassword)) {
				try {
					changePassword(newPassword, loggedInUser);
					out.println(alert("Password changed!"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				out.println(alert("Invalid password!"));
			}
			
			out.println(changePasswordForm());
			return;
		}

		// Default to System Users  (must be admin)
		if (loggedInUser.isAdmin()) {
			out.println("<p id=\"user_title_text\">System Users</p>");

			// check if the administrator wants to add a new user in the form
			String newName = req.getParameter("addname");
			if (newName != null) {
				if (checkNewName(newName)) {
					String password = addUser(newName);
					if (password == null) {
						out.println("<p>Error: Suggested user name not possible to add</p>");
					} else {
						out.println(alert("User added with password: " + password));
					}
				} else
					out.println("<p>Error: Suggested name not allowed</p>");
			}

			// check if the administrator wants to delete a user
			String deleteName = req.getParameter("deletename");
			if (deleteName != null) {
				if (checkNewName(deleteName)) {
					dbService.deleteUserByUsername(deleteName);
				} else
					out.println("<p>Error: URL wrong</p>");
			}

			try {
				List<User> users = dbService.getAllUsers();
				out.println("<table id=\"userTable\" border=" + addQuotes("1") + ">");
				out.println("<tr><th>Name</th><th colspan=\"2\">Settings</th></tr>");
				for (User u : users) {
					String name = u.getUsername();
					String deleteURL = Constants.USERS_PATH + "?deletename=" + name;
					String deleteCode = "<a href=" + addQuotes(deleteURL) + " onclick="
							+ addQuotes("return confirm('Are you sure you want to delete " + name + "?')")
							+ "> delete </a>";
					String resetURL = Constants.USERS_PATH + "?resetName=" + u.getUserId();
					String resetCode = "<a href=" + addQuotes(resetURL) + " onclick="
							+ addQuotes(
									"return confirm('Are you sure you want to reset password for: " + name + "?')")
							+ "> reset password </a>";

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

			out.println("</body></html>");

			String resetName = req.getParameter("resetName"); // TODO: Clarify that this is actually reset password for user
			if (resetName != null) {
				int reset = Integer.parseInt(resetName);
				try {
					out.print(resetPassword(reset));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	* Resets the password for a given user into an auto-generatoed one.
	* @param the user id for the specified user.
	* @return html popup window with the new password.
	* @throws Exception
	*/
	private String resetPassword(int reset) throws Exception {
		User user = dbService.getUserById(reset);
		String newPassword = generatePassword();
		user.setPassword(newPassword);
		dbService.updateUser(user);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<body>");
		sb.append(alert("Password changed to: " + newPassword));
		sb.append("</body>");
		sb.append("</html>");
		sb.append("<!DOCTYPE html>");
		sb.append("<!DOCTYPE html>");
		return sb.toString();
	}
	/**
	* A form for adding new users to the system.
	* @return html form for adding usernames.
	*/
	public String addUserForm() {
		String html;
		html = "<p> <form name=" + addQuotes("input");
		html += " method=" + addQuotes("get");
		html += "<p> Add user name: <input type=" + addQuotes("text") + " name=" + addQuotes("addname") + '>';
		html += "<input type=" + addQuotes("submit") + "value=" + addQuotes("Add user") + '>';
		html += "</form>";
		return html;
	}
	/**
	* A form for changing the users password.
	* @return html form for changing password.
	*/
	public String changePasswordForm() {
		String html = "<div id=" + addQuotes("bodyContent") + ">";
		html += "<p id=\"user_title_text\">Change Password</p>";
		html += "<form name=" + addQuotes("input");
		html += " method=" + addQuotes("get") + ">";
		html += "<p class=\"descriptors\">New password:</p><br>";
		html += "<div style=" + addQuotes("display: flex; align-items: center;") + ">";
		html += "<input type=" + addQuotes("text") + " name=" + addQuotes("newPassword") + "class=" + addQuotes("input") + '>';
		html += "<input type=" + addQuotes("submit") + "value=" + addQuotes("Change Password") + "class="+ addQuotes("btn") + '>';
		html += "</div>";
		html += "</form>";
		html += "</div>";

		return html;
	}
	/**
	* Adds a new user to the database with an auto-generated password.
	* @param name of user.
	*/
	private String addUser(String name) {
		try {
			String newPassword = generatePassword();
			User u = new User(0, name, newPassword, false);
			dbService.createUser(u);
			return u.getPassword();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return null;
	}
	/**
	* Changes the password for a certain user.
	* @param the desired password.
	* @param the specified user object.
	* @throws Exception
	*/
	private void changePassword(String newPassword, User u) throws Exception {
		u.setPassword(newPassword);
		dbService.updateUser(u);
		System.out.println(newPassword);
	}
	/**
	* Generates a random password of 6 letters.
	* @return auto-generated password.
	*/
	private String generatePassword() {
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		for (int i = 0; i < Constants.MIN_PASSWORD_LENGTH; i++)
			sb.append((char) (r.nextInt(26) + 97)); // 122-97+1=26
		return sb.toString();
	}

	/**
	 * Checks if given character is allowed as a part of a username or password.
	 * 
	 * @param c The character to check.
	 * @return Whether the character is allowed or not.
	 */
	private boolean isAllowedChar(char c) {
		int ci = (int) c;
		return (ci >= 48 && ci <= 57) || (ci >= 65 && ci <= 90) || (ci >= 97 && ci <= 122);
	}
	/**
	* Checks if a given username is allowed.
	* @param the name to check.
	* @return wether it is allowd or not.
	*/
	private boolean checkNewName(String name) {
		int length = name.length();
		boolean isOk = (length >= Constants.MIN_USERNAME_LENGTH && length <= Constants.MAX_USERNAME_LENGTH);
		
		for (int i = 0; i < length && isOk; i++) {
			isOk = isOk && isAllowedChar(name.charAt(i));
		}
		
		return isOk;
	}
	/**
	* Checks if a given password is allowed.
	* @param the password to check.
	* @return wether it is allowd or not.
	*/
	private boolean checkPassword(String password) {
		int length = password.length();
		boolean isOk = (length >= Constants.MIN_PASSWORD_LENGTH && length <= Constants.MAX_PASSWORD_LENGTH);
		
		for (int i = 0; i < length && isOk; i++) {
			isOk = isOk && isAllowedChar(password.charAt(i));
		}
		
		return isOk;
	}
}
