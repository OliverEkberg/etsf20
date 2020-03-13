package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;
import database.User;

/**
 * Servlet implementation class SessionController
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

@WebServlet("/SessionPage")
public class SessionController extends servletBase {

	public SessionController() {
		super();

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		out.println(getHeader(req));


		String name = req.getParameter("username"); // get the string that the user entered in the form
		String password = req.getParameter("password"); // get the entered password


		if (name != null && password != null) {
			if (login(name, password)) {
				setIsLoggedIn(req, true); // save the state in the session
				User u;
				try {
					u = dbService.getUserByCredentials(name, password);
					setUserId(req, u.getUserId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				resp.sendRedirect("UserPage");
			} else {

				out.println("<p><!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n" + "\n" + "\n" + "<script> {\n"
						+ "  alert(\"That was not a valid user name / password.\");\n" + "}\n" + "</script>\n" + "\n"
						+ "</body>\n" + "</html>\n" + " </p>");

				out.println(loginRequestForm());
			}

		} else {
			out.println(loginRequestForm());
		}

	}

	private boolean login(String name, String password) {

		boolean userOk = false;

		try {
			User user = dbService.getUserByCredentials(name, password);
			if (user != null) {
				userOk = true;
			}
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return userOk;
	}

	private boolean logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (isLoggedIn(req) == true) {
			setIsLoggedIn(req, false);
			resp.sendRedirect("SessionPage");
			return true;

		}
		return false;

	}

	private String loginRequestForm() {
		return "<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/SessionController.css\">" +

				"    <div class=\"wrapper\">\r\n" + "        <div class=\"title\">TimeKeep</div>\r\n"
				+ "        <div class=\"subTitle\">Keep track of time and stuff.</div>\r\n"
				+ "        <div class=\"center credentials_form\">\r\n"
				+ "            <form onsubmit=\"checkInput()\">\r\n"
				+ "                <input class=\"credentials_rect\" type=\"text\" id=\"username\" name=\"username\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Username\" required><br>\r\n"
				+ "                <input class=\"credentials_rect\" type=\"password\" id=\"password\" name=\"password\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Password\" required><br><br>\r\n"
				+ "                <input class=\"submitBtn\" type=\"submit\" value=\"Submit\">\r\n"
				+ "              </form> \r\n" + "        </div>\r\n"
				+ "        <div class=\"footerText\">Developed by some dudes at LTH.</div>\r\n" + "    </div>"

				+ "</body></html>";
	}

}
