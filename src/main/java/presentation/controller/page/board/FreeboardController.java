package presentation.controller.page.board;

import java.io.IOException;
import java.util.List;

import business.service.freeboard.FreeboardService;
import dto.board.FreeboardDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import util.web.IpUtil;

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
            String reportCategory = request.getParameter("category");
            
            // 필수 파라미터 검증
            if (reportReason == null || reportReason.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "신고 사유를 입력해주세요.");
                return;
            }
            
            if (reportCategory == null || reportCategory.trim().isEmpty()) {
                reportCategory = "기타"; // 기본값 설정
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
     * 불량 이용자 신고/제재
     */
    private void reportUserById(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "로그인이 필요합니다.");
            return;
        }
        
        try {
            long targetUserId = Long.parseLong(request.getParameter("userId"));
            String reportReason = request.getParameter("reason");
            String reportCategory = request.getParameter("category");
            
            // 필수 파라미터 검증
            if (reportReason == null || reportReason.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "신고 사유를 입력해주세요.");
                return;
            }
            
            if (reportCategory == null || reportCategory.trim().isEmpty()) {
                reportCategory = "기타"; // 기본값 설정
            }
            
            // 관리자/매니저만 제재 가능
            boolean isPenalty = "true".equals(request.getParameter("penalty"));
            String penaltyType = request.getParameter("penaltyType");
            int penaltyDuration = 0;
            
            if (isPenalty) {
                // 관리자/매니저 권한 확인
                if (!("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "제재 권한이 없습니다.");
                    return;
                }
                
                // 제재 기간 파싱
                try {
                    penaltyDuration = Integer.parseInt(request.getParameter("duration"));
                } catch (NumberFormatException e) {
                    penaltyDuration = 7; // 기본 7일
                }
            }
            
            boolean result;
            if (isPenalty) {
                // 제재 처리
                result = freeboardService.penalizeUser(targetUserId, user.getUserUid(), reportReason, 
                        reportCategory, penaltyType, penaltyDuration);
            } else {
                // 일반 신고만 처리
                result = freeboardService.reportUser(targetUserId, user.getUserUid(), reportReason, reportCategory);
            }
            
            if (result) {
                // AJAX 요청인 경우 JSON 응답
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    String message = isPenalty ? "이용자 제재가 처리되었습니다." : "이용자 신고가 접수되었습니다.";
                    response.getWriter().write("{\"success\": true, \"message\": \"" + message + "\"}");
                } else {
                    // 일반 요청인 경우 리다이렉트
                    String message = isPenalty ? "penalized=true" : "reported=true";
                    response.sendRedirect(request.getContextPath() + "/freeboard?action=list&" + message);
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"처리 중 오류가 발생했습니다.\"}");
                } else {
                    request.setAttribute("error", "처리 중 오류가 발생했습니다.");
                    request.getRequestDispatcher("/WEB-INF/views/board/freeboard-list.jsp").forward(request, response);
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 사용자 ID입니다.");
        }
    }
    
    /**
     * 게시글 첨부파일 강제 삭제 (관리자 전용)
     */
    private void deleteFreeboardAttachByFilename(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || !("admin".equals(user.getUserAuthority()) || "armband".equals(user.getUserAuthority()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return;
        }
        
        try {
            long postId = Long.parseLong(request.getParameter("id"));
            String filename = request.getParameter("filename");
            String deleteReason = request.getParameter("reason");
            
            // 필수 파라미터 검증
            if (filename == null || filename.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "파일명을 지정해주세요.");
                return;
            }
            
            boolean result = freeboardService.deleteAttachByFilename(postId, filename, deleteReason, user.getUserUid());
            
            if (result) {
                // AJAX 요청인 경우 JSON 응답
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"message\": \"첨부파일이 삭제되었습니다.\"}");
                } else {
                    // 일반 요청인 경우 리다이렉트
                    response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + postId + "&fileDeleted=true");
                }
            } else {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": false, \"message\": \"첨부파일 삭제 중 오류가 발생했습니다.\"}");
                } else {
                    request.setAttribute("error", "첨부파일 삭제 중 오류가 발생했습니다.");
                    request.getRequestDispatcher("/WEB-INF/views/board/freeboard-view.jsp").forward(request, response);
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
        }
    }
}
