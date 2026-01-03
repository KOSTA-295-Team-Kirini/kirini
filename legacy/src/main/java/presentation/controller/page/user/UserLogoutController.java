package presentation.controller.page.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;

/**
 * 로그아웃 처리를 담당하는 컨트롤러
 * URL 패턴: /logout.do
 */
@WebServlet({"/logout/*", "/logout.do"})
public class UserLogoutController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 세션 무효화 (로그아웃)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // JSON 응답인지 확인
        boolean isJsonRequest = request.getRequestURI().endsWith(".do");

        if (isJsonRequest) {
            // JSON 응답 전송
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "로그아웃 되었습니다.");

            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(result));
            out.flush();
        } else {
            // 홈페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/index.html");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST 요청도 GET과 동일하게 처리
        doGet(request, response);
    }
}
