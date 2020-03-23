package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;
import database.Project;
import database.Role;
import database.User;

/**
 * Servlet implementation class ProjectController
 * 
 * @author Ferit Bolezek 
 * @author Dominik Gashi
 * @version 0.1
 */
@WebServlet("/" + Constants.PROJECTS_PATH)
public class ProjectController extends servletBase {
	private static final long serialVersionUID = 1L;

	private List<Role> roles;
	
	private Project currentProject;
	
	private int userCountNotInProject = 0; // Amount of users that is not in the currently selected project.
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setSessionTimeout(req);
		
		if (getLoggedInUser(req) == null) {
			resp.sendRedirect("/BaseBlockSystem/" + Constants.SESSION_PATH);
			return;
		}
		PrintWriter out = resp.getWriter();

		if (roles == null) {
			roles = dbService.getAllRoles();
		}
		
		// Edit project provided and user is allowed, select this project globally
		if (req.getParameter("editProject") != null && actionIsAllowed(req, Integer.valueOf(req.getParameter("editProject")))) {
			setProjectId(req, Integer.parseInt(req.getParameter("editProject")));
		}
		
		out.println("<body>" + "<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/ProjectController.css\">\n");
		out.println(getHeader(req));
		out.println(
				"        <div id=\"wrapper\">\r\n" + 
						getNav(req) +
				"            <div id=\"bodyContent\">");
		
