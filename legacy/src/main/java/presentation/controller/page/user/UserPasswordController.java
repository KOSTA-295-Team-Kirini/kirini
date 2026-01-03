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
 * 비밀번호 찾기/변경 기능 컨트롤러
 * URL 패턴: /password.do 형식 지원
 */
@WebServlet({"/password/*", "/password.do"})
public class UserPasswordController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();

    public UserPasswordController() {
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
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "비밀번호 관리 API");
            return result;
        });

        // POST 요청 JSON 라우터 설정 - 비밀번호 찾기
        router.postJson("/find", (req, res) -> {
            String email = req.getParameter("email");

            // 유효성 검사
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "이메일을 입력해주세요.");
                return error;
            }

            // 이메일로 사용자 확인
            boolean userExists = userService.checkDuplicateEmail(email);
            if (!userExists) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "등록되지 않은 이메일입니다.");
                return error;
            }

            // 임시 비밀번호 발급 및 이메일 전송
            // TODO: 실제 메서드 구현 필요. 현재는 미구현 상태로 가정
            boolean success = false;  // userService.resetPassword(email);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "이메일로 임시 비밀번호가 발송되었습니다.");
            } else {
                result.put("message", "비밀번호 재설정 기능이 현재 구현 중입니다.");
            }

            return result;
        });

        // POST 요청 JSON 라우터 설정 - 비밀번호 변경
        router.postJson("/change", (req, res) -> {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "로그인이 필요합니다.");
                return error;
            }

            long userId = (long) session.getAttribute("userId");
            String currentPassword = req.getParameter("currentPassword");
            String newPassword = req.getParameter("newPassword");
            String confirmPassword = req.getParameter("confirmPassword");

            // 유효성 검사
            if (currentPassword == null || newPassword == null || confirmPassword == null ||
                    currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "모든 필드를 입력해주세요.");
                return error;
            }

            // 새 비밀번호 일치 확인
            if (!newPassword.equals(confirmPassword)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "새 비밀번호가 일치하지 않습니다.");
                return error;
            }

            // 현재 비밀번호 확인
            UserDTO user = userService.getUserById(userId);
            if (!userService.verifyPassword(user.getEmail(), currentPassword)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "현재 비밀번호가 일치하지 않습니다.");
                return error;
            }

            // 비밀번호 변경
            boolean success = userService.updatePassword(userId, newPassword);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            if (success) {
                result.put("message", "비밀번호가 변경되었습니다.");
            } else {
                result.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
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
            result.put("message", "비밀번호 관리 API");
            sendJsonResponse(response, result);
            return;
        }

        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }

        // 기본 비밀번호 찾기 페이지로 이동
        request.getRequestDispatcher("/view/pages/password/find.jsp").forward(request, response);
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

        // 경로에 따른 처리
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 기본 비밀번호 찾기 처리
            handleFindPassword(request, response);
        } else if (pathInfo.equals("/change")) {
            // 비밀번호 변경 처리
            handleChangePassword(request, response);
        } else {
            // 잘못된 경로
            response.sendError(404);
        }
    }

    private void handleFindPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 유효성 검사
        if (email == null || email.trim().isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"이메일을 입력해주세요.\"}");
            return;
        }

        // 이메일로 사용자 확인
        boolean userExists = userService.checkDuplicateEmail(email);
        if (!userExists) {
            response.getWriter().write("{\"success\": false, \"message\": \"등록되지 않은 이메일입니다.\"}");
            return;
        }

        // 임시 비밀번호 발급 및 이메일 전송 (실제 메서드명에 맞게 변경)
        boolean success = false;
        try {
            // TODO: 실제 UserService 메서드 호출 구현 필요
            // success = userService.resetPassword(email);

            // 현재는 기능 미구현으로 가정
            response.getWriter().write("{\"success\": false, \"message\": \"비밀번호 재설정 기능이 현재 구현 중입니다.\"}");
            return;
        } catch (Exception e) {
            success = false;
        }

        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"이메일로 임시 비밀번호가 발송되었습니다.\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"비밀번호 재설정 처리 중 오류가 발생했습니다.\"}");
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response)
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
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 유효성 검사
        if (currentPassword == null || newPassword == null || confirmPassword == null ||
                currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"모든 필드를 입력해주세요.\"}");
            return;
        }

        // 새 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            response.getWriter().write("{\"success\": false, \"message\": \"새 비밀번호가 일치하지 않습니다.\"}");
            return;
        }

        // 현재 비밀번호 확인
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            response.getWriter().write("{\"success\": false, \"message\": \"사용자 정보를 찾을 수 없습니다.\"}");
            return;
        }

        if (!userService.verifyPassword(userId, currentPassword)) {
            response.getWriter().write("{\"success\": false, \"message\": \"현재 비밀번호가 일치하지 않습니다.\"}");
            return;
        }
        // 비밀번호 변경 (실제 메서드명에 맞게 변경)
        boolean success = userService.updatePassword(userId, newPassword);

        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"비밀번호가 변경되었습니다.\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"비밀번호 변경 중 오류가 발생했습니다.\"}");
        }
    }
}