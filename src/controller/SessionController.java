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
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"StyleSheets/SessionController.css\">");
		
		out.println("    <div class=\"wrapper\">\r\n" + 
				"        <div class=\"credentials_form\">\r\n" + 
				"            Hello World\r\n" + 
				"        </div>\r\n" + 
				"    </div>"
				);

		out.println("</body></html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	doGet(req, resp);
    }
}
