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
            result.put("message", "자유게시판 API");
            return result;
        });
        
        // 게시글 작성 API 추가 (JSON 요청 본문 사용)
        router.postJson("/create", (req, res) -> {
            try {
                // 로그인 확인
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");
                
                if (user == null) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Map<String, Object> errorResult = new HashMap<>();
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
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "요청 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
                    return errorResult;
                }
                
                // JSON 파싱
                com.google.gson.JsonObject jsonRequest = gson.fromJson(sb.toString(), com.google.gson.JsonObject.class);
                String title = jsonRequest.has("title") ? jsonRequest.get("title").getAsString() : null; 
                String content = jsonRequest.has("content") ? jsonRequest.get("content").getAsString() : null; 
                
                if (title == null || title.trim().isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "제목을 입력해주세요.");
                    return errorResult;
                }
                
                if (content == null || content.trim().isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "내용을 입력해주세요.");
                    return errorResult;
                }
                
                String clientIp = IpUtil.getClientIpAddr(req);
                FreeboardDTO freeboard = new FreeboardDTO(title, content, clientIp, user.getUserUid());
                boolean result = freeboardService.createFreeboard(freeboard);
                
                if (result) {
                    Map<String, Object> successResult = new HashMap<>();
                    successResult.put("success", true);
                    successResult.put("message", "게시글이 성공적으로 등록되었습니다.");
                    successResult.put("postId", freeboard.getFreeboardUid());
                    return successResult;
                } else {
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 등록에 실패했습니다.");
                    return errorResult;
                }
            } catch (com.google.gson.JsonSyntaxException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 JSON 형식입니다: " + e.getMessage());
                return errorResult;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "게시글 작성 중 오류", e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
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
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }
                
                // JSON 요청 본문에서 ID 가져오기
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                com.google.gson.JsonObject jsonRequest = gson.fromJson(sb.toString(), com.google.gson.JsonObject.class);
                
                if (!jsonRequest.has("id")) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 ID가 제공되지 않았습니다.");
                    return errorResult;
                }
                long freeboardId = jsonRequest.get("id").getAsLong();
                
                FreeboardDTO original = freeboardService.getFreeboardById(freeboardId);
                
                if (original == null) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "존재하지 않는 게시글입니다.");
                    return errorResult;
                }
                // 작성자 또는 관리자만 삭제 가능
                if (original.getUserUid() != user.getUserUid() && user.getUserLevel() < 3) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "삭제 권한이 없습니다.");
                    return errorResult;
                }
                boolean success = freeboardService.deleteFreeboard(freeboardId, user.getUserUid(), user.getUserAuthority());
                
                Map<String, Object> result = new HashMap<>();
                if (success) {
                    result.put("success", true);
                    result.put("message", "게시글이 삭제되었습니다.");
                } else {
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    result.put("success", false);
                    result.put("message", "게시글 삭제에 실패했습니다.");
                }
                return result;
                
            } catch (com.google.gson.JsonSyntaxException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 JSON 형식입니다: " + e.getMessage());
                return errorResult;
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 게시글 ID 형식입니다.");
                return errorResult;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "게시글 삭제 중 오류", e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResult = new HashMap<>();
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
                String idParam = req.getParameter("id");
                if (idParam == null || idParam.trim().isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 ID가 제공되지 않았습니다.");
                    return errorResult;
                }
                long postId = Long.parseLong(idParam.trim());
                FreeboardDTO freeboard = freeboardService.getFreeboardById(postId);
                
                if (freeboard == null) {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글을 찾을 수 없습니다.");
                    return errorResult;
                }

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("freeboard", freeboard);
                responseMap.put("comments", freeboardService.getCommentsByPostId(postId));
                return responseMap;
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 게시글 ID 형식입니다.");
                return errorResult;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "게시글 조회 중 오류", e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });

        router.getJson("/comments", (req, res) -> {
            try {
                String postIdParam = req.getParameter("postId");
                if (postIdParam == null || postIdParam.trim().isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 ID(postId)가 제공되지 않았습니다.");
                    return errorResult;
                }
                long postId = Long.parseLong(postIdParam.trim());
                List<FreeboardCommentDTO> comments = freeboardService.getCommentsByPostId(postId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("comments", comments);
                return result;
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 게시글 ID 형식입니다.");
                return errorResult;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "댓글 목록 조회 중 오류", e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });

        router.postJson("/addComment", (req, res) -> {
            // 로그인 체크
            HttpSession session = req.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");

            if (user == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "로그인이 필요합니다.");
                return errorResult;
            }

            // JSON 요청 본문 읽기
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "요청 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }

            try {
                // JSON 파싱
                com.google.gson.JsonObject jsonRequest = gson.fromJson(sb.toString(), com.google.gson.JsonObject.class);
                
                if (!jsonRequest.has("freeboardUid") || !jsonRequest.has("freeboardCommentContents")) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "필수 파라미터(freeboardUid, freeboardCommentContents)가 누락되었습니다.");
                    return errorResult;
                }

                long postId = jsonRequest.get("freeboardUid").getAsLong();
                String commentContent = jsonRequest.get("freeboardCommentContents").getAsString();
                Long parentId = null;
                if (jsonRequest.has("parentId") && !jsonRequest.get("parentId").isJsonNull()) {
                    parentId = jsonRequest.get("parentId").getAsLong();
                }

                if (commentContent == null || commentContent.trim().isEmpty()) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "댓글 내용을 입력해주세요.");
                    return errorResult;
                }
                
                String clientIp = IpUtil.getClientIpAddr(req);
                FreeboardCommentDTO comment = new FreeboardCommentDTO(
                        postId,
                        user.getUserUid(),
                        commentContent,
                        clientIp);

                boolean success = freeboardService.addComment(comment);

                Map<String, Object> result = new HashMap<>();
                result.put("success", success);

                if (success) {
                    result.put("message", "댓글이 등록되었습니다.");
                    result.put("comments", freeboardService.getCommentsByPostId(postId));
                } else {
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    result.put("message", "댓글 등록에 실패했습니다.");
                }
                return result;
            } catch (com.google.gson.JsonSyntaxException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 JSON 형식입니다: " + e.getMessage());
                return errorResult;
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 ID 형식입니다 (게시글 또는 부모 댓글).");
                return errorResult;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "댓글 작성 중 오류", e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });

        // 추천 API 추가 (JSON 요청 본문 사용)
        router.postJson("/recommend", (req, res) -> {
            try {
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");

                if (user == null) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }

                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "요청 데이터를 읽는 중 오류가 발생했습니다: " + e.getMessage());
                    return errorResult;
                }
                
                com.google.gson.JsonObject jsonRequest = gson.fromJson(sb.toString(), com.google.gson.JsonObject.class);

                if (!jsonRequest.has("freeboardUid") || !jsonRequest.has("type")) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("message", "필수 파라미터 (freeboardUid, type)가 누락되었습니다.");
                    return errorResult;
                }

                long freeboardUid = jsonRequest.get("freeboardUid").getAsLong();
                String type = jsonRequest.get("type").getAsString(); // 예: "like", "unlike"

                // 서비스 호출하여 추천 처리
                boolean recommendationSuccess = freeboardService.handleRecommendation(freeboardUid, user.getUserUid(), type);
                
                Map<String, Object> responseMap = new HashMap<>();
                String message;

                if (recommendationSuccess) {
                    message = "추천이 " + (type.equalsIgnoreCase("like") ? "반영" : "취소") + "되었습니다.";
                    responseMap.put("success", true);
                } else {
                    // 실패 원인은 서비스 로직에 따라 다를 수 있음 (예: 이미 추천/취소됨, DB 오류 등)
                    // 여기서는 일반적인 실패 메시지를 사용합니다.
                    message = "추천 처리에 실패했습니다. 이미 처리되었거나 내부 오류일 수 있습니다.";
                    responseMap.put("success", false);
                    // 필요시 특정 HTTP 상태 코드 설정 (예: res.setStatus(HttpServletResponse.SC_CONFLICT);)
                }
                
                // 업데이트된 추천 수 가져오기
                int currentLikeCount = freeboardService.getPostLikeCount(freeboardUid);
                
                responseMap.put("message", message);
                responseMap.put("likeCount", currentLikeCount);
                // 필요하다면 사용자의 현재 추천 상태 (예: "liked", "unliked")도 추가할 수 있습니다.
                // String userAction = freeboardService.getUserRecommendationForPost(user.getUserUid(), freeboardUid);
                // responseMap.put("userAction", userAction);

                return responseMap;

            } catch (com.google.gson.JsonSyntaxException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 JSON 형식입니다: " + e.getMessage());
                return errorResult;
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "잘못된 freeboardUid 형식입니다.");
                return errorResult;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "추천 처리 중 오류", e);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null) {
            boolean handled = router.handleGetJson(request, response);
            if (handled) {
                return;
            }
        }
        
        String action = request.getParameter("action");
        
        if (action == null || action.equals("list")) {
            getAllFreeboards(request, response);
        } else if (action.equals("view")) {
            getFreeboardById(request, response);
        } else if (action.equals("write")) {
            request.getRequestDispatcher("/WEB-INF/views/board/freeboard-write.jsp").forward(request, response);
        } else if (action.equals("edit")) {
            showEditForm(request, response);
        } else if (action.equals("downloadAttachment")) {
            downloadAttachment(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null) {
            boolean handled = router.handlePostJson(request, response);
            if (handled) {
                return;
            }
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (action.equals("write")) {
            postFreeboard(request, response);
        } else if (action.equals("edit")) {
            updateFreeboardById(request, response);
        } else if (action.equals("delete")) {
            deleteFreeboardById(request, response);
        } else if (action.equals("notice")) {
            setNoticeById(request, response);
        } else if (action.equals("hide")) {
            hideFreeboardById(request, response);
        } else if (action.equals("report")) {
            reportFreeboardById(request, response);
        } else if (action.equals("reportUser")) {
            reportUserById(request, response);
        } else if (action.equals("deleteAttach")) {
            deleteFreeboardAttachByFilename(request, response);
        } else if (action.equals("uploadAttachment")) {
            uploadAttachment(request, response);
        } else if (action.equals("addComment")) {
            addComment(request, response);
        } else if (action.equals("updateComment")) {
            updateComment(request, response);
        } else if (action.equals("deleteComment")) {
            deleteComment(request, response);
        } else if (action.equals("getComments")) {
            getCommentsByPostId(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void getAllFreeboards(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        
        sendJsonResponse(response, result);
    }
    
    private void getFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            FreeboardDTO freeboard = freeboardService.getFreeboardById(postId);
            
            if (freeboard == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "게시글을 찾을 수 없습니다.");
                return;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("freeboard", freeboard);
            
            List<FreeboardCommentDTO> comments = freeboardService.getCommentsByPostId(postId);
            result.put("comments", comments);
            
            sendJsonResponse(response, result);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
        }
    }

    private void postFreeboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("redirect", request.getContextPath() + "/login?redirect=freeboard&action=write");
            sendJsonResponse(response, result);
            return;
        }
        
        try {
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String clientIp = IpUtil.getClientIpAddr(request);
            
            if (title == null || title.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "제목을 입력해주세요.");
                sendJsonResponse(response, errorResult);
                return;
            }
            
            if (content == null || content.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "내용을 입력해주세요.");
                sendJsonResponse(response, errorResult);
                return;
            }
            
            FreeboardDTO freeboard = new FreeboardDTO(title, content, clientIp, user.getUserUid());
            
            boolean result = freeboardService.createFreeboard(freeboard);
            
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
            
            sendJsonResponse(response, responseData);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "게시글 등록 중 오류가 발생했습니다: " + e.getMessage());
            sendJsonResponse(response, errorResult);
        }
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
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
            
            if (freeboard.getUserUid() != user.getUserUid() && 
                    !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "수정 권한이 없습니다.");
                sendJsonResponse(response, result);
                return;
            }
            
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

    private void updateFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
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
    
    private void deleteFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
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
    
    private void setNoticeById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
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
    
    private void hideFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
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
    
    private void reportFreeboardById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
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
    
    public void reportUserById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDTO reporter = (UserDTO) request.getSession().getAttribute("user");
        if (reporter == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=need_login");
            return;
        }
        
        try {
            long targetUserId = Long.parseLong(request.getParameter("userId"));
            String reportReason = request.getParameter("reason");
            String reportCategory = request.getParameter("category");
            
            reportCategory = validateReportCategory(reportCategory);
            
            if (targetUserId == reporter.getUserUid()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"자신을 신고할 수 없습니다.\"}");
                return;
            }
            
            boolean success = freeboardService.reportUser(targetUserId, reporter.getUserUid(), 
                                                         reportReason, reportCategory);
            
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
    
    public void deleteFreeboardAttachByFilename(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDTO admin = (UserDTO) request.getSession().getAttribute("user");
        if (admin == null || !("admin".equals(admin.getUserAuthority()) || "armband".equals(admin.getUserAuthority()))) {
            response.sendRedirect(request.getContextPath() + "/login?error=need_admin");
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("postId"));
            String filename = request.getParameter("filename");
            String reason = request.getParameter("reason");
            
            if (filename == null || filename.trim().isEmpty()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"파일명이 필요합니다.\"}");
                return;
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                reason = "관리자에 의한 삭제";
            }
            
            boolean success = freeboardService.deleteAttachByFilename(postId, filename, reason, admin.getUserUid());
            
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

    public void uploadAttachment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDTO user = (UserDTO) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }
        
        try {
            Part filePart = request.getPart("file");
            String postIdStr = request.getParameter("postId");
            long postId = Long.parseLong(postIdStr);
            
            if (filePart == null || filePart.getSize() == 0) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"파일이 선택되지 않았습니다.\"}");
                return;
            }
            
            String fileName = filePart.getSubmittedFileName();
            long fileSize = filePart.getSize();
            
            String uploadDirPath = FileUtil.getUploadDirectoryPath();
            String uniqueFileName = FileUtil.generateUniqueFilename(fileName);
            
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            File destinationFile = new File(uploadDir, uniqueFileName);
            
            filePart.write(destinationFile.getAbsolutePath());
            
            AttachmentDTO attachment = new AttachmentDTO();
            attachment.setPostId(postId);
            attachment.setFileName(fileName);
            attachment.setFilePath(uniqueFileName);
            attachment.setFileSize(fileSize);
            
            boolean success = freeboardService.addAttachment(attachment);
            
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
    
    private void downloadAttachment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = request.getParameter("filename");
        if (fileName == null || fileName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "파일명이 없습니다.");
            return;
        }
        
        try {
            AttachmentDTO attachment = freeboardService.getAttachmentByFilename(fileName);
            
            if (attachment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
                return;
            }
            
            String uploadDirPath = FileUtil.getUploadDirectoryPath();
            File file = new File(uploadDirPath, fileName);
            
            if (!file.exists() || !file.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "서버에 파일이 존재하지 않습니다.");
                return;
            }
            
            String mimeType = request.getServletContext().getMimeType(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            response.setContentType(mimeType);
            response.setContentLength((int) file.length());
            
            String originFilename = attachment.getFileName();
            String userAgent = request.getHeader("User-Agent");
            
            if (userAgent.contains("MSIE") || userAgent.contains("Trident") || userAgent.contains("Edge")) {
                originFilename = URLEncoder.encode(originFilename, "UTF-8").replaceAll("\\+", "%20");
            } else {
                originFilename = new String(originFilename.getBytes("UTF-8"), "ISO-8859-1");
            }
            
            response.setHeader("Content-Disposition", "attachment; filename=\"" + originFilename + "\"");
            
            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            
            freeboardService.increaseDownloadCount(attachment.getAttachId());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "파일 다운로드 처리 중 오류", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "파일 다운로드 중 오류가 발생했습니다.");
        }
    }
    
    private void updateComment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        String commentIdStr = request.getParameter("commentId");
        String content = request.getParameter("content");
        
        if (content == null || content.trim().isEmpty()) {
            sendJsonResponse(response, false, "댓글 내용을 입력해주세요.");
            return;
        }
        
        try {
            long commentId = Long.parseLong(commentIdStr);
            
            FreeboardCommentDTO existingComment = freeboardService.getCommentById(commentId);
            
            if (existingComment == null) {
                sendJsonResponse(response, false, "댓글을 찾을 수 없습니다.");
                return;
            }
            
            existingComment.setFreeboardCommentContents(content);
            
            boolean success = freeboardService.updateComment(existingComment, user.getUserUid(), user.getUserAuthority());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "댓글이 수정되었습니다.");
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
    
    private void deleteComment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        String commentIdStr = request.getParameter("commentId");
        
        try {
            long commentId = Long.parseLong(commentIdStr);
            
            FreeboardCommentDTO existingComment = freeboardService.getCommentById(commentId);
            
            if (existingComment == null) {
                sendJsonResponse(response, false, "댓글을 찾을 수 없습니다.");
                return;
            }
            
            boolean success = freeboardService.deleteComment(commentId, user.getUserUid(), user.getUserAuthority());
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "댓글이 삭제되었습니다.");
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
    
    private void addComment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            sendJsonResponse(response, false, "로그인이 필요합니다.");
            return;
        }
        
        String postIdStr = request.getParameter("postId");
        String content = request.getParameter("content");
        
        if (content == null || content.trim().isEmpty()) {
            sendJsonResponse(response, false, "댓글 내용을 입력해주세요.");
            return;
        }
        
        try {
            long postId = Long.parseLong(postIdStr);
            String clientIp = IpUtil.getClientIpAddr(request);
            
            FreeboardCommentDTO comment = new FreeboardCommentDTO(
                    postId, 
                    user.getUserUid(), 
                    content, 
                    clientIp);
            
            boolean success = freeboardService.addComment(comment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                result.put("message", "댓글이 등록되었습니다.");
                result.put("comments", freeboardService.getCommentsByPostId(postId));
            } else {
                result.put("message", "댓글 등록에 실패했습니다.");
            }
            
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
    
    private void getCommentsByPostId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String postIdStr = request.getParameter("postId");
        
        try {
            long postId = Long.parseLong(postIdStr);
            List<FreeboardCommentDTO> comments = freeboardService.getCommentsByPostId(postId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("comments", comments);
            
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
    
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }
    
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        sendJsonResponse(response, result);
    }
    
    private String validateReportCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "spam_ad";
        }
        
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
        
        return "spam_ad";
    }
}
