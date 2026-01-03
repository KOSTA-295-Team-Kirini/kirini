package presentation.controller.dispatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig; // 이 부분 추가
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import presentation.controller.mapper.HandlerMapping;
import presentation.controller.page.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("*.do")
@MultipartConfig // 이 부분 추가
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private HandlerMapping handlerMapping;

    @Override
    public void init() throws ServletException {
        handlerMapping = HandlerMapping.getInstance();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 요청 URI에서 명령어 추출
        String requestURI = request.getRequestURI();
        String command = extractCommand(requestURI);

        System.out.println("DispatcherServlet이 요청 처리: " + command);

        try {
            // 2. 핸들러 매핑에서 컨트롤러 찾기
            Controller controller = handlerMapping.getController(command);

            if (controller == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.");
                return;
            }

            // 3. HTTP 메서드에 따라 컨트롤러 메서드 호출
            String method = request.getMethod();
            if ("GET".equalsIgnoreCase(method)) {
                controller.doGet(request, response);
            } else if ("POST".equalsIgnoreCase(method)) {
                controller.doPost(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.");
            }

        } catch (Exception e) {
            System.out.println("요청 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    private String extractCommand(String requestURI) {
        // /kirini/login.do -> login
        String contextPath = getServletContext().getContextPath();
        String command = requestURI.substring(contextPath.length());

        // 확장자 .do 제거
        if (command.endsWith(".do")) {
            command = command.substring(0, command.length() - 3);
        }

        // 시작 / 제거
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        return command;
    }
}
