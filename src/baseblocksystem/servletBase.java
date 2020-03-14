package baseblocksystem;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import controller.Constants;
import database.DatabaseService;
import database.Project;
import database.Role;
import database.User;

public abstract class servletBase extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected DatabaseService dbService;
	
	/**
	 * Constructs a Servlet and makes a connection to the database through databaseService
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
    	try {
    		User loggedInUser = getLoggedInUser(request);
        	Role role = dbService.getRole(loggedInUser.getUserId(), projectId);
        	return role.getRoleId() == 1;
    	} catch (Exception e) {
			return false;
		}
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
    protected String getHeader(HttpServletRequest req) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<head><title>TimeKeep</title></head>");
    	sb.append("<html><link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/layout.css\">");
    	sb.append("<div id=\"headerBar\">");
		
		if (isLoggedIn(req)) {
			String userName = "";

			try {
				User u = getLoggedInUser(req);
				Project project = dbService.getProject(getProjectId(req));
				String projectName = "none";
				if (project != null) {
					projectName = project.getName();
				}
	
				userName = u.getUsername();
				sb.append("<div id=\"sessionInfo\">"); // TODO: Make this look better
				sb.append("<label><b>User: </b>" + userName + "		</label>");
				sb.append("<label><b>Project: </b>" + projectName + "</label>");
				sb.append("</div>");

			} catch (Exception e) {
			}
			sb.append("<a id=\"logoutbtn\" href=\"" + Constants.SESSION_PATH + "\">Logout</a>");
		}
		sb.append("</div>");
    	return sb.toString();
    }
    
    protected String getNav(HttpServletRequest req) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("<div id=\"navigation\">");
    	sb.append("<ul id=\"menu\">");
    	sb.append("<li><a class=\"linkBtn\" href=\"projects\">Projects</a></li>");
    	
    	boolean isAdmin = false;
    	int projectId = 0;
    	
    	try {
    		isAdmin = isAdmin(req);
    		projectId = getProjectId(req);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	if (isAdmin) {
			sb.append("<li><a class=\"linkBtn\" href=\"UserPage\">Users</a></li>");
		}
    	
    	if (projectId != 0 && !isAdmin) {
    		sb.append("<li><a class=\"linkBtn\" href=\"TimeReportPage\">Reports</a></li>");
    		sb.append("<li><a class=\"linkBtn\" href=\"statistics\">Statistics</a></li>");
    	}
    	sb.append("</ul>");
    	sb.append("</div>");
    	
		return sb.toString();
    }
    
    protected String alert(String text) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<script>");
    	sb.append("alert(\"");
    	sb.append(text);
    	sb.append("\");");
    	sb.append("</script>");
    	return sb.toString();
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
