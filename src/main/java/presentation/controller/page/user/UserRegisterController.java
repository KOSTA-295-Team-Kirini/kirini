package presentation.controller.page.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import business.service.user.UserService;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import presentation.controller.page.Controller;

/**
 * 사용자 회원가입 관련 요청을 처리하는 컨트롤러
 * URL 패턴: /signup.do 형식 지원
 */
@WebServlet({"/signup/*", "/signup.do"})
public class UserRegisterController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();
    
    @Override
    public void init() throws ServletException {
        super.init();
        userService = new UserService();
        
        // 라우터 설정
        initRequestRouter();
    }
    
    /**
     * 요청 라우터 초기화
     */    private void initRequestRouter() {
        router = new util.web.RequestRouter();
        
        // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "회원가입 API");
            return result;
        });
        
        // POST 요청 JSON 라우터 설정
        router.postJson("/register", (req, res) -> {
            // 필요한 파라미터 가져오기
            String email = req.getParameter("email");
            String nickname = req.getParameter("nickname");
            String password = req.getParameter("password");
            
            // 비밀번호 확인
            String confirmPassword = req.getParameter("passwordConfirm");
            if (confirmPassword == null) {
                confirmPassword = req.getParameter("password-confirm");
            }
            
            // 유효성 검사
            if (email == null || nickname == null || password == null || 
                (confirmPassword != null && !password.equals(confirmPassword))) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "입력 정보를 확인해주세요.");
                return error;
            }
            
            // 회원 등록
            UserDTO user = new UserDTO(password, email, nickname);
            boolean success = userService.registerUser(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "회원가입이 완료되었습니다.");
                result.put("redirect", "login.html");
            } else {
                result.put("message", "회원가입 처리 중 오류가 발생했습니다.");
            }
            
            return result;
        });
    }
    
    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // 아이디 중복확인 및 비밀번호 유효성 검사 요청 처리
        String action = request.getParameter("action");
          if (action != null) {
            if (action.equals("checkId")) {
                checkDuplicateId(request, response);
                return;
            } else if (action.equals("checkEmail")) {
                checkDuplicateEmail(request, response);
                return;
            } else if (action.equals("checkNickname")) {
                checkDuplicateNickname(request, response);
                return;
            } else if (action.equals("checkPassword")) {
                checkPasswordValidation(request, response);
                return;
            }
        }
        
        // 회원가입 페이지로 이동
        request.getRequestDispatcher("/view/pages/signup.html").forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답 형식으로 처리
        String requestURI = request.getRequestURI();
        boolean isDoRequest = (requestURI != null && requestURI.endsWith(".do"));
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // .do 요청이 아닌 경우 기존 로직 처리
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
     * 이메일 중복 확인
     */
    private void checkDuplicateEmail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String email = request.getParameter("email");
        boolean isDuplicate = userService.checkDuplicateEmail(email);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String message = isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
        response.getWriter().write("{\"isAvailable\": " + !isDuplicate + ", \"message\": \"" + message + "\", \"isDuplicate\": " + isDuplicate + "}");
    }
    
    /**
     * 닉네임 중복 확인
     */    private void checkDuplicateNickname(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String nickname = request.getParameter("nickname");
        boolean isDuplicate = userService.checkDuplicateNickname(nickname);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        response.getWriter().write("{\"isAvailable\": " + !isDuplicate + ", \"message\": \"" + message + "\", \"isDuplicate\": " + isDuplicate + "}");
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
    }    /**
     * 회원가입 처리
     */
    private void registerUserFromSignup(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // JSON 요청 본문 파싱
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            
            // JSON 파싱
            Map<String, Object> jsonMap = gson.fromJson(sb.toString(), Map.class);
            
            // 필요한 파라미터 가져오기
            String email = (String) jsonMap.get("email");
            String nickname = (String) jsonMap.get("nickname");
            String password = (String) jsonMap.get("password");
            
            // 유효성 검사
            if (email == null || nickname == null || password == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "입력 정보를 확인해주세요.");
                sendJsonResponse(response, errorResponse);
                return;
            }
            
            // 회원 등록
            UserDTO user = new UserDTO(password, email, nickname);
            boolean success = userService.registerUser(user);
            
            // 결과 응답
            Map<String, Object> resultResponse = new HashMap<>();
            resultResponse.put("success", success);
            
            if (success) {
                resultResponse.put("message", "회원가입이 완료되었습니다.");
                resultResponse.put("redirect", "login.html");
            } else {
                resultResponse.put("message", "회원가입 처리 중 오류가 발생했습니다.");
            }
            
            sendJsonResponse(response, resultResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            sendJsonResponse(response, errorResponse);
        }
    }
}
