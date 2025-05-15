package presentation.controller.page.board;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import business.service.freeboard.FreeboardService;
import dto.board.AttachmentDTO;
import dto.board.FreeboardDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import presentation.controller.page.Controller;
import util.web.IpUtil;
import util.file.FileUtil;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 10,     // 10 MB
    maxRequestSize = 1024 * 1024 * 50   // 50 MB
)
public class FreeboardController implements Controller {
    private final FreeboardService freeboardService;
    
    public FreeboardController() {
        this.freeboardService = new FreeboardService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        
        request.setAttribute("freeboardList", freeboardList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalCount", totalCount);
        
        request.getRequestDispatcher("/WEB-INF/views/board/freeboard-list.jsp").forward(request, response);
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
            
            request.setAttribute("freeboard", freeboard);
            request.getRequestDispatcher("/WEB-INF/views/board/freeboard-view.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
        }
    }
    
    /**
     * 게시글 등록
     */
    private void postFreeboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard&action=write");
            return;
        }
        
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String clientIp = IpUtil.getClientIpAddr(request);
        
        FreeboardDTO freeboard = new FreeboardDTO(title, content, clientIp, user.getUserUid());
        
        boolean result = freeboardService.createFreeboard(freeboard);
        
        if (result) {
            response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + freeboard.getFreeboardUid());
        } else {
            request.setAttribute("error", "게시글 등록에 실패했습니다.");
            request.setAttribute("freeboard", freeboard);
            request.getRequestDispatcher("/WEB-INF/views/board/freeboard-write.jsp").forward(request, response);
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
                response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard&action=edit");
                return;
            }
            
            long postId = Long.parseLong(request.getParameter("id"));
            FreeboardDTO freeboard = freeboardService.getFreeboardById(postId);
            
            if (freeboard == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "게시글을 찾을 수 없습니다.");
                return;
            }
            
            // 작성자 본인 또는 관리자만 수정 가능
            if (freeboard.getUserUid() != user.getUserUid() && 
                    !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                return;
            }
            
            request.setAttribute("freeboard", freeboard);
            request.getRequestDispatcher("/WEB-INF/views/board/freeboard-edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
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
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard&action=edit");
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
            
            if (result) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + postId);
            } else {
                request.setAttribute("error", "게시글 수정에 실패했습니다.");
                request.setAttribute("freeboard", freeboard);
                request.getRequestDispatcher("/WEB-INF/views/board/freeboard-edit.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            
            boolean result = freeboardService.deleteFreeboard(postId, user.getUserUid(), user.getUserAuthority());
            
            if (result) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=list");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "게시글 삭제에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            boolean isNotice = Boolean.parseBoolean(request.getParameter("isNotice"));
            
            boolean result = freeboardService.setNotice(postId, isNotice, user.getUserAuthority());
            
            if (result) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + postId);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "공지사항 설정/해제에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            String hideReason = request.getParameter("reason");
            
            boolean result = freeboardService.hideFreeboard(postId, hideReason, user.getUserAuthority());
            
            if (result) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=list");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게시글 숨김 처리에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            String reportReason = request.getParameter("reason");
            String reportCategory = validateReportCategory(request.getParameter("category"));
            
            // 필수 파라미터 검증
            if (reportReason == null || reportReason.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "신고 사유를 입력해주세요.");
                return;
            }
            
            boolean result = freeboardService.reportFreeboard(postId, user.getUserUid(), reportReason, reportCategory);
            
            if (result) {
                // AJAX 요청인 경우 JSON 응답
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"message\": \"신고가 접수되었습니다.\"}");
                } else {
                    // 일반 요청인 경우 리다이렉트
                    response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + postId + "&reported=true");
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"신고 처리 중 오류가 발생했습니다.\"}");
                } else {
                    request.setAttribute("error", "신고 처리 중 오류가 발생했습니다.");
                    request.getRequestDispatcher("/WEB-INF/views/board/freeboard-view.jsp").forward(request, response);
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
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
            long postId = Long.parseLong(request.getParameter("postId"));
            
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
            
            // 3. 전체 파일 경로 생성
            String filePath = new File(uploadDirPath, uniqueFileName).getAbsolutePath();
            
            // 4. 파일 저장
            filePart.write(filePath);
            
            // 5. DB에 파일 정보 저장 (상대 경로 저장)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String relativePath = sdf.format(new Date()) + "/" + uniqueFileName;
            
            boolean success = freeboardService.addAttachment(postId, fileName, relativePath, fileSize);
            
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
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 첨부파일 다운로드
     */
    public void downloadAttachment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            long attachId = Long.parseLong(request.getParameter("attachId"));
            
            // 첨부파일 정보 조회
            AttachmentDTO attachment = freeboardService.getAttachmentById(attachId);
            if (attachment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
                return;
            }
            
            // 상대 경로를 절대 경로로 변환
            String relativePath = attachment.getFilePath();
            String absolutePath = new File(AppConfig.getUploadPath(), relativePath).getAbsolutePath();
            
            // 파일 객체 생성
            File downloadFile = new File(absolutePath);
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
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
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
