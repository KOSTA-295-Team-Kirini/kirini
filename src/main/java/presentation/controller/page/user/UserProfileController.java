package presentation.controller.page.user;

import java.io.IOException;
import java.io.PrintWriter;
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
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import util.web.RequestRouter;

/**
 * 사용자 프로필 관리 컨트롤러
 * URL 패턴: /profile.do 형식 지원
 */
@WebServlet({"/profile/*", "/profile.do"})
public class UserProfileController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();
    
    public UserProfileController() {
        userService = new UserService();
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // 라우터 설정
        initRequestRouter();
    }
    
    /**
     * 요청 라우터 초기화
     */
    private void initRequestRouter() {
        router = new util.web.RequestRouter();
        
        // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "로그인이 필요합니다.");
                return error;
            }
            
            long userId = (long) session.getAttribute("userId");
            UserDTO user = userService.getUserById(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", user);
            return result;
        });
        
        // POST 요청 JSON 라우터 설정
        router.postJson("/update", (req, res) -> {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "로그인이 필요합니다.");
                return error;
            }
            
            long userId = (long) session.getAttribute("userId");
            String nickname = req.getParameter("nickname");
            
            // 유효성 검사
            if (nickname == null || nickname.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "닉네임을 입력해주세요.");
                return error;
            }
            
            // 닉네임 중복 확인
            if (userService.checkDuplicateNickname(nickname) && 
                !userService.getUserById(userId).getNickname().equals(nickname)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이미 사용 중인 닉네임입니다.");
                return error;
            }
              // 프로필 업데이트
            UserDTO user = userService.getUserById(userId);
            user.setNickname(nickname);
            
            boolean success = userService.updateUser(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "프로필이 업데이트되었습니다.");
            } else {
                result.put("message", "프로필 업데이트에 실패했습니다.");
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
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // JSON 응답 처리
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "프로필 페이지 API");
            sendJsonResponse(response, result);
            return;
        }
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // 기존 로직 처리
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
        // .do 요청일 경우 JSON 응답 형식으로 처리
        String requestURI = request.getRequestURI();
        boolean isDoRequest = (requestURI != null && requestURI.endsWith(".do"));
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // 프로필 업데이트 처리
        updateProfile(request, response);
    }
    
    private void updateProfile(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 세션 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 필요한 파라미터 가져오기
        long userId = (long) session.getAttribute("userId");
        String nickname = request.getParameter("nickname");
        
        // 유효성 검사
        if (nickname == null || nickname.trim().isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"닉네임을 입력해주세요.\"}");
            return;
        }
        
        // 닉네임 중복 확인
        if (userService.checkDuplicateNickname(nickname) && 
            !userService.getUserById(userId).getNickname().equals(nickname)) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"이미 사용 중인 닉네임입니다.\"}");
            return;
        }
        
        // 프로필 업데이트
        UserDTO user = userService.getUserById(userId);
        user.setNickname(nickname);
        
        boolean success = userService.updateUser(user);
        
        response.setContentType("application/json");
        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"프로필이 업데이트되었습니다.\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"프로필 업데이트에 실패했습니다.\"}");
        }
    }
}