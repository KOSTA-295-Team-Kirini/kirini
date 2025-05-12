package presentation.controller.page;

import java.io.IOException;

import business.service.user.UserService;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 사용자 프로필 관리 컨트롤러
 */
public class UserProfileController implements Controller {
    private UserService userService;
    
    public UserProfileController() {
        userService = new UserService();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        long userId = (long) session.getAttribute("userId");
        UserDTO user = userService.getUserById(userId);
        
        request.setAttribute("user", user);
        request.getRequestDispatcher("/view/pages/profile.jsp").forward(request, response);
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 프로필 업데이트 처리
        updateProfile(request, response);
    }
    
    private void updateProfile(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 세션 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        long userId = (long) session.getAttribute("userId");
        
        // 사용자 정보 가져오기
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"사용자 정보를 찾을 수 없습니다.\"}");
            return;
        }
        
        // 파라미터 추출
        String nickname = request.getParameter("nickname");
        String email = request.getParameter("email");
        String introduce = request.getParameter("introduce");
        
        // 필드 업데이트
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            if (userService.checkDuplicateEmail(email) && !email.equals(user.getEmail())) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"이미 사용 중인 이메일입니다.\"}");
                return;
            }
            user.setEmail(email);
        }
        
        if (introduce != null) {
            user.setIntroduce(introduce);
        }
        
        // 업데이트 처리
        boolean success = userService.updateUser(user);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"프로필이 성공적으로 업데이트되었습니다.\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"프로필 업데이트에 실패했습니다.\"}");
        }
    }
}