package presentation.controller.page.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import business.service.user.UserService;
import com.google.gson.Gson;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import util.logging.LoggerConfig;
import util.web.RequestRouter;

/**
 * 로그인 처리를 담당하는 컨트롤러
 * URL 패턴: /login.do 형식 지원
 */
@WebServlet({"/login/*", "/login.do"})
public class UserLoginController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private RequestRouter router;
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
        router = new RequestRouter();
        
        // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "로그인 API");
            return result;
        });
        
        // POST 요청 JSON 라우터 설정
        router.postJson("/login", (req, res) -> {
            // 필요한 파라미터 가져오기
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            
            // 유효성 검사
            if (email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty()) {
                
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "이메일과 비밀번호를 입력해주세요.");
                return errorResult;
            }
            
            try {
                // 이메일과 비밀번호로 로그인
                UserDTO user = userService.login(email, password);
                
                Map<String, Object> result = new HashMap<>();
                
                if (user != null) {
                    // 로그인 성공 처리
                    HttpSession session = req.getSession();
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getUserId());
                    
                    result.put("success", true);
                    result.put("message", "로그인 성공");
                    result.put("redirect", "index.html");
                    result.put("user", user);
                } else {
                    result.put("success", false);
                    result.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
                }
                
                return result;
            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "로그인 처리 중 오류가 발생했습니다.");
                return errorResult;
            }
        });
    }

    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // JSON 응답 처리
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "로그인 페이지 API");
            sendJsonResponse(response, result);
            return;
        }
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }

        // 일반 요청은 기존 방식대로 처리
        request.getRequestDispatcher("/view/pages/login.html").forward(request, response);    }
    
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
        
        // 필요한 파라미터 가져오기
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 유효성 검사
        if (email == null || password == null ||
            email.trim().isEmpty() || password.trim().isEmpty()) {

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "이메일과 비밀번호를 입력해주세요.");

            if (isDoRequest) {
                sendJsonResponse(response, errorResult);
            } else {
                response.getWriter().write(gson.toJson(errorResult));
            }
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

                Map<String, Object> successResult = new HashMap<>();
                successResult.put("success", true);
                successResult.put("message", "로그인 성공");
                successResult.put("redirect", "index.html");

                if (isDoRequest) {
                    sendJsonResponse(response, successResult);
                } else {
                    response.getWriter().write(gson.toJson(successResult));
                }
            } else {
                // 로그인 실패 처리
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "이메일 또는 비밀번호가 일치하지 않습니다.");

                if (isDoRequest) {
                    sendJsonResponse(response, errorResult);
                } else {                response.getWriter().write(gson.toJson(errorResult));
                }
            }
        } catch (Exception e) {
            LoggerConfig.logError(UserLoginController.class, "doPost", "로그인 처리 중 오류 발생", e);
            
            // 오류 응답 처리
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "로그인 처리 중 오류가 발생했습니다.");
            
            if (isDoRequest) {
                sendJsonResponse(response, errorResult);
            } else {
                response.getWriter().write(gson.toJson(errorResult));
            }
        }
    }
}
