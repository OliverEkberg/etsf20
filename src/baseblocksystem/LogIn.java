package baseblocksystem;
import java.io.*;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import database.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LogIn
 * 
 * A log-in page. 
 * 
 * The first thing that happens is that the user is logged out if he/she is logged in. 
 * Then the user is asked for name and password. 
 * If the user is logged in he/she is directed to the functionality page. 
 * 
 * @author Martin Host
 * @version 1.0
 * 
 */
@WebServlet("/LogIn")
public class LogIn extends servletBase {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogIn() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Generates a form for login. 
     * @return HTML code for the form
     */
    protected String loginRequestForm() {
    	String html = "<p>Please enter your name and password in order to log in:</p>";
    	html += "<p> <form name=" + addQuotes("input");
    	html += " method=" + addQuotes("post");
    	html += "<p> Name: <input type=" + addQuotes("text") + " name=" + addQuotes("user") + '>'; 
    	html += "<p> Password: <input type=" + addQuotes("password") + " name=" + addQuotes("password") + '>';  
    	html += "<p> <input type=" + addQuotes("submit") + "value=" + addQuotes("Submit") + '>';
    	return html;
    }
    
    
    /**
     * Checks with the database if the user should be accepted
     * @param name The name of the user
     * @param password The password of the user
     * @return true if the user should be accepted
     */
    private boolean checkUser(String name, String password) {
		boolean userOk = false;
		
		try{
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

    
	/**
	 * Implementation of all input to the servlet. All post-messages are forwarded to this method. 
	 * 
	 * First logout the user, then check if he/she has provided a username and a password. 
	 * If he/she has, it is checked with the database and if it matches then the session state is 
	 * changed to login, the username that is saved in the session is updated, and the user is 
	 * relocated to the functionality page. 
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println(getHeader());
		
		if (isLoggedIn(request)) {
			setIsLoggedIn(request, false);;
			out.println("<p>You are now logged out</p>");
		}
		
		String name;
		String password;
				
        name = request.getParameter("user"); // get the string that the user entered in the form
        password = request.getParameter("password"); // get the entered password
        if (name != null && password != null) {
        	if (checkUser(name, password)) {
       			setIsLoggedIn(request, true);  // save the state in the session
       			User u;
				try {
					u = dbService.getUserByCredentials(name, password);
					setUserId(request, u.getUserId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       			
       			response.sendRedirect("functionality.html");
       		}
       		else {
       			out.println("<p>That was not a valid user name / password. </p>");
       			out.println(loginRequestForm());
       		}
       	}else{ // name was null, probably because no form has been filled out yet. Display form.
       		out.println(loginRequestForm());
       	}
		
		out.println("</body></html>");
	}

	/**
	 * All requests are forwarded to the doGet method. 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
