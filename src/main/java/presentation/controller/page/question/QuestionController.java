package presentation.controller.page.question;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import business.service.question.QuestionService;
import com.google.gson.Gson;
import dto.board.AnswerDTO;
import dto.board.AttachmentDTO;
import dto.board.QuestionDTO;
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
import util.logging.LoggerConfig;
import util.web.IpUtil;
import util.web.RequestRouter;

/**
 * 질문 관련 기능을 처리하는 컨트롤러
 * URL 패턴: /question/* 및 /question.do 형식 지원
 */
@WebServlet({"/question/*", "/question.do"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,     // 10 MB
    maxRequestSize = 1024 * 1024 * 50   // 50 MB
)
public class QuestionController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private QuestionService questionService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();
    
    @Override
    public void init() throws ServletException {
        super.init();
        questionService = new QuestionService();
        
        // 라우터 설정
        initRequestRouter();
    }
    
    /**
     * 요청 라우터 초기화
     */
    private void initRequestRouter() {
        router = new util.web.RequestRouter();
          // GET 요청 JSON 라우터 설정
        router.getJson("/api/questions", (req, res) -> {
            // 기본 페이지네이션 값 설정
            int page = 1;
            int pageSize = 10;
            
            // 요청에서 페이지네이션 파라미터 추출
            try {
                String pageParam = req.getParameter("page");
                String sizeParam = req.getParameter("size");
                
                if (pageParam != null && !pageParam.trim().isEmpty()) {
                    page = Integer.parseInt(pageParam);
                }
                
                if (sizeParam != null && !sizeParam.trim().isEmpty()) {
                    pageSize = Integer.parseInt(sizeParam);
                }
            } catch (NumberFormatException e) {
                // 파라미터 변환 실패 시 기본값 사용
            }
            
            List<QuestionDTO> questions = questionService.getAllQuestions(page, pageSize);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", questions);
            return result;
        });
        
        router.getJson("/api/questions/([0-9]+)", (req, res) -> {
            try {
                long questionId = Long.parseLong(req.getPathInfo().split("/")[3]);
                QuestionDTO question = questionService.getQuestionById(questionId);
                
                Map<String, Object> result = new HashMap<>();
                if (question != null) {
                    result.put("status", "success");
                    result.put("data", question);
                } else {
                    result.put("status", "error");
                    result.put("message", "질문을 찾을 수 없습니다.");
                }
                return result;
            } catch (Exception e) {
                LoggerConfig.logError(QuestionController.class, "getQuestionById", "질문 조회 오류", e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "질문 조회 중 오류가 발생했습니다.");
                return errorResult;
            }
        });
        
        // POST 요청 JSON 라우터 설정
        router.postJson("/api/questions", (req, res) -> {
            try {
                // 사용자 인증 확인
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");
                
                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }
                
                // 질문 데이터 준비
                String title = req.getParameter("title");
                String content = req.getParameter("content");
                
                if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "제목과 내용을 입력해주세요.");
                    return errorResult;
                }
                  QuestionDTO question = new QuestionDTO();
                question.setTitle(title);
                question.setContent(content);
                question.setUserUid(user.getUserId());
                
                boolean success = questionService.createQuestion(question);
                
                Map<String, Object> result = new HashMap<>();
                if (success) {
                    result.put("status", "success");
                    result.put("message", "질문이 등록되었습니다.");
                    result.put("questionId", question.getQuestionId());
                } else {
                    result.put("status", "error");
                    result.put("message", "질문 등록에 실패했습니다.");
                }
                return result;
            } catch (Exception e) {
                LoggerConfig.logError(QuestionController.class, "createQuestion", "질문 등록 오류", e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "질문 등록 중 오류가 발생했습니다.");
                return errorResult;
            }
        });
    }
    
    /**
     * .do 요청 처리 메소드
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        
        // .do 요청 처리
        if (uri.endsWith(".do")) {
            String action = request.getParameter("action");
            
            if ("list".equals(action)) {
                getAllQuestions(request, response);
            } else if ("view".equals(action)) {
                getQuestionById(request, response);
            } else if ("write".equals(action)) {
                showQuestionForm(request, response);
            } else if ("edit".equals(action)) {
                showEditQuestionForm(request, response);
            } else if ("post".equals(action)) {
                postQuestion(request, response);
            } else if ("update".equals(action)) {
                updateQuestion(request, response);
            } else if ("delete".equals(action)) {
                deleteQuestion(request, response);
            } else if ("answer".equals(action)) {
                postAnswer(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }
        
        // API 경로를 확인해서 라우터로 처리
        if (request.getPathInfo() != null && request.getPathInfo().startsWith("/api/")) {
            try {
                boolean handled = false;
                
                if ("GET".equals(request.getMethod())) {
                    handled = router.handleGetJson(request, response);
                } else if ("POST".equals(request.getMethod())) {
                    handled = router.handlePostJson(request, response);
                }
                
                if (!handled) {
                    sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                        Map.of("status", "error", "message", "API 경로를 찾을 수 없습니다."));
                }
                return;
            } catch (Exception e) {
                LoggerConfig.logError(QuestionController.class, "service", "API 처리 오류", e);
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
                return;
            }
        }
        
        // 기본 서블릿 처리
        super.service(request, response);
    }
    
    /**
     * JSON 응답 전송 유틸리티 메소드
     */
    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            // 기본 Q&A 목록 페이지 표시
            getAllQuestions(request, response);
        } else if (action.equals("view")) {
            // 질문 상세 조회
            getQuestionById(request, response);
        } else if (action.equals("write")) {
            // 질문 작성 페이지
            showQuestionForm(request, response);
        } else if (action.equals("edit")) {
            // 질문 수정 페이지
            showEditQuestionForm(request, response);
        } else if (action.equals("myquestions")) {
            // 내 질문 목록
            getMyQuestions(request, response);
        } else if (action.equals("user")) {
            // 특정 사용자의 질문 목록
            getQuestionsByUserId(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (action.equals("post")) {
            // 질문 등록
            postQuestion(request, response);
        } else if (action.equals("update")) {
            // 질문 수정
            updateQuestion(request, response);
        } else if (action.equals("delete")) {
            // 질문 삭제
            deleteQuestion(request, response);
        } else if (action.equals("answer")) {
            // 답변 등록
            postAnswer(request, response);
        } else if (action.equals("updateAnswer")) {
            // 답변 수정
            updateAnswer(request, response);
        } else if (action.equals("deleteAnswer")) {
            // 답변 삭제
            deleteAnswer(request, response);
        } else if (action.equals("uploadAttachment")) {
            // 첨부파일 업로드
            uploadAttachment(request, response);
        } else if (action.equals("downloadAttachment")) {
            // 첨부파일 다운로드
            downloadAttachment(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * 모든 질문 목록을 조회하여 표시
     */
    private void getAllQuestions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 기본 페이지네이션 값 설정
            int page = 1;
            int pageSize = 10;
            
            // 요청에서 페이지네이션 파라미터 추출
            String pageParam = request.getParameter("page");
            String sizeParam = request.getParameter("size");
            
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            
            if (sizeParam != null && !sizeParam.trim().isEmpty()) {
                pageSize = Integer.parseInt(sizeParam);
            }
            
            // 질문 목록 조회
            List<QuestionDTO> questions = questionService.getAllQuestions(page, pageSize);
            int totalQuestions = questionService.getTotalQuestions();
            int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
            
            // JSON 응답 보내기
            Map<String, Object> result = new HashMap<>();
            result.put("questions", questions);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalQuestions", totalQuestions);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "getAllQuestions", "질문 목록 조회 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "질문 목록을 불러오는 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 특정 질문의 상세 정보 조회
     */
    private void getQuestionById(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String questionIdParam = request.getParameter("id");
            
            if (questionIdParam == null || questionIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "질문 ID가 제공되지 않았습니다."));
                return;
            }
            
            long questionId = Long.parseLong(questionIdParam);
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 질문을 찾을 수 없습니다."));
                return;
            }
            
            // 해당 질문에 대한 답변 목록도 조회
            List<AnswerDTO> answers = questionService.getAnswersByQuestionId(questionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("question", question);
            result.put("answers", answers);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 질문 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "getQuestionById", "질문 상세 조회 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "질문 상세 정보를 불러오는 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 새 질문 작성 페이지 표시
     */
    private void showQuestionForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // HTML/JSON 응답으로 질문 폼 관련 데이터 반환
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "새 질문 작성 페이지");
        
        sendJsonResponse(response, HttpServletResponse.SC_OK, result);
    }
    
    /**
     * 질문 수정 페이지 표시
     */
    private void showEditQuestionForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String questionIdParam = request.getParameter("id");
            
            if (questionIdParam == null || questionIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "질문 ID가 제공되지 않았습니다."));
                return;
            }
            
            long questionId = Long.parseLong(questionIdParam);
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 질문을 찾을 수 없습니다."));
                return;
            }
            
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null || user.getUserId() != question.getUserUid()) {
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    Map.of("status", "error", "message", "이 질문을 수정할 권한이 없습니다."));
                return;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("question", question);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 질문 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "showEditQuestionForm", "질문 수정 폼 로딩 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "질문 수정 정보를 불러오는 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 새 질문 등록 처리
     */
    private void postQuestion(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 요청 데이터 가져오기
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            
            // 유효성 검사
            if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "제목과 내용을 모두 입력해야 합니다."));
                return;
            }
            
            // DTO 객체 생성 및 값 설정
            QuestionDTO question = new QuestionDTO();
            question.setTitle(title);
            question.setContent(content);
            question.setUserUid(user.getUserId());
            
            // IP 주소 기록
            String ipAddress = IpUtil.getClientIpAddr(request);
            question.setAuthorIp(ipAddress);
            
            // 질문 등록
            boolean success = questionService.createQuestion(question);
            
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, 
                    Map.of("status", "success", 
                           "message", "질문이 성공적으로 등록되었습니다.", 
                           "questionId", question.getQuestionId()));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "질문 등록에 실패했습니다."));
            }
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "postQuestion", "질문 등록 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "질문 등록 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 질문 수정 처리
     */
    private void updateQuestion(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 요청 데이터 가져오기
            String questionIdParam = request.getParameter("id");
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            
            // 유효성 검사
            if (questionIdParam == null || title == null || content == null || 
                questionIdParam.trim().isEmpty() || title.trim().isEmpty() || content.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "질문 ID, 제목, 내용을 모두 입력해야 합니다."));
                return;
            }
            
            long questionId = Long.parseLong(questionIdParam);
            
            // 기존 질문 정보 가져오기
            QuestionDTO originalQuestion = questionService.getQuestionById(questionId);
            
            if (originalQuestion == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 질문을 찾을 수 없습니다."));
                return;
            }
            
            // 작성자 확인
            if (originalQuestion.getUserUid() != user.getUserId()) {
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    Map.of("status", "error", "message", "이 질문을 수정할 권한이 없습니다."));
                return;
            }
            
            // 질문 정보 업데이트
            originalQuestion.setTitle(title);
            originalQuestion.setContent(content);
            
            // IP 주소 갱신
            String ipAddress = IpUtil.getClientIpAddr(request);
            originalQuestion.setAuthorIp(ipAddress);            // 질문 업데이트 (사용자 ID와 권한 전달)
            boolean success = questionService.updateQuestion(originalQuestion, user.getUserId(), user.getUserAuthority());
            
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, 
                    Map.of("status", "success", "message", "질문이 성공적으로 수정되었습니다."));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "질문 수정에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 질문 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "updateQuestion", "질문 수정 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "질문 수정 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 질문 삭제 처리
     */
    private void deleteQuestion(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 요청 데이터 가져오기
            String questionIdParam = request.getParameter("id");
            
            if (questionIdParam == null || questionIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "질문 ID가 제공되지 않았습니다."));
                return;
            }
            
            long questionId = Long.parseLong(questionIdParam);
            
            // 기존 질문 정보 가져오기
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 질문을 찾을 수 없습니다."));
                return;
            }
            
            // 작성자 또는 관리자인지 확인
            boolean isAdmin = "admin".equals(user.getUserAuthority());
            if (!isAdmin && question.getUserUid() != user.getUserId()) {
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    Map.of("status", "error", "message", "이 질문을 삭제할 권한이 없습니다."));
                return;
            }
              // 질문 삭제 (논리적 삭제)
            String deleteReason = request.getParameter("reason");
            if (deleteReason == null || deleteReason.isEmpty()) {
                deleteReason = "사용자 요청";  // 기본 삭제 이유
            }
            
            boolean success = questionService.deleteQuestion(questionId, user.getUserId(), deleteReason);
            
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, 
                    Map.of("status", "success", "message", "질문이 성공적으로 삭제되었습니다."));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "질문 삭제에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 질문 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "deleteQuestion", "질문 삭제 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "질문 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 답변 등록 처리
     */
    private void postAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 요청 데이터 가져오기
            String questionIdParam = request.getParameter("questionId");
            String content = request.getParameter("content");
            
            // 유효성 검사
            if (questionIdParam == null || content == null || 
                questionIdParam.trim().isEmpty() || content.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "질문 ID와 답변 내용을 모두 입력해야 합니다."));
                return;
            }
            
            long questionId = Long.parseLong(questionIdParam);
            
            // 질문이 존재하는지 확인
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 질문을 찾을 수 없습니다."));
                return;
            }
            
            // DTO 객체 생성 및 값 설정
            AnswerDTO answer = new AnswerDTO();
            answer.setQuestionId(questionId);
            answer.setContent(content);
            answer.setUserUid(user.getUserId());
            
            // IP 주소 기록
            String ipAddress = IpUtil.getClientIpAddr(request);
            answer.setAuthorIp(ipAddress);
            
            // 답변 등록
            boolean success = questionService.createAnswer(answer);
            
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, 
                    Map.of("status", "success", 
                           "message", "답변이 성공적으로 등록되었습니다.", 
                           "answerId", answer.getAnswerId()));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "답변 등록에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 질문 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "postAnswer", "답변 등록 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "답변 등록 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 답변 수정 처리
     */
    private void updateAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 요청 데이터 가져오기
            String answerIdParam = request.getParameter("id");
            String content = request.getParameter("content");
            
            // 유효성 검사
            if (answerIdParam == null || content == null || 
                answerIdParam.trim().isEmpty() || content.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "답변 ID와 내용을 모두 입력해야 합니다."));
                return;
            }
            
            long answerId = Long.parseLong(answerIdParam);
            
            // 기존 답변 정보 가져오기
            AnswerDTO originalAnswer = questionService.getAnswerById(answerId);
            
            if (originalAnswer == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 답변을 찾을 수 없습니다."));
                return;
            }
            
            // 작성자 확인
            if (originalAnswer.getUserUid() != user.getUserId()) {
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    Map.of("status", "error", "message", "이 답변을 수정할 권한이 없습니다."));
                return;
            }
            
            // 답변 정보 업데이트
            originalAnswer.setContent(content);
            
            // IP 주소 갱신
            String ipAddress = IpUtil.getClientIpAddr(request);
            originalAnswer.setAuthorIp(ipAddress);
              // 답변 업데이트
            boolean success = questionService.updateAnswer(originalAnswer, user.getUserId(), user.getUserAuthority());
            
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, 
                    Map.of("status", "success", "message", "답변이 성공적으로 수정되었습니다."));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "답변 수정에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 답변 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "updateAnswer", "답변 수정 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "답변 수정 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 답변 삭제 처리
     */
    private void deleteAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 요청 데이터 가져오기
            String answerIdParam = request.getParameter("id");
            
            if (answerIdParam == null || answerIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "답변 ID가 제공되지 않았습니다."));
                return;
            }
            
            long answerId = Long.parseLong(answerIdParam);
            
            // 기존 답변 정보 가져오기
            AnswerDTO answer = questionService.getAnswerById(answerId);
            
            if (answer == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 답변을 찾을 수 없습니다."));
                return;
            }
            
            // 작성자 또는 관리자인지 확인
            boolean isAdmin = "admin".equals(user.getUserAuthority());
            if (!isAdmin && answer.getUserUid() != user.getUserId()) {
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    Map.of("status", "error", "message", "이 답변을 삭제할 권한이 없습니다."));
                return;
            }
              // 답변 삭제 (논리적 삭제)
            String deleteReason = request.getParameter("reason");
            if (deleteReason == null || deleteReason.isEmpty()) {
                deleteReason = "사용자 요청";  // 기본 삭제 이유
            }
            
            boolean success = questionService.deleteAnswer(answerId, user.getUserId(), deleteReason);
            
            if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, 
                    Map.of("status", "success", "message", "답변이 성공적으로 삭제되었습니다."));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "답변 삭제에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 답변 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "deleteAnswer", "답변 삭제 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "답변 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 첨부 파일 업로드 처리
     */
    private void uploadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 질문 ID 가져오기
            String questionIdParam = request.getParameter("questionId");
            
            if (questionIdParam == null || questionIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "질문 ID가 제공되지 않았습니다."));
                return;
            }
            
            long questionId = Long.parseLong(questionIdParam);
            
            // 질문이 존재하는지 확인
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 질문을 찾을 수 없습니다."));
                return;
            }
            
            // 질문 작성자 확인
            if (question.getUserUid() != user.getUserId()) {
                sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    Map.of("status", "error", "message", "이 질문에 파일을 첨부할 권한이 없습니다."));
                return;
            }
            
            // 첨부 파일 처리
            Part filePart = request.getPart("file");
            
            if (filePart == null || filePart.getSize() == 0) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "파일이 제공되지 않았습니다."));
                return;
            }
            
            // 파일 저장 처리
            String uploadPath = AppConfig.getUploadPath();
            String fileName = FileUtil.getSubmittedFileName(filePart);
            String savedFileName = System.currentTimeMillis() + "_" + fileName;
            String filePath = uploadPath + File.separator + savedFileName;
            
            // 디렉토리 생성
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 파일 저장
            filePart.write(filePath);
              // DB에 첨부 파일 정보 저장
            AttachmentDTO attachment = new AttachmentDTO();
            attachment.setPostId(questionId);
            attachment.setFileName(fileName);
            attachment.setFilePath(savedFileName);
            attachment.setFileSize((int) filePart.getSize());
            
            boolean success = questionService.addAttachment(questionId, fileName, savedFileName, filePart.getSize());              if (success) {
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, 
                    Map.of("status", "success", 
                           "message", "파일이 성공적으로 업로드되었습니다."));
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    Map.of("status", "error", "message", "파일 업로드에 실패했습니다."));
            }
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 질문 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "uploadAttachment", "파일 업로드 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "파일 업로드 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 첨부 파일 다운로드 처리
     */
    private void downloadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        FileInputStream fileInputStream = null;
        
        try {
            // 첨부 파일 ID 가져오기
            String attachmentIdParam = request.getParameter("id");
            
            if (attachmentIdParam == null || attachmentIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "첨부 파일 ID가 제공되지 않았습니다."));
                return;
            }
            
            long attachmentId = Long.parseLong(attachmentIdParam);
            
            // 첨부 파일 정보 가져오기
            AttachmentDTO attachment = questionService.getAttachmentById(attachmentId);
            
            if (attachment == null) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "해당 ID의 첨부 파일을 찾을 수 없습니다."));
                return;
            }
            
            // 파일 존재 여부 확인
            String uploadPath = AppConfig.getUploadPath();
            String filePath = uploadPath + File.separator + attachment.getFilePath();
            File file = new File(filePath);
            
            if (!file.exists()) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                    Map.of("status", "error", "message", "파일을 찾을 수 없습니다."));
                return;
            }
            
            // 파일 다운로드 설정
            response.setContentType("application/octet-stream");
            response.setContentLength((int) file.length());
            
            String encodedFileName = URLEncoder.encode(attachment.getFileName(), "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 파일 전송
            fileInputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.flush();
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 첨부 파일 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "downloadAttachment", "파일 다운로드 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "파일 다운로드 중 오류가 발생했습니다."));
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    // 무시
                }
            }
        }
    }
    
    /**
     * 내 질문 목록 조회
     */
    private void getMyQuestions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 사용자 인증 확인
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            
            if (user == null) {
                sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    Map.of("status", "error", "message", "로그인이 필요합니다."));
                return;
            }
            
            // 기본 페이지네이션 값 설정
            int page = 1;
            int pageSize = 10;
            
            // 요청에서 페이지네이션 파라미터 추출
            String pageParam = request.getParameter("page");
            String sizeParam = request.getParameter("size");
            
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            
            if (sizeParam != null && !sizeParam.trim().isEmpty()) {
                pageSize = Integer.parseInt(sizeParam);
            }
            
            // 사용자의 질문 목록 조회
            List<QuestionDTO> questions = questionService.getQuestionsByUserId(user.getUserId(), page, pageSize);
            int totalQuestions = questionService.getTotalQuestionsByUserId(user.getUserId());
            int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
            
            // JSON 응답 보내기
            Map<String, Object> result = new HashMap<>();
            result.put("questions", questions);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalQuestions", totalQuestions);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "getMyQuestions", "내 질문 목록 조회 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "내 질문 목록을 불러오는 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 특정 사용자의 질문 목록 조회
     */
    private void getQuestionsByUserId(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String userIdParam = request.getParameter("userId");
            
            if (userIdParam == null || userIdParam.trim().isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    Map.of("status", "error", "message", "사용자 ID가 제공되지 않았습니다."));
                return;
            }
            
            long userId = Long.parseLong(userIdParam);
            
            // 기본 페이지네이션 값 설정
            int page = 1;
            int pageSize = 10;
            
            // 요청에서 페이지네이션 파라미터 추출
            String pageParam = request.getParameter("page");
            String sizeParam = request.getParameter("size");
            
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            
            if (sizeParam != null && !sizeParam.trim().isEmpty()) {
                pageSize = Integer.parseInt(sizeParam);
            }
            
            // 사용자의 질문 목록 조회
            List<QuestionDTO> questions = questionService.getQuestionsByUserId(userId, page, pageSize);
            int totalQuestions = questionService.getTotalQuestionsByUserId(userId);
            int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
            
            // JSON 응답 보내기
            Map<String, Object> result = new HashMap<>();
            result.put("questions", questions);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalQuestions", totalQuestions);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, result);
        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                Map.of("status", "error", "message", "잘못된 사용자 ID 형식입니다."));
        } catch (Exception e) {
            LoggerConfig.logError(QuestionController.class, "getQuestionsByUserId", "사용자별 질문 목록 조회 중 오류 발생", e);
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                Map.of("status", "error", "message", "사용자별 질문 목록을 불러오는 중 오류가 발생했습니다."));
        }
    }
}