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

/**
 * Abstract base with common logic for controller 
 * 
 * @author Jesper Annefors
 * @author Oliver Ekberg
 * @version 0.1
 * @since 2020-03-14
 */
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
    
    /**
     * Sets sets logged in in the session using given boolean.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @param loggedIn whether to set the session to logged in or logged out.
     */
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
    
    /**
     * Sets sets is admin in the session using given boolean.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @param admin whether the user is admin or not.
     */
    protected void setIsAdmin(HttpServletRequest request, boolean admin) {
		HttpSession session = request.getSession(true);
		session.setAttribute("admin", admin);
	}
    
    /**
     * Gets the id of the currently choosen project.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @return id of the current project or 0.
     */
    protected int getProjectId(HttpServletRequest request) {
    	HttpSession session = request.getSession(true);
    	Object objectState = session.getAttribute("projectId");
    	int projectId = 0;
		if (objectState != null) {
			projectId = (int) objectState; 
		}
		return projectId;
    }
    
    /**
     * Sets sets user id in the session using given parameter.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @param userId the user id to set. Use 0 to reset this.
     */
    protected void setUserId(HttpServletRequest request, int userId) {
		HttpSession session = request.getSession(true);
		session.setAttribute("userId", userId);
	}
    
    /**
     * Gets the logged in user as a model object.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @return the logged in user model or null.
     */
    protected User getLoggedInUser(HttpServletRequest request) {
    	HttpSession session = request.getSession(true);
    	Object objectState = session.getAttribute("userId");
    	int userId = 0;
		if (objectState != null) {
			userId = (int) objectState; 
		}
		
		return dbService.getUserById(userId);
    }
    
    /**
     * Sets sets project id in the session using given parameter.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @param projectId the project id to set. Use 0 to reset this.
     */
    protected void setProjectId(HttpServletRequest request, int projectId) {
		HttpSession session = request.getSession(true);
		session.setAttribute("projectId", projectId);
	}
    
    /**
     * Checks currently logged in user is project leader in given project.
     * @param request The HTTP Servlet request (so that the session can be found)
     * @param projectId unique identifier of the project to check.
     * @return whether user is project leader or not.
     */
    protected boolean isProjectLeader(HttpServletRequest request, int projectId) {
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
     * @param req The HTTP Servlet request (so that the session can be found)
     * @return String with html code for the header. 
     */
    protected String getHeader(HttpServletRequest req) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<head><title>TimeKeep</title></head>");
    	sb.append("<html><link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/layout.css\">");
    	sb.append("<div id=\"headerBar\">");
		
		if (isLoggedIn(req)) { // Header should be empty if logged out
			try {
				User u = getLoggedInUser(req);
				Project project = dbService.getProject(getProjectId(req));
				String projectName = "none";
				if (project != null) {
					projectName = project.getName();
				}
	
				String userName = u.getUsername();
				sb.append("<div id=\"sessionInfo\">");
				sb.append("<label><b>User: </b>" + userName + "</label>");
				
				if (!isAdmin(req)) { // Since admin can not pick a project, this should only be shown for regular users
					sb.append("<label><b>, Project: </b>" + projectName + "</label>");
				}
				
				sb.append("</div>");

			} catch (Exception e) {
			}
			sb.append("<a id=\"logoutbtn\" href=\"" + Constants.SESSION_PATH + "\">Logout</a>");
		}
		sb.append("</div>");
    	return sb.toString();
    }
    
    /**
     * Constructs the navigation menu of all servlets. 
     * @param req The HTTP Servlet request (so that the session can be found)
     * @return String with html code for the navigation menu. 
     */
    protected String getNav(HttpServletRequest req) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("<div id=\"navigation\">");
    	sb.append("<ul id=\"menu\">");
    	sb.append("<li><a class=\"linkBtn\" href=\"" + Constants.PROJECTS_PATH + "\">Projects</a></li>");
    	
    	boolean isAdmin = isAdmin(req);
    	int projectId = getProjectId(req);
    	
    	if (isAdmin) {
			sb.append("<li><a class=\"linkBtn\" href=\"" + Constants.USERS_PATH + "\">Users</a></li>");
		}
    	
    	if (projectId != 0 && !isAdmin) {
    		sb.append("<li><a class=\"linkBtn\" href=\"" + Constants.TIMEREPORTS_PATH + "\">Reports</a></li>");
    		sb.append("<li><a class=\"linkBtn\" href=\"" + Constants.STATISTICS_PATH + "\">Statistics</a></li>");
    	}
    	
    	sb.append("<li><a class=\"linkBtn\" href=\"" + Constants.USERS_PATH + "?changePassword=yes\">Change password</a></li>");
    	sb.append("</ul>");
    	sb.append("</div>");
    	
		return sb.toString();
    }
    
    /**
     * Constructs html and javascript for displaying an alert. 
     * @param text the text to show in the alert.
     * @return String with html code that will resolve to an alert. 
     */
    protected String alert(String text) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<script>");
    	sb.append("alert(\"");
    	sb.append(text);
    	sb.append("\");");
    	sb.append("</script>");
    	return sb.toString();
    }
    
    /**
     * Constructs a footer that makes sure the navigation bar is always at 100 percent height. 
     * @return String with html and javascript. 
     */
    protected String getFooter() {
    	return "        <script>        \r\n" + 
				"        function setFromWindowSize(){\r\n" + 
				"            var h = window.innerHeight;	\r\n" + 
				"            var bodyContentHeight = document.getElementById(\"bodyContent\").offsetHeight;\r\n" + 
				"\r\n" + 
				"            if (bodyContentHeight < h) {\r\n" + 
				"              document.getElementById(\"navigation\").style.height = h;\r\n" + 
				"            } else {\r\n" + 
				"              document.getElementById(\"navigation\").style.height = bodyContentHeight;\r\n" + 
				"            }\r\n" + 
				"        }\r\n" + 
				"        window.addEventListener(\"resize\", setFromWindowSize);\r\n" + 
				"        setFromWindowSize();\r\n" + 
				"        </script>";
    }
    
    @Override
    protected abstract void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    
    /**
	 * All requests are forwarded to the doGet method. 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
    /**
	 * Makes sure the current session times out after configured time.
	 * @param req The HTTP Servlet request (so that the session can be found)
	 */
	protected void setSessionTimeout(HttpServletRequest req) {
		req.getSession().setMaxInactiveInterval(Constants.SESSION_LENGTH);
	}
}
