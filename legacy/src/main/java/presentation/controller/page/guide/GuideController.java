package presentation.controller.page.guide;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import business.service.guide.GuideService;
import dto.keyboard.GuideDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import presentation.controller.page.Controller;
import util.web.RequestRouter;

/**
 * 키보드 용어집 컨트롤러
 * URL 패턴: /guide.do 형식 지원
 */
@WebServlet({"/guide/*", "/guide.do"})
public class GuideController extends HttpServlet implements Controller { // Controller 인터페이스 구현 명시
    private static final long serialVersionUID = 1L;
    private final GuideService guideService;
    private RequestRouter router;
    private final Gson gson;

    // LocalDateTime을 위한 TypeAdapter 정의
    private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(formatter.format(value));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }

    public GuideController() {
        this.guideService = new GuideService();
        // Gson 객체 생성 시 TypeAdapter 등록
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
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
        router = new RequestRouter();

        // GET 요청 JSON 라우터 설정
        router.getJson("/search", (req, res) -> {
            String keyword = req.getParameter("keyword");
            if (keyword == null || keyword.trim().isEmpty()) {
                // 키워드가 없으면 빈 리스트 반환
                return new java.util.ArrayList<GuideDTO>();
            }
            // 검색 결과 반환
            return guideService.searchGuidesByKeyword(keyword);
        });

        router.getJson("/detail", (req, res) -> {
            String guideIdStr = req.getParameter("guideId");
            if (guideIdStr == null || guideIdStr.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "guideId 파라미터가 필요합니다.");
                // res.setStatus(HttpServletResponse.SC_BAD_REQUEST); // RequestRouter 콜백 내에서 직접 상태 코드 설정은 지양하는 것이 좋다냥.
                // RequestRouter의 handle 메소드나 sendJsonResponse에서 처리하는 것을 고려해볼 수 있다냥.
                return errorResponse;
            }
            try {
                long guideId = Long.parseLong(guideIdStr);
                GuideDTO guide = guideService.getGuideById(guideId);
                if (guide == null) {
                    Map<String, String> notFoundResponse = new HashMap<>();
                    notFoundResponse.put("message", "해당 ID의 용어를 찾을 수 없습니다.");
                    // res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return notFoundResponse;
                }
                return guide;
            } catch (NumberFormatException e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "잘못된 guideId 형식입니다.");
                // res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return errorResponse;
            }
        });

        // 기본 "/" 경로에 대한 핸들러
        router.getJson("/", (req, res) -> {
            return guideService.getAllGuides(); // 모든 가이드 반환
        });
    }

    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 데이터 객체의 내용에 따라 상태 코드를 설정할 수 있다냥.
        // 예를 들어, data가 Map이고 "error" 키를 포함하면 400 또는 404를 설정할 수 있다냥.
        if (data instanceof Map && ((Map<?, ?>) data).containsKey("error")) {
            if ("guideId 파라미터가 필요합니다.".equals(((Map<?, ?>) data).get("error")) || "잘못된 guideId 형식입니다.".equals(((Map<?, ?>) data).get("error"))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else if (data instanceof Map && ((Map<?, ?>) data).containsKey("message") && "해당 ID의 용어를 찾을 수 없습니다.".equals(((Map<?, ?>) data).get("message"))) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // RequestRouter의 handle 메소드를 사용하여 요청 처리
        // handle 메소드가 true를 반환하면 요청이 처리된 것이므로 추가 작업 필요 없음
        if (router.handle(request, response)) {
            return;
        }

        // .do 요청에 대한 기존 로직
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            String action = request.getParameter("action");
            if ("search".equals(action)) {
                searchGuidesByContent(request, response);
            } else if ("detail".equals(action)) {
                getGuideDetail(request, response);
            } else {
                // .do 요청이지만 action이 명시되지 않은 경우, 모든 가이드를 보여주도록 처리
                getGuides(request, response);
            }
            return;
        }

        // RequestRouter가 처리하지 못하고 .do 요청도 아닌 경우 (예: JSP 페이지 직접 요청 등)
        // 여기서는 기본적으로 모든 가이드를 보여주는 것으로 처리
        getGuides(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (router.handle(request, response)) {
            return;
        }
        String action = request.getParameter("action");
        if ("search".equals(action)) {
            searchGuidesByContent(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST method not supported for this URL");
        }
    }

    /**
     * 키보드 용어집 전체 목록을 가져와 JSON으로 응답 (RequestRouter에서 사용되거나 직접 호출될 수 있음)
     */
    private void getGuides(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<GuideDTO> guides = guideService.getAllGuides();
        sendJsonResponse(response, guides);
    }

    /**
     * 키워드로 키보드 용어집 검색하고 JSON으로 응답 (RequestRouter에서 사용되거나 직접 호출될 수 있음)
     */
    private void searchGuidesByContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<GuideDTO> searchResults;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = guideService.searchGuidesByKeyword(keyword);
        } else {
            // 키워드가 없으면 모든 가이드 반환 (또는 빈 리스트/에러 처리)
            searchResults = guideService.getAllGuides();
        }
        sendJsonResponse(response, searchResults);
    }

    /**
     * ID로 특정 키보드 용어 상세 조회하고 JSON으로 응답 (RequestRouter에서 사용되거나 직접 호출될 수 있음)
     */
    private void getGuideDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String guideIdStr = request.getParameter("id");
        try {
            long guideId = Long.parseLong(guideIdStr);
            GuideDTO guide = guideService.getGuideById(guideId);
            if (guide != null) {
                sendJsonResponse(response, guide);
            } else {
                // response.setStatus(HttpServletResponse.SC_NOT_FOUND); // sendJsonResponse에서 처리하도록 변경
                Map<String, String> error = new HashMap<>();
                error.put("message", "해당 ID의 용어를 찾을 수 없습니다."); // "error" 대신 "message"로 보내서 sendJsonResponse에서 구분
                sendJsonResponse(response, error);
            }
        } catch (NumberFormatException e) {
            // response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // sendJsonResponse에서 처리하도록 변경
            Map<String, String> error = new HashMap<>();
            error.put("error", "유효하지 않은 ID 형식입니다.");
            sendJsonResponse(response, error);
        }
    }

    // Controller 인터페이스의 execute 메소드 구현 (필요한 경우)
    // 현재 구조에서는 HttpServlet의 doGet/doPost를 직접 사용하므로,
    // Controller 인터페이스의 execute 메소드가 반드시 필요하지 않을 수 있다냥.
    // 만약 Controller 인터페이스가 execute 메소드를 강제한다면 아래와 같이 구현할 수 있다냥.
    //@Override
    public String execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 요청 메소드에 따라 doGet 또는 doPost 호출
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            doGet(request, response);
        } else if ("POST".equalsIgnoreCase(request.getMethod())) {
            doPost(request, response);
        }
        // execute 메소드는 일반적으로 뷰 이름을 반환하지만,
        // 여기서는 응답을 직접 처리하므로 null을 반환하거나 다른 방식을 사용할 수 있다냥.
        return null;
    }
}