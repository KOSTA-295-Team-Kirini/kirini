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
 * 비밀번호 찾기/변경 기능 컨트롤러
 */
public class UserPasswordController implements Controller {
    private UserService userService;
    
    public UserPasswordController() {
        userService = new UserService();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("reset".equals(action)) {
            // 비밀번호 재설정 페이지로 이동
            request.getRequestDispatcher("/view/pages/password-reset.jsp").forward(request, response);
        } else if ("change".equals(action)) {
            // 비밀번호 변경 페이지로 이동 (로그인 상태에서)
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.getRequestDispatcher("/view/pages/password-change.jsp").forward(request, response);
        } else {
            // 기본 - 비밀번호 찾기 페이지로 이동
            request.getRequestDispatcher("/view/pages/password-find.jsp").forward(request, response);
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("find".equals(action)) {
            // 비밀번호 찾기 처리
            findPassword(request, response);
        } else if ("reset".equals(action)) {
            // 비밀번호 재설정 처리
            resetPassword(request, response);
        } else if ("change".equals(action)) {
            // 비밀번호 변경 처리
            changePassword(request, response);
        }
    }
    
    // 비밀번호 찾기 처리
    private void findPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 구현 필요
    }
    
    // 비밀번호 재설정 처리
    private void resetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 구현 필요
    }
    
    // 비밀번호 변경 처리
    private void changePassword(HttpServletRequest request, HttpServletResponse response) 
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
        
        // 파라미터 추출
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // 필수 입력값 검증
        if (currentPassword == null || newPassword == null || confirmPassword == null ||
            currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"모든 필드를 입력해주세요.\"}");
            return;
        }
        
        // 현재 비밀번호 확인
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"사용자 정보를 찾을 수 없습니다.\"}");
            return;
        }
        
        // 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"새 비밀번호가 일치하지 않습니다.\"}");
            return;
        }
        
        // 비밀번호 유효성 검사
        if (!userService.checkPasswordValidation(newPassword)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다.\"}");
            return;
        }
        
        // 비밀번호 변경
        boolean success = userService.updatePassword(userId, newPassword);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"비밀번호가 성공적으로 변경되었습니다.\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"비밀번호 변경에 실패했습니다.\"}");
        }
    }
}