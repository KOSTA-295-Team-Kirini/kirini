package presentation.controller.page.user;

import java.io.IOException;

import business.service.user.UserService;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;

/**
 * 로그인 처리를 담당하는 컨트롤러
 */
public class UserLoginController implements Controller {
    private UserService userService;
    
    public UserLoginController() {
        userService = new UserService();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 페이지로 포워딩
        request.getRequestDispatcher("/view/pages/login.html").forward(request, response);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 필요한 파라미터 가져오기
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 유효성 검사
        if (email == null || password == null || 
            email.trim().isEmpty() || password.trim().isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"이메일과 비밀번호를 입력해주세요.\"}");
            return;
        }
        
        try {
            // 이메일과 비밀번호로 로그인
            UserDTO user = userService.login(email, password);
            
            if (user != null) {
                // 로그인 성공 처리
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userNickname", user.getNickname());
                session.setAttribute("userLevel", user.getUserLevel());
                
                response.getWriter().write("{\"success\": true, \"message\": \"로그인 성공\", \"redirect\": \"index.html\"}");
            } else {
                // 로그인 실패 처리
                response.getWriter().write("{\"success\": false, \"message\": \"이메일 또는 비밀번호가 일치하지 않습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"로그인 처리 중 오류가 발생했습니다.\"}");
        }
    }
}
