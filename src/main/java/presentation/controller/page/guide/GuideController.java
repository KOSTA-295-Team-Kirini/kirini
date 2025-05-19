package presentation.controller.page.guide;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
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
public class GuideController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private final GuideService guideService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();
    
    public GuideController() {
        this.guideService = new GuideService();
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
            return guideService.getAllGuides();
        });
        
        router.getJson("/search", (req, res) -> {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.trim().isEmpty()) {
                return guideService.searchGuidesByKeyword(keyword);
            } else {
                return guideService.getAllGuides();
            }
        });
        
        router.getJson("/detail", (req, res) -> {
            String guideIdStr = req.getParameter("id");
            if (guideIdStr == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID 파라미터가 필요합니다.");
                return error;
            }
            
            try {
                long guideId = Long.parseLong(guideIdStr);
                GuideDTO guide = guideService.getGuideById(guideId);
                
                if (guide != null) {
                    return guide;
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "해당 ID의 용어를 찾을 수 없습니다.");
                    return error;
                }
            } catch (NumberFormatException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "유효하지 않은 ID 형식입니다.");
                return error;
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
        out.flush();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // JSON 응답 처리
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "용어집 페이지 API");
            sendJsonResponse(response, result);
            return;
        }
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // 기존 로직 처리
        String action = request.getParameter("action");
        
        if (action == null) {
            // 기본 액션 - 모든 용어집 가져오기
            getGuides(request, response);
        } else if (action.equals("search")) {
            // 검색 액션
            searchGuidesByContent(request, response);
        } else if (action.equals("detail")) {
            // 상세 조회 액션
            getGuideDetail(request, response);
        } else {
            // 기본 액션으로 리다이렉트
            getGuides(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // .do 요청일 경우 JSON 응답 형식으로 처리
        String requestURI = request.getRequestURI();
        boolean isDoRequest = (requestURI != null && requestURI.endsWith(".do"));
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        // POST 요청으로 검색을 처리할 경우
        String action = request.getParameter("action");
        
        if (action != null && action.equals("search")) {
            searchGuidesByContent(request, response);
        } else {
            response.sendRedirect("guide");
        }
    }
    
    /**
     * 키보드 용어집 전체 목록을 가져와 JSON으로 응답
     */
    private void getGuides(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<GuideDTO> guides = guideService.getAllGuides();
        
        // JSON 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // JSON 변환 및 응답 전송
        sendJsonResponse(response, guides);
    }
    
    /**
     * 키워드로 키보드 용어집 검색하고 JSON으로 응답
     */
    private void searchGuidesByContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<GuideDTO> searchResults;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = guideService.searchGuidesByKeyword(keyword);
        } else {
            // 키워드가 없으면 전체 목록
            searchResults = guideService.getAllGuides();
        }
        
        // JSON 응답 전송
        sendJsonResponse(response, searchResults);
    }
    
    /**
     * ID로 특정 키보드 용어 상세 조회하고 JSON으로 응답
     */
    private void getGuideDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String guideIdStr = request.getParameter("id");
        
        try {
            long guideId = Long.parseLong(guideIdStr);
            GuideDTO guide = guideService.getGuideById(guideId);
            
            if (guide != null) {
                // 성공 응답
                sendJsonResponse(response, guide);
            } else {
                // 해당 ID의 용어를 찾을 수 없음 - 에러 응답
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, String> error = new HashMap<>();
                error.put("error", "해당 ID의 용어를 찾을 수 없습니다.");
                sendJsonResponse(response, error);
            }
        } catch (NumberFormatException e) {
            // ID가 유효하지 않음 - 에러 응답
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("error", "유효하지 않은 ID 형식입니다.");
            sendJsonResponse(response, error);
        }
    }
}