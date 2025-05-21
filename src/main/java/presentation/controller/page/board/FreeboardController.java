package presentation.controller.page.board;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import business.service.freeboard.FreeboardService;
import dto.board.AttachmentDTO;
import dto.board.FreeboardCommentDTO;
import dto.board.FreeboardDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import presentation.controller.page.Controller;
import util.FileUtil;
import util.config.AppConfig;
import util.web.IpUtil;

/**
 * 자유게시판 관련 요청을 처리하는 컨트롤러
 * URL 패턴: /freeboard.do 형식 지원
 */
@WebServlet({"/freeboard/*", "/freeboard.do"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,     // 10 MB
    maxRequestSize = 1024 * 1024 * 50   // 50 MB
)
public class FreeboardController extends HttpServlet implements Controller {
    private static final Logger logger = Logger.getLogger(FreeboardController.class.getName());
    private FreeboardService freeboardService;
    private util.web.RequestRouter router;
    private static final long serialVersionUID = 1L;
    
    // LocalDateTime 직렬화/역직렬화를 위한 Gson 설정
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    // LocalDateTime 어댑터
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.freeboardService = new FreeboardService();
        
        // 라우터 설정
        initRequestRouter();
    }    /**
     * 요청 라우터 초기화
     */    private void initRequestRouter() {
        router = new util.web.RequestRouter();
          // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "자유게시판 API");
            return result;
        });
        
        // 게시글 작성 API 추가
        router.postJson("/create", (req, res) -> {
            try {
                // 로그인 확인
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");
                
                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }
                
                // JSON 요청 바디 읽기
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "요청 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
                    return errorResult;
                }
                
                // JSON 파싱
                com.google.gson.JsonObject jsonRequest = new Gson().fromJson(sb.toString(), com.google.gson.JsonObject.class);
                String title = jsonRequest.has("freeboardTitle") ? jsonRequest.get("freeboardTitle").getAsString() : "";
                String content = jsonRequest.has("freeboardContents") ? jsonRequest.get("freeboardContents").getAsString() : "";
                String clientIp = IpUtil.getClientIpAddr(req);
                
                if (title == null || title.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "제목을 입력해주세요.");
                    return errorResult;
                }
                
                if (content == null || content.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "내용을 입력해주세요.");
                    return errorResult;
                }
                
                // 게시글 생성
                FreeboardDTO freeboard = new FreeboardDTO(title, content, clientIp, user.getUserUid());
                boolean result = freeboardService.createFreeboard(freeboard);
                
                if (result) {
                    Map<String, Object> successResult = new HashMap<>();
                    successResult.put("status", "success");
                    successResult.put("success", true);
                    successResult.put("message", "게시글이 성공적으로 등록되었습니다.");
                    successResult.put("postId", freeboard.getFreeboardUid());
                    return successResult;
                } else {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 등록에 실패했습니다.");
                    return errorResult;
                }
            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("success", false);
                errorResult.put("message", "오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });
        
        // 게시글 삭제 API 추가
        router.postJson("/delete", (req, res) -> {
            try {
                // 로그인 확인
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");
                
                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }
                
                // 파라미터 가져오기 (id 파라미터 지원)
                String idParam = req.getParameter("id");
                System.out.println("삭제 요청 ID: " + idParam);
                
                if (idParam == null || idParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 ID가 제공되지 않았습니다.");
                    return errorResult;
                }
                
                try {
                    long freeboardId = Long.parseLong(idParam.trim());
                    
                    // 원본 게시글 가져오기
                    FreeboardDTO original = freeboardService.getFreeboardById(freeboardId);
                    
                    if (original == null) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("status", "error");
                        errorResult.put("success", false);
                        errorResult.put("message", "존재하지 않는 게시글입니다.");
                        return errorResult;
                    }
                      // 작성자 또는 관리자만 삭제 가능
                    if (original.getUserUid() != user.getUserUid() && user.getUserLevel() < 3) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("status", "error");
                        errorResult.put("success", false);
                        errorResult.put("message", "삭제 권한이 없습니다.");
                        return errorResult;
                    }                    // 게시글 삭제
                    boolean success = freeboardService.deleteFreeboard(freeboardId, user.getUserUid(), user.getUserAuthority());
                    
                    Map<String, Object> result = new HashMap<>();
                    if (success) {
                        result.put("status", "success");
                        result.put("success", true);
                        result.put("message", "게시글이 삭제되었습니다.");
                    } else {
                        result.put("status", "error");
                        result.put("success", false);
                        result.put("message", "게시글 삭제에 실패했습니다.");
                    }
                    return result;
                    
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "잘못된 게시글 ID입니다.");
                    return errorResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
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
                // 잘못된 파라미터가 넘어온 경우 기본값 사용
            }
            
            List<FreeboardDTO> freeboardList = freeboardService.getAllFreeboards(page, pageSize);
            int totalCount = freeboardService.getTotalCount();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            
            Map<String, Object> result = new HashMap<>();
            result.put("freeboardList", freeboardList);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("pageSize", pageSize);
            result.put("totalCount", totalCount);
            
            return result;
        });
          router.getJson("/view", (req, res) -> {
            try {
                long postId = Long.parseLong(req.getParameter("id"));
                FreeboardDTO freeboard = freeboardService.getFreeboardById(postId);
                
                if (freeboard == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "게시글을 찾을 수 없습니다.");
                    return errorResult;
                }
                
                return freeboard;
            } catch (NumberFormatException e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "잘못된 게시글 ID입니다.");
                return errorResult;
            }
        });
          router.getJson("/comments", (req, res) -> {
            try {
                long postId = Long.parseLong(req.getParameter("postId"));
                List<FreeboardCommentDTO> comments = freeboardService.getCommentsByPostId(postId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("comments", comments);
                return result;
            } catch (NumberFormatException e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 게시글 ID입니다.");
                return errorResult;
            }
        });
          // POST 요청 JSON 라우터 설정
        router.postJson("/write", (req, res) -> {
            // 로그인 확인
            HttpSession session = req.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "로그인이 필요합니다.");
                return errorResult;
            }
            
            String title = req.getParameter("title");
            String content = req.getParameter("content");
            String clientIp = IpUtil.getClientIpAddr(req);
            
            FreeboardDTO freeboard = new FreeboardDTO(title, content, clientIp, user.getUserUid());
            
            boolean result = freeboardService.createFreeboard(freeboard);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            
            if (result) {
                response.put("message", "게시글이 등록되었습니다.");
                response.put("postId", freeboard.getFreeboardUid());
            } else {
                response.put("message", "게시글 등록에 실패했습니다.");
            }
            
            return response;
        });
          router.postJson("/addComment", (req, res) -> {
            // 로그인 체크
            HttpSession session = req.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "로그인이 필요합니다.");
                return errorResult;
            }
            
            // 파라미터 받기
            String postIdStr = req.getParameter("postId");
            String content = req.getParameter("content");
            
            // 필수 값 체크
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "댓글 내용을 입력해주세요.");
                return errorResult;
            }
            
            try {
                long postId = Long.parseLong(postIdStr);
                String clientIp = IpUtil.getClientIpAddr(req);
                  // 댓글 객체 생성
                FreeboardCommentDTO comment = new FreeboardCommentDTO(
                        postId, 
                        user.getUserUid(), 
                        content, 
                        clientIp);
                
                // 댓글 등록
                boolean success = freeboardService.addComment(comment);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", success);
                
                if (success) {
                    result.put("message", "댓글이 등록되었습니다.");
                    // 최신 댓글 목록 조회해서 함께 보내기
                    result.put("comments", freeboardService.getCommentsByPostId(postId));
                } else {
                    result.put("message", "댓글 등록에 실패했습니다.");
                }
                
                return result;
            } catch (NumberFormatException e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 게시글 ID입니다.");
                return errorResult;
            }
        });
    }    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // API 요청인지 먼저 확인 (router에 의해 처리될 수 있는지)
        String pathInfo = request.getPathInfo();
        
        // pathInfo가 있으면 API 요청으로 간주하고 Router를 통해 처리 시도
        if (pathInfo != null) {
            boolean handled = router.handleGetJson(request, response);
            if (handled) {
                // Router가 요청을 처리했으므로 메서드 종료
                return;
            }
        }
        
        // API 요청이 아니거나 Router가 처리하지 못한 경우, 기존 로직으로 처리
        String action = request.getParameter("action");
        
        if (action == null || action.equals("list")) {
            // 게시글 목록 조회
            getAllFreeboards(request, response);
        } else if (action.equals("view")) {
            // 게시글 상세 조회
            getFreeboardById(request, response);
        } else if (action.equals("write")) {
            // 글쓰기 폼으로 이동
            request.getRequestDispatcher("/WEB-INF/views/board/freeboard-write.jsp").forward(request, response);
        } else if (action.equals("edit")) {
            // 수정 폼으로 이동
            showEditForm(request, response);
        } else if (action.equals("downloadAttachment")) {
            // 첨부파일 다운로드
            downloadAttachment(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // API 요청인지 먼저 확인 (router에 의해 처리될 수 있는지)
        String pathInfo = request.getPathInfo();
        
        // pathInfo가 있으면 API 요청으로 간주하고 Router를 통해 처리 시도
        if (pathInfo != null) {
            boolean handled = router.handlePostJson(request, response);
            if (handled) {
                // Router가 요청을 처리했으므로 메서드 종료
                return;
            }
        }
        
        // API 요청이 아니거나 Router가 처리하지 못한 경우, 기존 로직으로 처리
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (action.equals("write")) {
            // 게시글 등록
            postFreeboard(request, response);
        } else if (action.equals("edit")) {
            // 게시글 수정
            updateFreeboardById(request, response);
        } else if (action.equals("delete")) {
            // 게시글 삭제
            deleteFreeboardById(request, response);
        } else if (action.equals("notice")) {
            // 공지사항 설정/해제
            setNoticeById(request, response);
        } else if (action.equals("hide")) {
            // 게시글 숨김 처리
            hideFreeboardById(request, response);
        } else if (action.equals("report")) {
            // 게시글 신고
            reportFreeboardById(request, response);
        } else if (action.equals("reportUser")) {
            // 이용자 신고
            reportUserById(request, response);
        } else if (action.equals("deleteAttach")) {
            // 첨부파일 삭제
            deleteFreeboardAttachByFilename(request, response);
        } else if (action.equals("uploadAttachment")) {
            // 첨부파일 업로드
            uploadAttachment(request, response);
        } else if (action.equals("addComment")) {
            // 댓글 추가
            addComment(request, response);
        } else if (action.equals("updateComment")) {
            // 댓글 수정
            updateComment(request, response);
        } else if (action.equals("deleteComment")) {
            // 댓글 삭제
            deleteComment(request, response);
        } else if (action.equals("getComments")) {
            // 댓글 목록 조회
            getCommentsByPostId(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 게시글 목록 조회
     */
    private void getAllFreeboards(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 페이징 처리
        int page = 1;
        int pageSize = 10;
        
        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
            
            if (request.getParameter("pageSize") != null) {
                pageSize = Integer.parseInt(request.getParameter("pageSize"));
            }
        } catch (NumberFormatException e) {
            // 잘못된 파라미터가 넘어온 경우 기본값 사용
        }
        
        List<FreeboardDTO> freeboardList = freeboardService.getAllFreeboards(page, pageSize);
        int totalCount = freeboardService.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        // JSP 페이지로 포워딩 대신 JSON 응답 반환
        Map<String, Object> result = new HashMap<>();
        result.put("freeboardList", freeboardList);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("pageSize", pageSize);
        result.put("totalCount", totalCount);
        
        sendJsonResponse(response, result);
    }
    
    /**
     * 게시글 상세 조회
     */
    private void getFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            FreeboardDTO freeboard = freeboardService.getFreeboardById(postId);
            
            if (freeboard == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "게시글을 찾을 수 없습니다.");
                return;
            }
            
            // JSP 페이지로 포워딩 대신 JSON 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("freeboard", freeboard);
            
            // 댓글 목록도 함께 조회
            List<FreeboardCommentDTO> comments = freeboardService.getCommentsByPostId(postId);
            result.put("comments", comments);
            
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
        }
    }    /**
     * 게시글 등록
     */    private void postFreeboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            // 로그인이 필요하다는 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("redirect", request.getContextPath() + "/login?redirect=freeboard&action=write");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            // 요청 정보 디버깅 출력
            System.out.println("==== 게시글 등록 요청 시작 ====");
            System.out.println("ContentType: " + request.getContentType());
            System.out.println("CharacterEncoding: " + request.getCharacterEncoding());
            System.out.println("요청 URI: " + request.getRequestURI());
            System.out.println("요청 URL: " + request.getRequestURL());
            System.out.println("쿼리 스트링: " + request.getQueryString());
            
            // 모든 헤더 목록 출력
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            System.out.println("===== 요청 헤더 목록 =====");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println(headerName + ": " + request.getHeader(headerName));
            }
            
            // 모든 파라미터 이름 출력
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            System.out.println("===== 요청 파라미터 목록 =====");
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                if (paramValues.length > 1) {
                    System.out.println(paramName + ": [다중 값]");
                    for (int i = 0; i < paramValues.length; i++) {
                        System.out.println("  - 값 " + i + ": " + paramValues[i]);
                    }
                } else {
                    System.out.println(paramName + ": " + request.getParameter(paramName));
                }
            }
            
            // 폼 데이터에서 파라미터 읽기
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String clientIp = IpUtil.getClientIpAddr(request);
            
            System.out.println("폼 데이터: title=[" + title + "], content 길이=[" + (content != null ? content.length() : "null") + "]");
            
            // Part 목록 출력 (멀티파트 요청인 경우)
            if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
                System.out.println("멀티파트 요청 감지됨, Part 목록:");
                for (Part part : request.getParts()) {
                    System.out.println(" - Part 이름: " + part.getName() + ", 크기: " + part.getSize());
                }
            }
            
            // 유효성 검사
            if (title == null || title.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "제목을 입력해주세요.");
                sendJsonResponse(response, errorResult);
                System.out.println("유효성 검사 실패: 제목 없음");
                return;
            }
            
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "내용을 입력해주세요.");
                sendJsonResponse(response, errorResult);
                System.out.println("유효성 검사 실패: 내용 없음");
                return;
            }
            
            FreeboardDTO freeboard = new FreeboardDTO(title, content, clientIp, user.getUserUid());
            
            boolean result = freeboardService.createFreeboard(freeboard);
            System.out.println("게시글 등록 결과: " + (result ? "성공" : "실패") + ", ID: " + freeboard.getFreeboardUid());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result);
            
            if (result) {
                responseData.put("message", "게시글이 성공적으로 등록되었습니다.");
                responseData.put("postId", freeboard.getFreeboardUid());
                responseData.put("redirect", request.getContextPath() + "/freeboard?action=view&id=" + freeboard.getFreeboardUid());
            } else {
                responseData.put("message", "게시글 등록에 실패했습니다.");
                responseData.put("error", "게시글 등록에 실패했습니다.");
                responseData.put("freeboard", freeboard);
            }
            
            System.out.println("응답 전송: " + responseData);
            sendJsonResponse(response, responseData);
            System.out.println("==== 게시글 등록 요청 완료 ====");
        } catch (Exception e) {
            System.err.println("게시글 등록 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "게시글 등록 중 오류가 발생했습니다: " + e.getMessage());
            sendJsonResponse(response, errorResult);
        }
    }
    
    /**
     * 수정 폼 표시
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 로그인 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                // 로그인되지 않은 경우 권한 없음 응답
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                result.put("redirect", request.getContextPath() + "/login?redirect=freeboard&action=edit");
                sendJsonResponse(response, result);
                return;
            }
            
            long postId = Long.parseLong(request.getParameter("id"));
            FreeboardDTO freeboard = freeboardService.getFreeboardById(postId);
            
            if (freeboard == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "게시글을 찾을 수 없습니다.");
                sendJsonResponse(response, result);
                return;
            }
            
            // 작성자 본인 또는 관리자만 수정 가능
            if (freeboard.getUserUid() != user.getUserUid() && 
                    !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "수정 권한이 없습니다.");
                sendJsonResponse(response, result);
                return;
            }
            
            // 수정 폼 정보를 JSON으로 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("freeboard", freeboard);
            
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "잘못된 게시글 ID입니다.");
            sendJsonResponse(response, result);
        }
    }
      /**
     * 게시글 수정
     */
    private void updateFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            // 로그인이 필요하다는 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("redirect", request.getContextPath() + "/login?redirect=freeboard&action=edit");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            
            FreeboardDTO freeboard = new FreeboardDTO();
            freeboard.setFreeboardUid(postId);
            freeboard.setFreeboardTitle(title);
            freeboard.setFreeboardContents(content);
            
            boolean result = freeboardService.updateFreeboard(freeboard, user.getUserUid(), user.getUserAuthority());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result);
            
            if (result) {
                responseData.put("message", "게시글이 성공적으로 수정되었습니다.");
                responseData.put("postId", postId);
                responseData.put("redirect", request.getContextPath() + "/freeboard?action=view&id=" + postId);
            } else {
                responseData.put("message", "게시글 수정에 실패했습니다.");
                responseData.put("freeboard", freeboard);
            }
            
            sendJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "잘못된 게시글 ID입니다.");
            sendJsonResponse(response, errorResult);
        }
    }
    
    /**
     * 게시글 삭제
     */
    private void deleteFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            // 로그인이 필요하다는 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            
            boolean result = freeboardService.deleteFreeboard(postId, user.getUserUid(), user.getUserAuthority());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result);
            
            if (result) {
                responseData.put("message", "게시글이 성공적으로 삭제되었습니다.");
                responseData.put("redirect", request.getContextPath() + "/freeboard?action=list");
            } else {
                responseData.put("message", "게시글 삭제에 실패했습니다. 권한을 확인해주세요.");
            }
            
            sendJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "잘못된 게시글 ID입니다.");
            sendJsonResponse(response, errorResult);
        }
    }
    
    /**
     * 공지사항 설정/해제 (관리자 전용)
     */
    private void setNoticeById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
            // 권한 없음 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "관리자 권한이 필요합니다.");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            boolean isNotice = Boolean.parseBoolean(request.getParameter("isNotice"));
            
            boolean result = freeboardService.setNotice(postId, isNotice, user.getUserAuthority());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result);
            
            if (result) {
                responseData.put("message", isNotice ? "공지사항으로 설정되었습니다." : "공지사항에서 해제되었습니다.");
                responseData.put("redirect", request.getContextPath() + "/freeboard?action=view&id=" + postId);
                responseData.put("isNotice", isNotice);
            } else {
                responseData.put("message", "공지사항 설정/해제에 실패했습니다.");
            }
            
            sendJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "잘못된 게시글 ID입니다.");
            sendJsonResponse(response, errorResult);
        }
    }
    
    /**
     * 게시글 숨김 처리 (관리자 전용)
     */
    private void hideFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
            // 권한 없음 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "관리자 권한이 필요합니다.");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            String hideReason = request.getParameter("reason");
            
            boolean result = freeboardService.hideFreeboard(postId, hideReason, user.getUserAuthority());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result);
            
            if (result) {
                responseData.put("message", "게시글이 숨김 처리되었습니다.");
                responseData.put("redirect", request.getContextPath() + "/freeboard?action=list");
            } else {
                responseData.put("message", "게시글 숨김 처리에 실패했습니다.");
            }
            
            sendJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "잘못된 게시글 ID입니다.");
            sendJsonResponse(response, errorResult);
        }
    }
    
    /**
     * 불량 게시글 신고
     */
    private void reportFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            // 로그인이 필요하다는 JSON 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            String reportReason = request.getParameter("reason");
            String reportCategory = validateReportCategory(request.getParameter("category"));
            
            // 필수 파라미터 검증
            if (reportReason == null || reportReason.trim().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "신고 사유를 입력해주세요.");
                sendJsonResponse(response, result);
                return;
            }
            
            boolean result = freeboardService.reportFreeboard(postId, user.getUserUid(), reportReason, reportCategory);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result);
            
            if (result) {
                responseData.put("message", "신고가 접수되었습니다.");
                responseData.put("postId", postId);
                // 리다이렉트 정보 추가
                responseData.put("redirect", request.getContextPath() + "/freeboard?action=view&id=" + postId + "&reported=true");
            } else {
                responseData.put("message", "신고 처리 중 오류가 발생했습니다.");
            }
            
            sendJsonResponse(response, responseData);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "잘못된 게시글 ID입니다.");
            sendJsonResponse(response, errorResult);
        }
    }
    
    /**
     * 사용자 신고 처리
     */
    public void reportUserById(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 로그인 확인
        UserDTO reporter = (UserDTO) request.getSession().getAttribute("user");
        if (reporter == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=need_login");
            return;
        }
        
        try {
            // 파라미터 받기
            long targetUserId = Long.parseLong(request.getParameter("userId"));
            String reportReason = request.getParameter("reason");
            String reportCategory = request.getParameter("category");
            
            // 카테고리 검증 로직 추가
            reportCategory = validateReportCategory(reportCategory);
            
            // 로그인한 사용자가 자신을 신고하는 경우 방지
            if (targetUserId == reporter.getUserUid()) { // getUserId() → getUserUid()
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"자신을 신고할 수 없습니다.\"}");
                return;
            }
            
            // 사용자 신고 처리
            boolean success = freeboardService.reportUser(targetUserId, reporter.getUserUid(), 
                                                         reportReason, reportCategory);
            
            // 결과 반환
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"신고가 접수되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"신고 처리 중 오류가 발생했습니다.\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 요청입니다.\"}");
        }
    }
    
    /**
     * 첨부파일 삭제 (관리자/매니저용)
     */
    public void deleteFreeboardAttachByFilename(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 로그인 및 권한 확인
        UserDTO admin = (UserDTO) request.getSession().getAttribute("user");
        if (admin == null || !("admin".equals(admin.getUserAuthority()) || "armband".equals(admin.getUserAuthority()))) {
            response.sendRedirect(request.getContextPath() + "/login?error=need_admin");
            return;
        }
        
        try {
            // 파라미터 받기
            long postId = Long.parseLong(request.getParameter("postId"));
            String filename = request.getParameter("filename");
            String reason = request.getParameter("reason");
            
            // 파일명이 없거나 비어있는 경우 체크
            if (filename == null || filename.trim().isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"파일명이 필요합니다.\"}");
                return;
            }
            
            // 삭제 이유가 없거나 비어있는 경우 기본값 설정
            if (reason == null || reason.trim().isEmpty()) {
                reason = "관리자에 의한 삭제";
            }
            
            // 첨부파일 삭제 처리 (수정된 서비스 메서드 호출)
            boolean success = freeboardService.deleteAttachByFilename(postId, filename, reason, admin.getUserUid());
            
            // 결과 반환
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write("{\"success\": true, \"message\": \"첨부파일이 삭제되었습니다.\"}");
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"첨부파일 삭제 중 오류가 발생했습니다.\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 요청입니다.\"}");
        }
    }
      /**
     * 첨부파일 업로드 처리
     */
    public void uploadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 사용자 로그인 확인
        UserDTO user = (UserDTO) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }
        
        try {
            // 파일 업로드를 위한 멀티파트 요청 처리
            Part filePart = request.getPart("file");
            String postIdStr = request.getParameter("postId");
            long postId = Long.parseLong(postIdStr);
            
            // 파일이 비어있는지 확인
            if (filePart == null || filePart.getSize() == 0) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"파일이 선택되지 않았습니다.\"}");
                return;
            }
            
            // 파일 이름 및 크기 추출
            String fileName = filePart.getSubmittedFileName();
            long fileSize = filePart.getSize();
            
            // 파일 저장 로직
            // 1. 업로드 디렉토리 경로 얻기 (외부 설정 사용)
            String uploadDirPath = FileUtil.getUploadDirectoryPath();
            
            // 2. 안전한 고유 파일명 생성
            String uniqueFileName = FileUtil.generateUniqueFilename(fileName);
            
            // 3. 파일 저장 경로 생성
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            File destinationFile = new File(uploadDir, uniqueFileName);
            
            // 4. 파일 저장
            filePart.write(destinationFile.getAbsolutePath());
              // 5. 첨부파일 정보 DB에 저장
            AttachmentDTO attachment = new AttachmentDTO();
            attachment.setPostId(postId);
            attachment.setFileName(fileName);
            attachment.setFilePath(uniqueFileName);
            attachment.setFileSize(fileSize);
            
            boolean success = freeboardService.addAttachment(attachment);
            
            // 6. 응답 처리
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "파일이 업로드되었습니다.");
                result.put("filename", uniqueFileName);
                result.put("originalName", fileName);
                result.put("fileSize", fileSize);
            } else {
                result.put("message", "파일 업로드 중 오류가 발생했습니다.");
            }
            
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(result));
            out.flush();
            
        } catch (NumberFormatException e) {
            logger.warning("Invalid post ID format: " + request.getParameter("postId"));
            sendJsonResponse(response, false, "잘못된 게시글 ID입니다.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "첨부파일 업로드 실패", e);
            sendJsonResponse(response, false, "첨부파일 업로드 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 첨부파일 다운로드 처리
     */
    private void downloadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String fileName = request.getParameter("filename");
        if (fileName == null || fileName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "파일명이 없습니다.");
            return;
        }
        
        try {
            // 첨부파일 정보 조회
            AttachmentDTO attachment = freeboardService.getAttachmentByFilename(fileName);
            
            if (attachment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
                return;
            }
            
            // 파일 경로 구성
            String uploadDirPath = FileUtil.getUploadDirectoryPath();
            File file = new File(uploadDirPath, fileName);
            
            if (!file.exists() || !file.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "서버에 파일이 존재하지 않습니다.");
                return;
            }
            
            // 파일 다운로드를 위한 HTTP 헤더 설정
            String mimeType = request.getServletContext().getMimeType(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            response.setContentType(mimeType);
            response.setContentLength((int) file.length());
              // 다운로드 파일명 설정 (한글 파일명 인코딩 처리)
            String originFilename = attachment.getFileName();
            String userAgent = request.getHeader("User-Agent");
            
            if (userAgent.contains("MSIE") || userAgent.contains("Trident") || userAgent.contains("Edge")) {
                // IE, Edge 브라우저
                originFilename = URLEncoder.encode(originFilename, "UTF-8").replaceAll("\\+", "%20");
            } else {
                // 기타 브라우저
                originFilename = new String(originFilename.getBytes("UTF-8"), "ISO-8859-1");
            }
            
            response.setHeader("Content-Disposition", "attachment; filename=\"" + originFilename + "\"");
            
            // 파일 전송
            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                  out.flush();
            }
            
            // 다운로드 카운트 증가 (비동기적으로 처리)
            freeboardService.increaseDownloadCount(attachment.getAttachId());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "파일 다운로드 처리 중 오류", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "파일 다운로드 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 댓글 기능 관련 메서드 - 댓글 수정
     */
    private void updateComment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 로그인 체크
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        // 파라미터 받기
        String commentIdStr = request.getParameter("commentId");
        String content = request.getParameter("content");
        
        // 필수 값 체크
        if (content == null || content.trim().isEmpty()) {
            sendJsonResponse(response, false, "댓글 내용을 입력해주세요.");
            return;
        }
        
        try {
            long commentId = Long.parseLong(commentIdStr);
            
            // 기존 댓글 조회
            FreeboardCommentDTO existingComment = freeboardService.getCommentById(commentId);
            
            if (existingComment == null) {
                sendJsonResponse(response, false, "댓글을 찾을 수 없습니다.");
                return;
            }
            
            // 수정할 내용 설정
            existingComment.setFreeboardCommentContents(content);
            
            // 댓글 수정
            boolean success = freeboardService.updateComment(existingComment, user.getUserUid(), user.getUserAuthority());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "댓글이 수정되었습니다.");
                // 최신 댓글 목록 조회해서 함께 보내기
                result.put("comments", freeboardService.getCommentsByPostId(existingComment.getFreeboardUid()));
            } else {
                result.put("message", "댓글 수정에 실패했습니다. 본인이 작성한 댓글만 수정할 수 있습니다.");
            }
            
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(result));
            out.flush();
            
        } catch (NumberFormatException e) {
            logger.warning("Invalid comment ID format: " + commentIdStr);
            sendJsonResponse(response, false, "잘못된 댓글 ID입니다.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "댓글 수정 실패", e);
            sendJsonResponse(response, false, "댓글 수정 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 댓글 기능 관련 메서드 - 댓글 삭제
     */
    private void deleteComment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 로그인 체크
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        // 파라미터 받기
        String commentIdStr = request.getParameter("commentId");
        
        try {
            long commentId = Long.parseLong(commentIdStr);
            
            // 기존 댓글 조회 (삭제 후 게시글 ID가 필요하기 때문에 미리 조회)
            FreeboardCommentDTO existingComment = freeboardService.getCommentById(commentId);
            
            if (existingComment == null) {
                sendJsonResponse(response, false, "댓글을 찾을 수 없습니다.");
                return;
            }
            
            // 댓글 삭제
            boolean success = freeboardService.deleteComment(commentId, user.getUserUid(), user.getUserAuthority());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "댓글이 삭제되었습니다.");
                // 최신 댓글 목록 조회해서 함께 보내기
                result.put("comments", freeboardService.getCommentsByPostId(existingComment.getFreeboardUid()));
            } else {
                result.put("message", "댓글 삭제에 실패했습니다. 본인이 작성한 댓글만 삭제할 수 있습니다.");
            }
            
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(result));
            out.flush();
            
        } catch (NumberFormatException e) {
            logger.warning("Invalid comment ID format: " + commentIdStr);
            sendJsonResponse(response, false, "잘못된 댓글 ID입니다.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "댓글 삭제 실패", e);
            sendJsonResponse(response, false, "댓글 삭제 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 댓글 추가 처리
     */
    private void addComment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 로그인 체크
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        // 파라미터 받기
        String postIdStr = request.getParameter("postId");
        String content = request.getParameter("content");
        
        // 필수 값 체크
        if (content == null || content.trim().isEmpty()) {
            sendJsonResponse(response, false, "댓글 내용을 입력해주세요.");
            return;
        }
        
        try {
            long postId = Long.parseLong(postIdStr);
            String clientIp = IpUtil.getClientIpAddr(request);
            
            // 댓글 객체 생성
            FreeboardCommentDTO comment = new FreeboardCommentDTO(
                    postId, 
                    user.getUserUid(), 
                    content, 
                    clientIp);
            
            // 댓글 등록
            boolean success = freeboardService.addComment(comment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "댓글이 등록되었습니다.");
                // 최신 댓글 목록 조회해서 함께 보내기
                result.put("comments", freeboardService.getCommentsByPostId(postId));
            } else {
                result.put("message", "댓글 등록에 실패했습니다.");
            }
            
            // JSON 형식으로 응답
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(result));
            out.flush();
            
        } catch (NumberFormatException e) {
            logger.warning("Invalid post ID format: " + postIdStr);
            sendJsonResponse(response, false, "잘못된 게시글 ID입니다.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "댓글 등록 실패", e);
            sendJsonResponse(response, false, "댓글 등록 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 댓글 목록 조회 및 JSON 형태로 반환
     */
    private void getCommentsByPostId(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String postIdStr = request.getParameter("postId");
        
        try {
            long postId = Long.parseLong(postIdStr);
            List<FreeboardCommentDTO> comments = freeboardService.getCommentsByPostId(postId);
            
            // 댓글 목록 조회 결과를 JSON으로 응답
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("comments", comments);
            
            // JSON 형식으로 응답
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(result));
            out.flush();
            
        } catch (NumberFormatException e) {
            logger.warning("Invalid post ID format: " + postIdStr);
            sendJsonResponse(response, false, "잘못된 게시글 ID입니다.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "댓글 목록 조회 실패", e);
            sendJsonResponse(response, false, "댓글 목록 조회 중 오류가 발생했습니다.");
        }
    }
      /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        // 커스텀 어댑터를 사용하여 LocalDateTime 직렬화 처리
        out.print(gson.toJson(data));
        out.flush();
    }
    
    /**
     * 메시지 상태와 함께 JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        sendJsonResponse(response, result);
    }
    
    /**
     * 신고 카테고리 검증
     * @param category 요청으로 전달된 카테고리
     * @return 유효한 카테고리 값, 유효하지 않을 경우 기본값 'spam_ad' 반환
     */
    private String validateReportCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "spam_ad"; // 기본값
        }
        
        // 유효한 카테고리 목록
        String[] validCategories = {
            "spam_ad", 
            "profanity_hate_speech", 
            "adult_content", 
            "impersonation_fraud", 
            "copyright_infringement"
        };
        
        for (String validCategory : validCategories) {
            if (validCategory.equals(category)) {
                return category;
            }
        }
        
        return "spam_ad"; // 유효하지 않은 값일 경우 기본값
    }
}
