package controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;

/**
 * Servlet implementation class SessionController
 * 
 * A xx page. 
 * 
 * Description of the class.
 * 
 * @author Ferit Bölezek ( Enter name if you've messed around with this file ;) )
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
		out.println(getPageIntro());
		

		String name = req.getParameter("username"); // get the string that the user entered in the form
		String password = req.getParameter("password"); // get the entered password
		
		System.out.println(name);
		System.out.println(password);
        
        
		if (name != null && password != null) {
			
			// TODO: Login check...
			
			out.println(loginRequestForm()); // TODO: Remove this later.
			
		} else { 
			out.println(loginRequestForm());
		}	
		
    }
		
	private String loginRequestForm() {
			return 	"<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/SessionController.css\">" +
			
			    "    <div class=\"wrapper\">\r\n" + 
			    "        <div class=\"title\">TimeKeep</div>\r\n" + 
			    "        <div class=\"subTitle\">Keep track of time and stuff.</div>\r\n" + 
			    "        <div class=\"center credentials_form\">\r\n" + 
			    "            <form onsubmit=\"checkInput()\">\r\n" + 
			    "                <input class=\"credentials_rect\" type=\"text\" id=\"username\" name=\"username\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Username\" required><br>\r\n" + 
			    "                <input class=\"credentials_rect\" type=\"password\" id=\"password\" name=\"password\" pattern=\"^[a-zA-Z0-9]*$\" title=\"Please enter letters and numbers only.\" maxlength=\"10\" placeholder=\"Password\" required><br><br>\r\n" + 
			    "                <input class=\"submitBtn\" type=\"submit\" value=\"Submit\">\r\n" + 
			    "              </form> \r\n" + 
			    "        </div>\r\n" + 
			    "        <div class=\"footerText\">Developed by some dudes at LTH.</div>\r\n" + 
			    "    </div>"

			+"</body></html>";
	}
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	doGet(req, resp);
    }
}