		try {
			List<Project> plist;
			
			if (getLoggedInUser(req).isAdmin())
				plist = dbService.getAllProjects();
			else
				plist = dbService.getAllProjects(getLoggedInUser(req).getUserId());
			
			String pname = req.getParameter("projectName");
			String delete = req.getParameter("deleteProjectId");
			String deleteUser = req.getParameter("deleteUserId");
			String newRole = req.getParameter("newRole");
			
			String projId = req.getParameter("projectId");
			String userId = req.getParameter("userId");
			
			String initRole = req.getParameter("role");
			String projectSelected = req.getParameter("pickProjectId");
			String createdProject = req.getParameter("createdProject");
			
			String success = req.getParameter("success");
			
			if (projectSelected != null ) {
				setProjectId(req, Integer.valueOf(projectSelected));
				if (isAdmin(req)) {
					resp.sendRedirect(Constants.USERS_PATH);
				} else {
					resp.sendRedirect(Constants.TIMEREPORTS_PATH);
				}
			}
			
			if (pname != null && !isBlank(pname) && isAdmin(req) && success == null) {
				Project project = new Project(1, pname);
				project = createProject(project);
	
				plist.add(project);
				resp.sendRedirect(Constants.PROJECTS_PATH + "?createdProject=" + pname);
			
			} else if(createdProject != null && !isBlank(createdProject) && success == null) {
				out.println("<p style=\"background-color:#16a085;color:white;padding:16px;\">SUCCESFULLY CREATED PROJECT: " + createdProject + "</p>");			
			} else if (pname != null && !isBlank(pname) && !isAdmin(req) && success == null) {
				out.println("<p style=\"background-color:#c0392b;color:white;padding:16px;\">FAILED TO CREATE PROJECT: REASON: You are not allowed to perform this action. (Only Admins are allowed to create projects)" +  "</p>");
			} else if (success != null && !isBlank(success) && success.equals("false")) {
				out.println("<p style=\"background-color:#c0392b;color:white;padding:16px;\">FAILED TO CREATE PROJECT: REASON: The chosen project name already exists." +  "</p>");
			}
			
			if ( delete != null && !isBlank(delete) && (deleteUser == null || isBlank(deleteUser))) {
				Project projToDelete = plist.stream().filter(p -> p.getName().equals(delete)).findAny().orElse(null);
				if(projToDelete != null && actionIsAllowed(req, Integer.valueOf(projToDelete.getProjectId()))) {
					deleteProject(projToDelete.getProjectId());
					out.println("<p style=\"background-color:#16a085;color:white;padding:16px;\">SUCCESFULLY DELETED PROJECT:" + projToDelete.getName() + "</p>");
					plist.remove(projToDelete);
				} else {
					out.println("<p style=\"background-color:#c0392b;color:white;padding:16px;\">FAILED TO DELETE PROJECT:" + projToDelete.getName() +" REASON: You are not allowed to perform this action." +  "</p>");
				}
			}
			
			if ((delete != null && !isBlank(delete)) && (deleteUser != null && !isBlank(deleteUser)) ) {
				dbService.removeUserFromProject(Integer.parseInt(deleteUser), Integer.parseInt(delete));
				resp.sendRedirect("/BaseBlockSystem/" + Constants.PROJECTS_PATH + "?editProject=" + delete);
				return;
			}
			
			if ( (userId != null && !isBlank(userId)) && (projId != null && !isBlank(projId)) && (newRole != null && !isBlank(newRole)) ) {
				int roleId = getRoleIdFor(newRole, roles);
				dbService.updateUserProjectRole(Integer.parseInt(userId), Integer.parseInt(projId), roleId);
				resp.sendRedirect("/BaseBlockSystem/" + Constants.PROJECTS_PATH + "?editProject=" + projId);
				return;
			}
			
			if ((userId != null && !isBlank(userId)) && (projId != null && !isBlank(projId)) && (initRole != null && !isBlank(initRole))) {
				List<User> allUsers = dbService.getAllUsers();
				User findUser = allUsers.stream().filter(u -> u.getUsername().equals(userId)).findAny().orElse(null);
					
				dbService.addUserToProject(findUser.getUserId(), Integer.parseInt(projId), getRoleIdFor(initRole, roles));
				resp.sendRedirect("/BaseBlockSystem/" + Constants.PROJECTS_PATH + "?editProject=" + projId);
				return;
			}
			
	
			if (req.getParameter("editProject") != null && actionIsAllowed(req, Integer.valueOf(req.getParameter("editProject")))) {
				Project p = new Project(Integer.parseInt(req.getParameter("editProject")),req.getParameter("editProjectName") );
				currentProject = p;
				out.println("<a href=\"" + Constants.PROJECTS_PATH + "\" style=\"padding:36px\">BACK</a>"
						+ "<h2>Add new user to project:</h2>\r\n" + 
						"<form id=\"user_form\">\r\n" + 
						"<table id=\"table\">\r\n" + 
						"<tr>\r\n" + 
						"<td><label for=\"projectName\">enter username:</label>\r\n" + 
						"<select id=\"rol_picker\" name=\"userId\" form=\"user_form\">\r\n" + 
						getUserSelectOptions(currentProject.getProjectId()) +
						"                    </select>\r\n" + 
						"</td>\r\n" +
						"<td>\r\n" + 
						"<label for=\"rol_picker\">Pick role:<label>\r\n" + 
						"<select id=\"rol_picker\" name=\"role\" form=\"user_form\">\r\n" + 
						 getRoleSelectOptions() +
						"                    </select>\r\n" + 
						"</td>\r\n" +
						"<td>\n<input type=\"hidden\" name=\"projectId\" value=\"" + currentProject.getProjectId() + "\">\n</td>\n" +
						"<td> \r\n" + 
						
						"<input id=\"addUserBtn\" " + (userCountNotInProject == 0 ? "disabled " : "") + "type=\"submit\" value=\"Add user\">\r\n" + 
						
						"</td>" +
						"</tr>\r\n" + 
						"</table>\r\n" + 
						"</form><br>\r\n" + 
						"<h2>Active users in the project</h2>" +
						"<table id=\"table\"> \r\n" + 
						"<tr>\r\n" + 
						"<th>Username</th>\r\n" + 
						"<th colspan=\"6\">Settings</th>\r\n" + 
						"</tr>\r\n" + 
						"				\r\n" + 
						getUserFormsForProject(p) +
						"				\r\n" + 
						"</table>");
				out.println("<script>");
				out.println("const addUserBtn = document.querySelector('#addUserBtn');");
				out.println("const addUserForm = document.querySelector('#user_form');");
				out.println("addUserBtn.addEventListener('click', () => { addUserBtn.disabled = true; addUserForm.submit(); });");
				out.println("</script>");
				out.println(getFooter());
				return;
			} else if (req.getParameter("editProject") != null && !actionIsAllowed(req, Integer.valueOf(req.getParameter("editProject")))) {
				out.println("<p style=\"background-color:#c0392b;color:white;padding:16px;\">ACTION NOT ALLOWED: " + ", reason: You are not an admin or a projectleader for this project.</p>");
			}
			
			boolean[] allowed = new boolean[plist.size()];
			
			for(int i = 0; i < plist.size(); i++) {
				allowed[i] = actionIsAllowed(req, plist.get(i).getProjectId());
			}
			
			boolean atLeastOneAllowed = false;
			for (int i = 0; i < allowed.length; i++) {
				if (allowed[i]) {
					atLeastOneAllowed = true;
					break;
				}
			}
			
			out.println("<p id=\"head_text\">Projects</p>\n" +
	        "<table id=\"table\">\n" +
	          "<tr>\n" +
	            "<th>Project Name</th>\n" +
	            (atLeastOneAllowed ? "<th colspan=\"2\"> Settings </th>\n" : "") +
	          "</tr>");
			
			boolean isAdmin = isAdmin(req);
			
			for(int i = 0; i < plist.size(); i++) {
				out.print("<tr>\n" + 
							(isAdmin ? "<td>" + plist.get(i).getName() + "</td>" : "<td><a href=\"" + Constants.PROJECTS_PATH + "?pickProjectId=" + plist.get(i).getProjectId() + "\">" + plist.get(i).getName() + "</a></td>\n") + 
							(!allowed[i] ? "" :"<td><a href=\"" + Constants.PROJECTS_PATH + "?editProject=" + plist.get(i).getProjectId()  + "&" + "editProjectName=" + plist.get(i).getName()  +"\"" +  "id=\"editBtn\">edit</a></td>\n") + 
							(!allowed[i]  || !isAdmin? "" :"<td><a href=\"" + Constants.PROJECTS_PATH + "?deleteProjectId=" + plist.get(i).getName() + "\" onclick=" + addQuotes("return confirm('Are you sure you want to delete this project ?')") + ">delete</a></td>\n") +
						"</tr>\n");
			}
			
			if (isAdmin(req)) {
				out.println("<button type=\"button\" id=\"myBtn\">create new project</button>\n" + 
					"        \n" + 
					"        \n" + 
					"        <!-- create-new-project btn popup window -->\n" + 
					"        <div id=\"myModal\" class=\"modal\">   \n" + 
					"            <div class=\"modal-content\">\n" + 
					"                <span class=\"close1\">&times;</span>\n" + 
					"                  <label for=\"projectName\">Project name:</label>\n" +
					"				   <form>" +
					"                  	<input type=\"text\" id=\"projectName\" name=\"projectName\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" minlength=\"3\" maxlength=\"20\" required><br><br>\n" + 
					"                  	<input type=\"submit\" value=\"Create\" onclick=\"create();\">\n" + 
					"					</form>\n" +
					"            </div>\n" + 
					"        </div>\n" +
					"		</table>" +
					"        <!-- create-new-project btn onclick-action (open popup) -->\n" + 
					"        <script>\n" + 
					"            var modal = document.getElementById(\"myModal\");\n" + 
					"            var btn = document.getElementById(\"myBtn\");\n" + 
					"            var span = document.getElementsByClassName(\"close1\")[0];\n" + 
					"            btn.onclick = function() {\n" + 
					"              modal.style.display = \"block\";\n" + 
					"            }\n" + 
					"            span.onclick = function() {\n" + 
					"              modal.style.display = \"none\";\n" + 
					"            }\n" + 
					"            window.onclick = function(event) {\n" + 
					"              if (event.target == modal) {\n" + 
					"                modal.style.display = \"none\";\n" + 
					"              }\n" + 
					"            }\n" + 
					"\n" + 
					"        </script>");
			}
		
		} catch (Exception e) {
			String pname = req.getParameter("projectName");
			if (pname != null && !isBlank(pname)) {
				resp.sendRedirect(Constants.PROJECTS_PATH + "?" + pname + "&" + "success=false");
			}
			e.printStackTrace();
		}
		out.println(getFooter());
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
				if (r.getRoleId() == Constants.PROJECT_LEADER) {
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
	 * Checks whether given string is empty or not.
	 * @param s the string to check.
	 * @return whether the provided string is empty or not.
	 */
    private Boolean isBlank(String s) {
    	return s.length() == 0;
    }
    
    /**
     * Given a project, generates rows for each user to display in HTML.
     * @param project the project to get user forms for.
     * @return the HTML code for the user forms.
     */
	private String getUserFormsForProject(Project project) {
		StringBuilder sbBuilder = new StringBuilder();
		try {
			List<User> projectUsers = dbService.getAllUsers(project.getProjectId());
			
			for (int i = 0; i < projectUsers.size(); i++) {
				Role role = dbService.getRole(projectUsers.get(i).getUserId(), project.getProjectId());
				sbBuilder.append("<tr>\n");
				sbBuilder.append("<form id = \"user_form" + (i + 1) + "\">\n");
				sbBuilder.append("<td>");
				sbBuilder.append(projectUsers.get(i).getUsername());
				sbBuilder.append("</td>\n");
				sbBuilder.append("<td>\n<input type=\"hidden\" name=\"userId\" value=\"" + projectUsers.get(i).getUserId() + "\">\n</td>\n");
				sbBuilder.append("<td>\n");
				sbBuilder.append("</td>\n");
				sbBuilder.append("<td>\n<input type=\"hidden\" name=\"projectId\" value=\"" + project.getProjectId() + "\">\n</td>\n");
				sbBuilder.append("<td>\n");
				sbBuilder.append("<select id=\"rol_picker\" name=\"newRole\" form=\"user_form" + (i+1) +"\">\n");
				sbBuilder.append(getRoleSelectOptions(role));
				sbBuilder.append("                    </select>\r\n" + 
						"                </td>\r\n" + 
						"            <td><input type=\"submit\" value=\"Update Role\"></td>\r\n" + 
						"        </form>\r\n" + 
						"                <td><a href=\"" + Constants.PROJECTS_PATH + "?deleteUserId=" + projectUsers.get(i).getUserId() + "&" + "deleteProjectId=" + project.getProjectId() +"\" onclick=" +  addQuotes("return confirm('Are you sure you want to remove this user from project?')") + ">remove from project</a></td>\r\n" + 
						"            </tr>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sbBuilder.toString();
	}
	
	/**
	 * Gets the roleId given the roles name.
	 * @param name the role name.
	 * @param roles all available roles.
	 * @return the roleId. Will default to 1 if none matching found.
	 */
    private int getRoleIdFor(String name, List<Role> roles) {
    	for (Role role : roles) {
			if (role.getRole().equals(name))
				return role.getRoleId();
		}
    	return 1;
    }
	
	/**
	 * Gets HTML options with given role preselected.
	 * @param projectRole the role to be preselected.
	 * @return the HTML code for role options.
	 */
	private String getRoleSelectOptions(Role projectRole) {
		StringBuilder sbBuilder = new StringBuilder();
		roles = dbService.getAllRoles();
		for (Role role : roles) {
			sbBuilder.append("<option value=\"");
			sbBuilder.append(role.getRole());
			if(projectRole.getRoleId() == role.getRoleId())
				sbBuilder.append("\" selected=\"selected\">");
			else
				sbBuilder.append("\">");
			sbBuilder.append(role.getRole());
			sbBuilder.append("</option>\n");
		}
		
		return sbBuilder.toString();
	}

	
	/**
	 * Gets the options in HTML format for the roles.
	 * @param projectId the project in focus.
	 * @return the HTML code for select options.
	 */
	private String getUserSelectOptions(int projectId) {
		StringBuilder sbBuilder = new StringBuilder();
		userCountNotInProject = 0;
		try {
			List<User> users = dbService.getAllUsers();
			Set<Integer> userIdsInProject = new HashSet<Integer>();
			for (User u : dbService.getAllUsers(projectId)) {
				userIdsInProject.add(u.getUserId());
			}
			for (User user : users) {
				if (!user.isAdmin() && !userIdsInProject.contains(user.getUserId())) { // Admins can not be added to project nor users that already belongs to project
					sbBuilder.append("<option value=\"");
					sbBuilder.append(user.getUsername());
					sbBuilder.append("\">");
					sbBuilder.append(user.getUsername());
					sbBuilder.append("</option>\n");
					userCountNotInProject++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sbBuilder.toString();
	}
	
	/**
	 * Gets the options in HTML format for the roles.
	 * @return the HTML code for select options.
	 */
	private String getRoleSelectOptions() {
		StringBuilder sbBuilder = new StringBuilder();
		for (Role role : dbService.getAllRoles()) {
			sbBuilder.append("<option value=\"");
			sbBuilder.append(role.getRole());
			sbBuilder.append("\">");
			sbBuilder.append(role.getRole());
			sbBuilder.append("</option>\n");
		}
		
		return sbBuilder.toString();
	}
	
	/**
	 * Given a projectId, deletes the project.
	 * @param projectId the project's id.
	 */
	public void deleteProject(int projectId) {
		dbService.deleteProject(projectId);
	}
	
	/**
	 * Given project details, creates the project.
	 * @param proj the project's id.
	 * @return the newly created project.
	 * @throws SQLException if the project could not be created.
	 */
	public Project createProject(Project proj) throws SQLException {
		return dbService.createProject(proj);
	}

}
