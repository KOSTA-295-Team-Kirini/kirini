package presentation.controller.page.board;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

import business.service.question.QuestionService;
import dto.board.AnswerDTO;
import dto.board.AttachmentDTO;
import dto.board.QuestionDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import presentation.controller.page.Controller;
import util.FileUtil;
import util.config.AppConfig;
import util.web.IpUtil;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,     // 10 MB
    maxRequestSize = 1024 * 1024 * 50   // 50 MB
)
public class QuestionController implements Controller {
    private final QuestionService questionService;
    
    public QuestionController() {
        this.questionService = new QuestionService();
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
     * Q&A 질문 작성 페이지 표시
     */
    private void showQuestionForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=question&action=write");
            return;
        }
        
        request.getRequestDispatcher("/WEB-INF/views/board/question-form.jsp").forward(request, response);
    }
    
    /**
     * Q&A 질문 수정 페이지 표시
     */
    private void showEditQuestionForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=question&action=edit");
            return;
        }
        
        try {
            long questionId = Long.parseLong(request.getParameter("id"));
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "질문을 찾을 수 없습니다.");
                return;
            }
            
            // 작성자 본인 또는 관리자만 수정 가능
            if (question.getUserUid() != user.getUserUid() && 
                    !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                return;
            }
            
            request.setAttribute("question", question);
            request.getRequestDispatcher("/WEB-INF/views/board/question-edit.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 질문 ID입니다.");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * Q&A 질문 작성
     */
    private void postQuestion(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=question&action=write");
            }
            return;
        }
        
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String category = request.getParameter("category");
        String clientIp = IpUtil.getClientIpAddr(request);
        
        // 필수 입력값 검증
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"제목과 내용을 모두 입력해주세요.\"}");
            } else {
                request.setAttribute("error", "제목과 내용을 모두 입력해주세요.");
                request.getRequestDispatcher("/WEB-INF/views/board/question-form.jsp").forward(request, response);
            }
            return;
        }
        
        // category가 없으면 기본값 설정
        if (category == null || category.trim().isEmpty()) {
            category = "question"; // 기본값은 'question'
        }
        
        // DTO 객체 생성
        QuestionDTO question = new QuestionDTO();
        question.setTitle(title);
        question.setContent(content);
        question.setCategory(category);
        question.setAuthorIp(clientIp);
        question.setUserUid(user.getUserUid());
        
        try {
            boolean result = questionService.createQuestion(question);
            
            if (result) {
                // 첨부파일이 있는 경우 처리 (멀티파트)
                try {
                    Part filePart = request.getPart("file");
                    if (filePart != null && filePart.getSize() > 0) {
                        // 파일 업로드 처리
                        String fileName = filePart.getSubmittedFileName();
                        String uploadPath = FileUtil.getUploadDirectoryPath();
                        String uniqueFileName = FileUtil.generateUniqueFilename(fileName);
                        String filePath = new File(uploadPath, uniqueFileName).getAbsolutePath();
                        
                        filePart.write(filePath);
                        
                        // DB에 첨부파일 정보 저장
                        questionService.addAttachment(question.getQuestionId(), fileName, filePath, filePart.getSize());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 첨부파일 업로드 실패는 전체 등록 실패로 처리하지 않음
                }
                
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\": true, \"message\": \"질문이 등록되었습니다.\", \"id\": " + question.getQuestionId() + "}"
                    );
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + question.getQuestionId());
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"질문 등록에 실패했습니다.\"}");
                } else {
                    request.setAttribute("error", "질문 등록에 실패했습니다.");
                    request.getRequestDispatcher("/WEB-INF/views/board/question-form.jsp").forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
            } else {
                request.setAttribute("error", "서버 오류가 발생했습니다.");
                request.getRequestDispatcher("/WEB-INF/views/board/question-form.jsp").forward(request, response);
            }
        }
    }
    
    /**
     * Q&A 질문 수정
     */
    private void updateQuestion(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            }
            return;
        }
        
        try {
            long questionId = Long.parseLong(request.getParameter("id"));
            QuestionDTO existingQuestion = questionService.getQuestionById(questionId);
            
            if (existingQuestion == null) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"질문을 찾을 수 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "질문을 찾을 수 없습니다.");
                }
                return;
            }
            
            // 작성자 본인 또는 관리자만 수정 가능
            if (existingQuestion.getUserUid() != user.getUserUid() && 
                    !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"수정 권한이 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                }
                return;
            }
            
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String category = request.getParameter("category");
            
            // 필수 입력값 검증
            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"제목과 내용을 모두 입력해주세요.\"}");
                } else {
                    request.setAttribute("error", "제목과 내용을 모두 입력해주세요.");
                    request.setAttribute("question", existingQuestion);
                    request.getRequestDispatcher("/WEB-INF/views/board/question-edit.jsp").forward(request, response);
                }
                return;
            }
            
            // 질문 객체 업데이트
            existingQuestion.setTitle(title);
            existingQuestion.setContent(content);
            if (category != null && !category.trim().isEmpty()) {
                existingQuestion.setCategory(category);
            }
            
            boolean result = questionService.updateQuestion(existingQuestion, user.getUserUid(), user.getUserAuthority());
            
            if (result) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"message\": \"질문이 수정되었습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId);
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"질문 수정에 실패했습니다.\"}");
                } else {
                    request.setAttribute("error", "질문 수정에 실패했습니다.");
                    request.setAttribute("question", existingQuestion);
                    request.getRequestDispatcher("/WEB-INF/views/board/question-edit.jsp").forward(request, response);
                }
            }
        } catch (NumberFormatException e) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"잘못된 질문 ID입니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 질문 ID입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            }
        }
    }
    
    /**
     * Q&A 질문 삭제
     */
    private void deleteQuestion(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            }
            return;
        }
        
        try {
            long questionId = Long.parseLong(request.getParameter("id"));
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"질문을 찾을 수 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "질문을 찾을 수 없습니다.");
                }
                return;
            }
            
            // 작성자 본인 또는 관리자만 삭제 가능
            if (question.getUserUid() != user.getUserUid() && 
                    !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"삭제 권한이 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "삭제 권한이 없습니다.");
                }
                return;
            }
            
            String reason = request.getParameter("reason");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "사용자에 의한 삭제";
            }
            
            boolean result = questionService.deleteQuestion(questionId, user.getUserUid(), reason);
            
            if (result) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"message\": \"질문이 삭제되었습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question");
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"질문 삭제에 실패했습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "질문 삭제에 실패했습니다.");
                }
            }
        } catch (NumberFormatException e) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"잘못된 질문 ID입니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 질문 ID입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            }
        }
    }
    
    /**
     * Q&A 질문 열람 (권한 체크: 작성자 본인 또는 관리자만 확인 가능)
     */
    private void getQuestionById(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            return;
        }
        
        try {
            long questionId = Long.parseLong(request.getParameter("id"));
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "질문을 찾을 수 없습니다.");
                return;
            }
            
            // 작성자 본인 또는 관리자만 조회 가능
            boolean isOwner = question.getUserUid() == user.getUserUid();
            boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
            
            if (!isOwner && !isAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "열람 권한이 없습니다.");
                return;
            }
            
            // 조회수 증가 (중복 방지를 위해 세션 확인)
            String viewedQuestions = (String) session.getAttribute("viewedQuestions");
            if (viewedQuestions == null) {
                viewedQuestions = "," + questionId + ",";
                questionService.increaseViewCount(questionId);
            } else if (!viewedQuestions.contains("," + questionId + ",")) {
                viewedQuestions += questionId + ",";
                questionService.increaseViewCount(questionId);
            }
            session.setAttribute("viewedQuestions", viewedQuestions);
            
            // 첨부파일 정보 조회
            List<AttachmentDTO> attachments = questionService.getAttachmentsByQuestionId(questionId);
            
            // 답변 목록 조회
            List<AnswerDTO> answers = questionService.getAnswersByQuestionId(questionId);
            
            // 사용자 정보 (작성자)
            UserDTO author = questionService.getUserById(question.getUserUid());
            
            request.setAttribute("question", question);
            request.setAttribute("attachments", attachments);
            request.setAttribute("answers", answers);
            request.setAttribute("author", author);
            request.setAttribute("isOwner", isOwner);
            request.setAttribute("isAdmin", isAdmin);
            
            request.getRequestDispatcher("/WEB-INF/views/board/question-view.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 질문 ID입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * 내가 작성한 Q&A 목록 조회
     */
    private void getMyQuestions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=question&action=myquestions");
            return;
        }
        
        // 페이징 처리
        int page = 1;
        int pageSize = 10;
        
        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (NumberFormatException e) {
            // 기본값인 1 사용
        }
        
        try {
            List<QuestionDTO> myQuestions = questionService.getQuestionsByUserId(user.getUserUid(), page, pageSize);
            int totalQuestions = questionService.getTotalQuestionsByUserId(user.getUserUid());
            int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
            
            request.setAttribute("questions", myQuestions);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalQuestions", totalQuestions);
            request.setAttribute("isMyQuestions", true);
            
            request.getRequestDispatcher("/WEB-INF/views/board/question-list.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * 모든 사용자의 Q&A 목록 조회 (관리자용)
     */
    private void getAllQuestions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            return;
        }
        
        // 관리자만 모든 질문 조회 가능 (일반 사용자는 자신의 질문만 조회)
        boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
        
        if (!isAdmin) {
            getMyQuestions(request, response);
            return;
        }
        
        // 페이징 처리
        int page = 1;
        int pageSize = 10;
        
        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (NumberFormatException e) {
            // 기본값인 1 사용
        }
        
        try {
            List<QuestionDTO> allQuestions = questionService.getAllQuestions(page, pageSize);
            int totalQuestions = questionService.getTotalQuestions();
            int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
            
            request.setAttribute("questions", allQuestions);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalQuestions", totalQuestions);
            request.setAttribute("isAdmin", isAdmin);
            
            request.getRequestDispatcher("/WEB-INF/views/board/question-list.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * 특정 사용자의 Q&A 목록 조회 (관리자용)
     */
    private void getQuestionsByUserId(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            return;
        }
        
        // 관리자 권한 확인
        boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
        
        if (!isAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return;
        }
        
        try {
            long targetUserId = Long.parseLong(request.getParameter("userId"));
            
            // 페이징 처리
            int page = 1;
            int pageSize = 10;
            
            try {
                if (request.getParameter("page") != null) {
                    page = Integer.parseInt(request.getParameter("page"));
                }
            } catch (NumberFormatException e) {
                // 기본값인 1 사용
            }
            
            List<QuestionDTO> userQuestions = questionService.getQuestionsByUserId(targetUserId, page, pageSize);
            int totalQuestions = questionService.getTotalQuestionsByUserId(targetUserId);
            int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
            
            // 사용자 정보 조회
            UserDTO targetUser = questionService.getUserById(targetUserId);
            
            request.setAttribute("questions", userQuestions);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalQuestions", totalQuestions);
            request.setAttribute("targetUser", targetUser);
            request.setAttribute("isAdmin", isAdmin);
            
            request.getRequestDispatcher("/WEB-INF/views/board/question-user-list.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 사용자 ID입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
    
    /**
     * Q&A 답변 작성
     */
    private void postAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            }
            return;
        }
        
        // 관리자 권한 확인 (일반 사용자는 본인의 질문에만 답변 가능)
        boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
        
        try {
            long questionId = Long.parseLong(request.getParameter("questionId"));
            String content = request.getParameter("content");
            String clientIp = IpUtil.getClientIpAddr(request);
            
            // 필수 입력값 검증
            if (content == null || content.trim().isEmpty()) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 내용을 입력해주세요.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId + "&error=emptyAnswer");
                }
                return;
            }
            
            // 질문 작성자 또는 관리자만 답변 작성 가능 (권한 확인)
            QuestionDTO question = questionService.getQuestionById(questionId);
            if (question == null) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"질문을 찾을 수 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "질문을 찾을 수 없습니다.");
                }
                return;
            }
            
            boolean isQuestionOwner = question.getUserUid() == user.getUserUid();
            
            if (!isAdmin && !isQuestionOwner) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 작성 권한이 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "답변 작성 권한이 없습니다.");
                }
                return;
            }
            
            // DTO 객체 생성
            AnswerDTO answer = new AnswerDTO();
            answer.setQuestionId(questionId);
            answer.setContent(content);
            answer.setAuthorIp(clientIp);
            answer.setUserUid(user.getUserUid());
            
            boolean result = questionService.createAnswer(answer);
            
            if (result) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\": true, \"message\": \"답변이 등록되었습니다.\", \"id\": " + answer.getAnswerId() + "}"
                    );
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId);
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 등록에 실패했습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId + "&error=answerFailed");
                }
            }
        } catch (NumberFormatException e) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"잘못된 질문 ID입니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 질문 ID입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            }
        }
    }
    
    /**
     * Q&A 답변 수정
     */
    private void updateAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            }
            return;
        }
        
        try {
            long answerId = Long.parseLong(request.getParameter("answerId"));
            long questionId = Long.parseLong(request.getParameter("questionId"));
            String content = request.getParameter("content");
            
            // 필수 입력값 검증
            if (content == null || content.trim().isEmpty()) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 내용을 입력해주세요.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId + "&error=emptyAnswer");
                }
                return;
            }
            
            // 답변 정보 조회
            AnswerDTO answer = questionService.getAnswerById(answerId);
            
            if (answer == null) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변을 찾을 수 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "답변을 찾을 수 없습니다.");
                }
                return;
            }
            
            // 작성자 본인 또는 관리자만 수정 가능 (권한 확인)
            boolean isOwner = answer.getUserUid() == user.getUserUid();
            boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
            
            if (!isOwner && !isAdmin) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 수정 권한이 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "답변 수정 권한이 없습니다.");
                }
                return;
            }
            
            // 답변 객체 업데이트
            answer.setContent(content);
            
            boolean result = questionService.updateAnswer(answer, user.getUserUid(), user.getUserAuthority());
            
            if (result) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"message\": \"답변이 수정되었습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId);
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 수정에 실패했습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId + "&error=updateAnswerFailed");
                }
            }
        } catch (NumberFormatException e) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"잘못된 답변 ID입니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 답변 ID입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            }
        }
    }
    
    /**
     * Q&A 답변 삭제
     */
    private void deleteAnswer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?redirect=question");
            }
            return;
        }
        
        try {
            long answerId = Long.parseLong(request.getParameter("answerId"));
            long questionId = Long.parseLong(request.getParameter("questionId"));
            
            // 답변 정보 조회
            AnswerDTO answer = questionService.getAnswerById(answerId);
            
            if (answer == null) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변을 찾을 수 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "답변을 찾을 수 없습니다.");
                }
                return;
            }
            
            // 작성자 본인 또는 관리자만 삭제 가능 (권한 확인)
            boolean isOwner = answer.getUserUid() == user.getUserUid();
            boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
            
            if (!isOwner && !isAdmin) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 삭제 권한이 없습니다.\"}");
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "답변 삭제 권한이 없습니다.");
                }
                return;
            }
            
            String reason = request.getParameter("reason");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "사용자에 의한 삭제";
            }
            
            boolean result = questionService.deleteAnswer(answerId, user.getUserUid(), reason);
            
            if (result) {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"message\": \"답변이 삭제되었습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId);
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"답변 삭제에 실패했습니다.\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/question?action=view&id=" + questionId + "&error=deleteAnswerFailed");
                }
            }
        } catch (NumberFormatException e) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"잘못된 답변 ID입니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 답변 ID입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            }
        }
    }
    
    /**
     * 첨부파일 업로드
     */
    private void uploadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }
        
        try {
            // 파일 업로드를 위한 멀티파트 요청 처리
            Part filePart = request.getPart("file");
            long questionId = Long.parseLong(request.getParameter("questionId"));
            
            // 질문 정보 조회 및 권한 확인
            QuestionDTO question = questionService.getQuestionById(questionId);
            
            if (question == null) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"질문을 찾을 수 없습니다.\"}");
                return;
            }
            
            boolean isOwner = question.getUserUid() == user.getUserUid();
            boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
            
            if (!isOwner && !isAdmin) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"파일 업로드 권한이 없습니다.\"}");
                return;
            }
            
            // 파일이 비어있는지 확인
            if (filePart == null || filePart.getSize() == 0) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"파일이 선택되지 않았습니다.\"}");
                return;
            }
            
            // 파일 이름 및 크기 추출
            String fileName = filePart.getSubmittedFileName();
            long fileSize = filePart.getSize();
            
            // 파일 확장자 검증
            if (!FileUtil.isAllowedFileType(fileName)) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"허용되지 않는 파일 형식입니다.\"}");
                return;
            }
            
            // MIME 타입 검증
            if (!FileUtil.validateMimeType(filePart)) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"유효하지 않은 파일 타입입니다.\"}");
                return;
            }
            
            // 파일 저장 로직
            // 1. 업로드 디렉토리 경로 얻기
            String uploadDirPath = FileUtil.getUploadDirectoryPath();
            
            // 2. 안전한 고유 파일명 생성
            String uniqueFileName = FileUtil.generateUniqueFilename(fileName);
            
            // 3. 전체 파일 경로 생성
            String filePath = new File(uploadDirPath, uniqueFileName).getAbsolutePath();
            
            // 4. 파일 저장
            filePart.write(filePath);
            
            // 5. DB에 파일 정보 저장
            boolean success = questionService.addAttachment(questionId, fileName, filePath, fileSize);
            
            // 결과 반환
            response.setContentType("application/json");
            if (success) {
                response.getWriter().write(
                    "{\"success\": true, \"message\": \"파일이 업로드되었습니다.\", " +
                    "\"fileName\": \"" + fileName + "\", \"fileSize\": " + fileSize + "}"
                );
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"파일 업로드 중 오류가 발생했습니다.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 첨부파일 다운로드
     */
    private void downloadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long attachId = Long.parseLong(request.getParameter("attachId"));
            
            // 첨부파일 정보 조회
            AttachmentDTO attachment = questionService.getAttachmentById(attachId);
            if (attachment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
                return;
            }
            
            // 권한 확인 (질문 소유자 또는 관리자만 다운로드 가능)
            QuestionDTO question = questionService.getQuestionById(attachment.getPostId());
            if (question == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "관련 질문을 찾을 수 없습니다.");
                return;
            }
            
            boolean isOwner = question.getUserUid() == user.getUserUid();
            boolean isAdmin = "admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority());
            
            if (!isOwner && !isAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "다운로드 권한이 없습니다.");
                return;
            }
            
            // 파일 경로
            String filePath = attachment.getFilePath();
            File downloadFile = new File(filePath);
            
            // 파일 존재 확인
            if (!downloadFile.exists() || !downloadFile.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "서버에 파일이 존재하지 않습니다.");
                return;
            }
            
            // 보안 검사: 업로드 디렉토리 외부에 접근하려는 시도 방지
            if (!downloadFile.getCanonicalPath().startsWith(new File(AppConfig.getUploadPath()).getCanonicalPath())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근이 거부되었습니다.");
                return;
            }
            
            // 다운로드 설정
            String mimeType = request.getServletContext().getMimeType(downloadFile.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + 
                    URLEncoder.encode(attachment.getFileName(), "UTF-8") + "\"");
            response.setContentLength((int)downloadFile.length());
            
            // 파일 전송
            try (FileInputStream fileInputStream = new FileInputStream(downloadFile);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.flush();
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 파일 ID입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}