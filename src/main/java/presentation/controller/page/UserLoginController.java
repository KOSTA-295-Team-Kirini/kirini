package presentation.controller.page;

import java.io.IOException;

import business.service.user.UserService;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 사용자 로그인 관련 요청을 처리하는 컨트롤러
 */
public class UserLoginController implements Controller {
    private UserService userService;
    
    public UserLoginController() {
        userService = new UserService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 상태 확인 요청 처리
        String action = request.getParameter("action");
        
        if (action != null && action.equals("status")) {
            getLoginStatus(request, response);
            return;
        }
        
        // 로그인 페이지로 이동
        request.getRequestDispatcher("/view/pages/login.html").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 요청 처리
        String action = request.getParameter("action");
        
        if (action != null && action.equals("logout")) {
            logout(request, response);
            return;
        }
        
        // 기본 액션은 로그인
        login(request, response);
    }
    
    /**
     * 로그인 처리
     */
    private void login(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String redirectUrl = request.getParameter("redirect"); // 로그인 후 리다이렉트할 URL
        
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"아이디와 비밀번호를 입력해주세요.\"}");
            return;
        }
        
        UserDTO user = userService.login(username, password);
        
        if (user != null) {
            // 로그인 성공 - 세션에 사용자 정보 저장
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("loggedIn", true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userLevel", user.getUserLevel());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                response.getWriter().write("{\"success\": true, \"redirect\": \"" + redirectUrl + "\"}");
            } else {
                response.getWriter().write("{\"success\": true, \"redirect\": \"/\"}");
            }
        } else {
            // 로그인 실패
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"아이디 또는 비밀번호가 일치하지 않습니다.\"}");
        }
    }
    
    /**
     * 로그아웃 처리
     */
    private void logout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 홈페이지로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/");
    }
    
    /**
     * 로그인 상태 확인
     */
    private void getLoginStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = false;
        String username = "";
        int userLevel = 0;
        
        if (session != null && session.getAttribute("loggedIn") != null) {
            isLoggedIn = (boolean) session.getAttribute("loggedIn");
            username = (String) session.getAttribute("username");
            userLevel = (int) session.getAttribute("userLevel");
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"loggedIn\": " + isLoggedIn + 
                                  ", \"username\": \"" + username + 
                                  "\", \"userLevel\": " + userLevel + "}");
    }
}
