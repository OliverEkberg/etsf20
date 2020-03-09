package baseblocksystem;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.DatabaseService;
import database.Role;
import database.User;


/**
 *  This class is the superclass for all servlets in the application. 
 *  It includes basic functionality required by many servlets, like for example a page head 
 *  written by all servlets, and the connection to the database. 
 *  
 *  This application requires a database.
 *  For username and password, see the constructor in this class.
 *  
 *  <p>The database can be created with the following SQL command: 
 *  mysql> create database base;
 *  <p>The required table can be created with created with:
 *  mysql> create table users(name varchar(10), password varchar(10), primary key (name));
 *  <p>The administrator can be added with:
 *  mysql> insert into users (name, password) values('admin', 'adminp'); 
 *  
 *  @author Martin Host
 *  @version 1.0
 *  
 */
public abstract class servletBase extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected DatabaseService dbService;
	
	/**
	 * Constructs a servlet and makes a connection to the database through databaseService
	 */
    public servletBase() {
    	try{
    		dbService = new DatabaseService();
		} catch (SQLException ex) {
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
    }
    
    /**
     * Checks if a user is logged in or not.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @return true if the user is logged in, otherwise false.
     */
    protected boolean isLoggedIn(HttpServletRequest request) {
    	HttpSession session = request.getSession(true);
    	Object objectState = session.getAttribute("loggedIn");
    	boolean isLoggedIn = false;
		if (objectState != null) {
			isLoggedIn = (boolean) objectState; 
		}
		return isLoggedIn;
    }
    
    protected void setIsLoggedIn(HttpServletRequest request, boolean loggedIn) {
		HttpSession session = request.getSession(true);
		session.setAttribute("loggedIn", loggedIn);
	}
    
    /**
     * Checks if a user is admin or not.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @return true if the user is admin, otherwise false.
     */
    protected boolean isAdmin(HttpServletRequest request) {
    	HttpSession session = request.getSession(true);
    	Object objectState = session.getAttribute("admin");
    	boolean isAdmin = false;
		if (objectState != null) {
			isAdmin = (boolean) objectState; 
		}
		return isAdmin;
    }
    
    protected void setIsAdmin(HttpServletRequest request, boolean admin) {
		HttpSession session = request.getSession(true);
		session.setAttribute("admin", admin);
	}
    
    protected int getProjectId(HttpServletRequest request) {
    	HttpSession session = request.getSession(true);
    	Object objectState = session.getAttribute("projectId");
    	int projectId = 0;
		if (objectState != null) {
			projectId = (int) objectState; 
		}
		return projectId;
    }
    
    protected void setUserId(HttpServletRequest request, int userId) {
		HttpSession session = request.getSession(true);
		session.setAttribute("userId", userId);
	}
    
    protected User getLoggedInUser(HttpServletRequest request) throws Exception {
    	HttpSession session = request.getSession(true);
    	Object objectState = session.getAttribute("userId");
    	int userId = 0;
		if (objectState != null) {
			userId = (int) objectState; 
		}
		
		return dbService.getUserById(userId);
    }
    
    protected void setProjectId(HttpServletRequest request, int projectId) {
		HttpSession session = request.getSession(true);
		session.setAttribute("projectId", projectId);
	}
    
    protected boolean isProjectLeader(HttpServletRequest request, int projectId) throws Exception {
    	User loggedInUser = getLoggedInUser(request);
    	Role role = dbService.getRole(loggedInUser.getUserId(), projectId);
    	return role.getRoleId() == 1;
    }
    
    /**
     * Adds quotes to the given string.
     * @param str Input string
     * @return output string = "str" 
     */
    protected String addQuotes(String str) {
    	return '"' + str + '"';
    }
    
    
    /**
     * Constructs the header of all servlets. 
     * @return String with html code for the header. 
     */
    protected String getHeader() {
    	String header = "<head><title> The Base Block System (TODO) </title></head>";
    	return header;
    }
    
    protected abstract void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    
    /**
	 * All requests are forwarded to the doGet method. 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
