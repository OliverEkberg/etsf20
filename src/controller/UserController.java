package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import baseblocksystem.servletBase;

/**
 * Servlet implementation class UserController
 * 
 * A xx page. 
 * 
 * Description of the class.
 * 
 * @author Ferit B�lezek ( Enter name if you've messed around with this file ;) )
 * @version 1.0
 * 
 */

@WebServlet("/UserPage")
public class UserController extends servletBase {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		//test comment
		
	}

}
