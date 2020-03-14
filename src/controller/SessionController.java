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

@WebServlet("/" + Constants.SESSION_PATH)
public class SessionController extends servletBase {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();

		String name = req.getParameter("username");
		String password = req.getParameter("password");

		if (name != null && password != null) {
			if (login(name, password)) {
				setIsLoggedIn(req, true);
				try {
					User u = dbService.getUserByCredentials(name, password);
					setUserId(req, u.getUserId());
					setIsAdmin(req, u.isAdmin());
				} catch (SQLException e) {
					e.printStackTrace();
				}

				resp.sendRedirect(Constants.PROJECTS_PATH);
			} else {

				out.println("<p><!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n" + "\n" + "\n" + "<script> {\n"
						+ "  alert(\"That was not a valid user name / password.\");\n" + "}\n" + "</script>\n" + "\n"
						+ "</body>\n" + "</html>\n" + " </p>");
			}

		} else {
			logout(req);
		}
		
		out.println(getHeader(req));
		out.println(loginRequestForm());
	}

	private boolean login(String name, String password) {
		try {
			return dbService.getUserByCredentials(name, password) != null;
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return false;
	}

	private boolean logout(HttpServletRequest req) throws IOException {
		if (isLoggedIn(req) == true) {
			setIsLoggedIn(req, false);
			setUserId(req, 0);
			setProjectId(req, 0);
			setIsAdmin(req, false);
			return true;
		}
		return false;
	}

	private String loginRequestForm() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/SessionController.css\">");
		sb.append("<div class=\"wrapper\">");
		sb.append("<div class=\"title\">TimeKeep</div>");
		sb.append("<div class=\"subTitle\">Keep track of time and stuff</div>");
		sb.append("<div class=\"center credentials_form\">");
		sb.append("<form onsubmit=\"checkInput()\">"); // TODO: Add JS?
		sb.append("<input class=\"credentials_rect\" type=\"text\" id=\"username\" name=\"username\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Username\" required><br>");
		sb.append("<input class=\"credentials_rect\" type=\"password\" id=\"password\" name=\"password\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Password\" required><br><br>");
		sb.append("<input class=\"submitBtn\" type=\"submit\" value=\"Submit\">");
		sb.append("</form>");
		sb.append("</div>");
		sb.append("<div class=\"footerText\">Developed by some dudes at LTH</div>");
		sb.append("</body>");
		sb.append("</html>");
		
		return sb.toString();
	}
}
