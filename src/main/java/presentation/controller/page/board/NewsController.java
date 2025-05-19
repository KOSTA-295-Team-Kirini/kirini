package presentation.controller.page.board;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import business.service.news.NewsService;
import dto.board.NewsDTO;
import dto.board.NewsCommentDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.web.IpUtil;
import util.web.RequestRouter;
import presentation.controller.page.Controller;

/**
 * 키보드 소식 게시판 관련 요청을 처리하는 컨트롤러
 * URL 패턴: /news.do 형식 지원
 */
@WebServlet({"/news/*", "/news.do"})
public class NewsController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private NewsService newsService;
    private util.web.RequestRouter router;
    
    @Override
    public void init() throws ServletException {
        super.init();
        newsService = new NewsService();
        
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
            result.put("message", "뉴스 게시판 API");
            return result;
        });
          router.getJson("/list", (req, res) -> {
            int page = 1;
            int pageSize = 10;
            
            try {
                if (req.getParameter("page") != null) {
                    page = Integer.parseInt(req.getParameter("page"));
                }
                
                if (req.getParameter("pageSize") != null) {
                    pageSize = Integer.parseInt(req.getParameter("pageSize"));
                }
            } catch (NumberFormatException e) {
                // 기본값 사용
            }
            
            List<NewsDTO> newsList = newsService.getAllNews(page, pageSize);
            int totalCount = newsService.getTotalNewsCount();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("newsList", newsList);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalCount", totalCount);
            
            return result;
        });
          router.getJson("/view", (req, res) -> {
            try {
                long newsId = Long.parseLong(req.getParameter("id"));
                NewsDTO news = newsService.getNewsById(newsId);
                
                if (news == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "뉴스를 찾을 수 없습니다.");
                    return errorResult;
                }
                  Map<String, Object> result = new HashMap<>();
                result.put("news", news);
                
                // 댓글 목록도 함께 조회
                List<NewsCommentDTO> comments = newsService.getNewsComments(newsId);
                result.put("comments", comments);
                
                return result;
            } catch (NumberFormatException e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "잘못된 뉴스 ID입니다.");
                return errorResult;
            }
        });
        
        // POST 요청 JSON 라우터 설정은 추후 필요에 따라 추가
    }
    
    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(data));
        out.flush();
    }
      @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // JSON 응답 처리
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "뉴스 게시판 API");
            sendJsonResponse(response, result);
            return;
        }
        
        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }
        
        if (action == null) {
            // 기본값은 목록 보기
            action = "list";
        }
        
        try {
            switch (action) {
                case "list":
                    listNews(request, response);
                    break;
                case "view":
                    viewNews(request, response);
                    break;
                case "write":
                    writeForm(request, response);
                    break;
                case "edit":
                    editForm(request, response);
                    break;
                case "toggle-notice":
                    toggleNotice(request, response);
                    break;
                default:
                    listNews(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            // 기본값은 목록 보기로 리다이렉트
            response.sendRedirect("news.do");
            return;
        }
        
        try {
            switch (action) {
                case "write":
                    createNews(request, response);
                    break;
                case "edit":
                    updateNews(request, response);
                    break;
                case "delete":
                    deleteNews(request, response);
                    break;
                case "recommend":
                    recommendNews(request, response);
                    break;
                case "toggleNotice":
                    toggleNotice(request, response);
                    break;
                default:
                    response.sendRedirect("news.do");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * 뉴스 목록을 조회하여 목록 페이지로 포워딩
     */
    private void listNews(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 페이지네이션을 위한 파라미터 처리
        int page = 1;
        int pageSize = 10;
        
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 숫자가 아닌 값이 들어온 경우 기본값 사용
            }
        }
        
        // 공지사항 및 일반 게시글 목록 가져오기
        List<NewsDTO> notificationList = newsService.getAllNews(1, 100); // 공지사항 목록 가져오기 - 최대 100개
        List<NewsDTO> newsList = newsService.getAllNews(page, pageSize);
        int totalPages = (int) Math.ceil(newsService.getTotalNewsCount() / (double) pageSize);
        
        // 요청 속성에 설정
        request.setAttribute("notificationList", notificationList);
        request.setAttribute("newsList", newsList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        
        // 목록 페이지로 포워딩
        request.getRequestDispatcher("/view/pages/news/list.jsp").forward(request, response);
    }
    
    /**
     * 특정 뉴스 게시글 조회
     */
    private void viewNews(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        String newsIdStr = request.getParameter("id");
        
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }
        
        try {
            long newsId = Long.parseLong(newsIdStr);
            
            // 조회수 증가
            // NewsDAO에서 이미 getNewsById 내부에서 조회수를 증가시키므로 별도 호출은 필요 없음
            
            // 게시글 정보 가져오기
            NewsDTO news = newsService.getNewsById(newsId);
            
            if (news == null || "deleted".equals(news.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }
            
            // 요청 속성에 설정
            request.setAttribute("news", news);
            
            // 상세 페이지로 포워딩
            request.getRequestDispatcher("/view/pages/news/view.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }
    
    /**
     * 글 작성 폼을 표시
     */
    private void writeForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            // 로그인되지 않은 경우
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }
        
        // 작성 폼으로 포워딩
        request.getRequestDispatcher("/view/pages/news/write.jsp").forward(request, response);
    }
    
    /**
     * 글 수정 폼을 표시
     */
    private void editForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            // 로그인되지 않은 경우
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }
        
        String newsIdStr = request.getParameter("id");
        
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }
        
        try {
            long newsId = Long.parseLong(newsIdStr);
            NewsDTO news = newsService.getNewsById(newsId);
            
            if (news == null || "deleted".equals(news.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }
            
            // 작성자 또는 관리자만 수정 가능
            if (news.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                return;
            }
            
            // 요청 속성에 설정
            request.setAttribute("news", news);
            
            // 수정 폼으로 포워딩
            request.getRequestDispatcher("/view/pages/news/edit.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }
    
    /**
     * 새 뉴스 게시글 생성
     */
    private void createNews(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }
        
        // 폼 데이터 가져오기
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String notifyValue = request.getParameter("notify");
        
        // 데이터 유효성 검사
        if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
            request.setAttribute("error", "제목과 내용을 모두 입력해야 합니다.");
            request.getRequestDispatcher("/view/pages/news/write.jsp").forward(request, response);
            return;
        }
        
        // 새 게시글 DTO 생성
        NewsDTO news = new NewsDTO();
        news.setNewsTitle(title);
        news.setNewsContents(content);
        news.setUserId(user.getUserId());
        news.setNewsAuthorIp(IpUtil.getClientIpAddr(request));
        
        // 관리자인 경우 공지사항으로 설정 가능
        if (user.getUserLevel() >= 3 && "notification".equals(notifyValue)) {
            news.setNewsNotify("notification");
        } else {
            news.setNewsNotify("common");
        }
        
        // 저장
        // NewsService의 postNews 메서드 사용
        long newsId = newsService.postNews(news, user.getUserLevel() >= 3 ? "admin" : "user") ? news.getNewsId() : -1;
        
        // 상세 페이지로 리다이렉트
        response.sendRedirect("news.do?action=view&id=" + newsId);
    }
    
    /**
     * 뉴스 게시글 수정
     */
    private void updateNews(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }
        
        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }
        
        try {
            long newsId = Long.parseLong(newsIdStr);
            
            // 원본 게시글 가져오기
            NewsDTO original = newsService.getNewsById(newsId);
            
            if (original == null || "deleted".equals(original.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }
            
            // 작성자 또는 관리자만 수정 가능
            if (original.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                return;
            }
            
            // 폼 데이터 가져오기
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String notifyValue = request.getParameter("notify");
            
            // 데이터 유효성 검사
            if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
                request.setAttribute("error", "제목과 내용을 모두 입력해야 합니다.");
                request.setAttribute("news", original);
                request.getRequestDispatcher("/view/pages/news/edit.jsp").forward(request, response);
                return;
            }
            
            // 게시글 수정
            original.setNewsTitle(title);
            original.setNewsContents(content);
            
            // 관리자인 경우 공지사항으로 변경 가능
            if (user.getUserLevel() >= 3 && "notification".equals(notifyValue)) {
                original.setNewsNotify("notification");
            } else if (user.getUserLevel() >= 3) {
                original.setNewsNotify(notifyValue); // 관리자는 상태 변경 가능
            }
            
            // 저장
            newsService.updateNewsById(original, user.getUserLevel() >= 3 ? "admin" : "user");
            
            // 상세 페이지로 리다이렉트
            response.sendRedirect("news.do?action=view&id=" + newsId);
            
        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }
    
    /**
     * 뉴스 게시글 삭제
     */
    private void deleteNews(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }
        
        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }
        
        try {
            long newsId = Long.parseLong(newsIdStr);
            
            // 원본 게시글 가져오기
            NewsDTO original = newsService.getNewsById(newsId);
            
            if (original == null || "deleted".equals(original.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }
            
            // 작성자 또는 관리자만 삭제 가능
            if (original.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "삭제 권한이 없습니다.");
                return;
            }
            
            // 게시글 삭제
            newsService.deleteNewsById(newsId, user.getUserId(), user.getUserLevel() >= 3 ? "admin" : "user");
            
            // 목록 페이지로 리다이렉트
            response.sendRedirect("news.do");
            
        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }
    
    /**
     * 게시글 추천
     */
    private void recommendNews(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"게시글 ID가 필요합니다.\"}");
            return;
        }
        
        try {
            long newsId = Long.parseLong(newsIdStr);
            
            // 게시글 존재 여부 확인
            NewsDTO news = newsService.getNewsById(newsId);
            
            if (news == null || "deleted".equals(news.getNewsDeleted())) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"존재하지 않는 게시글입니다.\"}");
                return;
            }
            
            // 본인 게시글 추천 방지
            if (news.getUserId() == user.getUserId()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"자신의 게시글은 추천할 수 없습니다.\"}");
                return;
            }
            
            // 이미 추천했는지 확인
            boolean alreadyRecommended = isAlreadyRecommended(newsId, user.getUserId());
            
            if (alreadyRecommended) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"이미 추천한 게시글입니다.\"}");
                return;
            }
            
            // 추천 처리
            newsService.recommendNewsById(newsId, user.getUserId());
            
            // 업데이트된 추천 수 가져오기
            NewsDTO updatedNews = newsService.getNewsById(newsId);
            int recommendCount = updatedNews != null ? updatedNews.getNewsRecommend() : 0;
            
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true, \"message\": \"추천이 완료되었습니다.\", \"count\": " + recommendCount + "}");
            
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 게시글 ID입니다.\"}");
        }
    }
    
    /**
     * 공지사항 설정/해제 토글
     */
    private void toggleNotice(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        // 관리자 권한 확인
        if (user.getUserLevel() < 3) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"관리자만 공지사항 상태를 변경할 수 있습니다.\"}");
            return;
        }
        
        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"게시글 ID가 필요합니다.\"}");
            return;
        }
        
        try {
            long newsId = Long.parseLong(newsIdStr);
            
            // 원본 게시글 가져오기
            NewsDTO original = newsService.getNewsById(newsId);
            
            if (original == null || "deleted".equals(original.getNewsDeleted())) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"존재하지 않는 게시글입니다.\"}");
                return;
            }
            
            // 공지사항 상태 토글
            boolean isCurrentlyNotice = "notification".equals(original.getNewsNotify());
            boolean success = newsService.setNoticeById(newsId, !isCurrentlyNotice, user.getUserLevel() >= 3 ? "admin" : "user");
            
            if (success) {
                String newStatus = isCurrentlyNotice ? "common" : "notification";
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"공지사항 상태가 변경되었습니다.\", \"status\": \"" + newStatus + "\"}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"공지사항 상태 변경에 실패했습니다.\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 게시글 ID입니다.\"}");
        }
    }
    
    /**
     * 사용자가 이미 게시글을 추천했는지 확인
     */
    private boolean isAlreadyRecommended(long newsId, long userId) throws SQLException {
        try {
            // NewsService의 hasUserRecommended 메서드를 사용하여 추천 여부를 확인합니다.
            return newsService.hasUserRecommended(newsId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
