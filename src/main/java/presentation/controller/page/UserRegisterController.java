package presentation.controller.page;

import java.io.IOException;

import business.service.user.UserService;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 사용자 회원가입 관련 요청을 처리하는 컨트롤러
 */
public class UserRegisterController implements Controller {
    private UserService userService;
    
    public UserRegisterController() {
        userService = new UserService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 아이디 중복확인 및 비밀번호 유효성 검사 요청 처리
        String action = request.getParameter("action");
        
        if (action != null) {
            if (action.equals("checkId")) {
                checkDuplicateId(request, response);
                return;
            } else if (action.equals("checkEmail")) {  // 새로 추가: 이메일 중복 체크
                checkDuplicateEmail(request, response);
                return;
            } else if (action.equals("checkNickname")) {  // 새로 추가: 닉네임 중복 체크
                checkDuplicateNickname(request, response);
                return;
            } else if (action.equals("checkPassword")) {
                checkPasswordValidation(request, response);
                return;
            }
        }
        
        // 회원가입 페이지로 이동 - 경로 수정
        request.getRequestDispatcher("/view/pages/signup.html").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 회원가입 처리 메소드 수정
        registerUserFromSignup(request, response);
    }
    
    /**
     * 아이디 중복 확인
     */
    private void checkDuplicateId(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        boolean isDuplicate = userService.checkDuplicateId(username);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String message = isDuplicate ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";
        response.getWriter().write("{\"success\": " + !isDuplicate + ", \"message\": \"" + message + "\", \"isDuplicate\": " + isDuplicate + "}");
    }
    
    /**
     * 이메일 중복 확인 (새로 추가)
     */
    private void checkDuplicateEmail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        boolean isDuplicate = userService.checkDuplicateEmail(email);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String message = isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
        response.getWriter().write("{\"success\": " + !isDuplicate + ", \"message\": \"" + message + "\", \"isDuplicate\": " + isDuplicate + "}");
    }
    
    /**
     * 닉네임 중복 확인 (새로 추가)
     */
    private void checkDuplicateNickname(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String nickname = request.getParameter("nickname");
        boolean isDuplicate = userService.checkDuplicateNickname(nickname); // 실제 메서드 호출
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        response.getWriter().write("{\"success\": " + !isDuplicate + ", \"message\": \"" + message + "\", \"isDuplicate\": " + isDuplicate + "}");
    }
    
    /**
     * 비밀번호 유효성 검사
     */
    private void checkPasswordValidation(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String password = request.getParameter("password");
        boolean isValid = userService.checkPasswordValidation(password);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String message = isValid ? "사용 가능한 비밀번호입니다." : "비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다.";
        response.getWriter().write("{\"success\": " + isValid + ", \"message\": \"" + message + "\", \"isValid\": " + isValid + "}");
    }
    
    /**
     * 회원가입 처리 - signup.html 양식에 맞춤
     */
    private void registerUserFromSignup(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 디버깅용 로그 추가
        System.out.println("수신한 파라미터들:");
        System.out.println("email: " + request.getParameter("email"));
        System.out.println("nickname: " + request.getParameter("nickname"));
        System.out.println("password: " + request.getParameter("password"));
        System.out.println("password-confirm: " + request.getParameter("password-confirm"));
        System.out.println("passwordConfirm: " + request.getParameter("passwordConfirm"));
        System.out.println("agree: " + request.getParameter("agree"));

        // 필드명 처리 유연하게 수정
        String email = request.getParameter("email");
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");

        // 두 가지 필드명 모두 시도
        String confirmPassword = request.getParameter("passwordConfirm");
        if (confirmPassword == null) {
            confirmPassword = request.getParameter("password-confirm");
        }

        String agree = request.getParameter("agree");

        // 유효성 검사
        if (email == null || nickname == null || password == null || confirmPassword == null || agree == null) {
            response.getWriter().write("{\"success\": false, \"message\": \"필수 정보가 누락되었습니다.\"}");
            return;
        }
        
        // 비밀번호 일치 확인
        if (!password.equals(confirmPassword)) {
            response.getWriter().write("{\"success\": false, \"message\": \"비밀번호가 일치하지 않습니다.\"}");
            return;
        }
        
        // username 생성 (이메일에서 @ 앞부분 사용)
        String username = email.split("@")[0];
        
        // 회원 등록
        UserDTO user = new UserDTO(username, password, email, nickname);
        boolean success = userService.registerUser(user);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"회원가입이 완료되었습니다.\", \"redirect\": \"login.html\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"회원가입 처리 중 오류가 발생했습니다.\"}");
        }
    }
}
