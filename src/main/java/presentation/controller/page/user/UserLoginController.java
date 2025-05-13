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
        // 디버깅용 로그
        System.out.println("로그인 요청 수신:");
        System.out.println("email: " + request.getParameter("email"));
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 필수 파라미터 확인
        if (email == null || password == null || 
            email.trim().isEmpty() || password.trim().isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"이메일과 비밀번호를 입력해주세요.\"}");
            return;
        }
        
        try {
            // 이메일로 사용자 정보 조회
            UserDTO user = userService.getUserByEmail(email);
            
            if (user == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"등록되지 않은 이메일입니다.\"}");
                return;
            }
            
            // 비밀번호 검증
            boolean isPasswordValid = userService.verifyPassword(password, user.getPassword());
            
            if (!isPasswordValid) {
                response.getWriter().write("{\"success\": false, \"message\": \"비밀번호가 일치하지 않습니다.\"}");
                return;
            }
            
            // 로그인 성공 처리
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());
            
            // 성공 응답
            response.getWriter().write("{\"success\": true, \"message\": \"로그인 성공\", \"redirect\": \"/kirini/view/index.html\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\": false, \"message\": \"로그인 처리 중 오류가 발생했습니다.\"}");
        }
    }
}
