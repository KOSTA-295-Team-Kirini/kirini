package presentation.controller.page.guide;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;
import business.service.guide.GuideService;
import dto.keyboard.GuideDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import presentation.controller.page.Controller;

public class GuideController implements Controller {
    
    private final GuideService guideService;
    
    public GuideController() {
        this.guideService = new GuideService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        
        // Gson을 사용하여 JSON 변환
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(guides);
        
        // 응답 전송
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
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
        
        // JSON 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Gson을 사용하여 JSON 변환
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(searchResults);
        
        // 응답 전송
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }
    
    /**
     * ID로 특정 키보드 용어 상세 조회하고 JSON으로 응답
     */
    private void getGuideDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String guideIdStr = request.getParameter("id");
        
        // JSON 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        
        try {
            long guideId = Long.parseLong(guideIdStr);
            GuideDTO guide = guideService.getGuideById(guideId);
            
            if (guide != null) {
                // 성공 응답
                String jsonResponse = gson.toJson(guide);
                out.print(jsonResponse);
            } else {
                // 해당 ID의 용어를 찾을 수 없음 - 에러 응답
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"해당 ID의 용어를 찾을 수 없습니다.\"}");
            }
        } catch (NumberFormatException e) {
            // ID가 유효하지 않음 - 에러 응답
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"유효하지 않은 ID 형식입니다.\"}");
        }
        
        out.flush();
    }
}