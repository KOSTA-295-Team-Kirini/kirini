package presentation.servlet;

import java.io.IOException;

import business.service.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import presentation.controller.page.UserRegisterController;

/**
 * 회원가입 요청을 처리하는 서블릿
 */
@WebServlet("/signup")
@MultipartConfig
public class UserRegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserRegisterController controller;
    private UserService userService;
    
    public UserRegisterServlet() {
        controller = new UserRegisterController();
        userService = new UserService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 컨트롤러에 요청 위임
        controller.doGet(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 컨트롤러에 요청 위임
        controller.doPost(request, response);
    }
}