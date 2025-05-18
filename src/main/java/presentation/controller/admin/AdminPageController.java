package presentation.controller.admin;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import business.service.admin.AdminGuideService;
import business.service.admin.AdminKeyboardService;
import business.service.admin.AdminLogService;
import business.service.admin.AdminReportService;
import business.service.admin.AdminUserService;
import dto.admin.AdminDeleteLogDTO;
import dto.admin.AdminReportDTO;
import dto.admin.AdminUserPenaltyDTO;
import dto.keyboard.GuideDTO;
import dto.keyboard.KeyboardCategoryDTO;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardTagDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 관리자 전용 페이지 컨트롤러
 * 게시판의 콘텐츠 이외의 내용을 관리하기 위한 페이지
 */
@WebServlet("/admin/*")
public class AdminPageController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AdminUserService userService;
    private AdminReportService reportService;
    private AdminLogService logService;
    private AdminGuideService guideService;
    private AdminKeyboardService keyboardService;
    
    /**
     * 서비스 객체 초기화
     */
    @Override
    public void init() throws ServletException {
        userService = new AdminUserService();
        reportService = new AdminReportService();
        logService = new AdminLogService();
        guideService = new AdminGuideService();
        keyboardService = new AdminKeyboardService();
    }
    
    /**
     * GET 요청 처리
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 관리자 대시보드 페이지로 이동
            request.getRequestDispatcher("/view/pages/admin/dashboard.jsp").forward(request, response);
            return;
        }
        
        // 경로에 따른 처리
        switch (pathInfo) {
            // 불량 회원 관리
            case "/user/penalty":
                handleGetAllUserPenalty(request, response);
                break;
            case "/user/penalty/search":
                handleGetUserPenaltyByUserId(request, response);
                break;
                
            // 신고 처리
            case "/report":
                handleGetAllReport(request, response);
                break;
            case "/report/search":
                handleGetReportsByCondition(request, response);
                break;
                
            // 게시물 관리
            case "/log/post":
                handleGetAllDeletePostLogs(request, response);
                break;
            case "/log/comment":
                handleGetAllDeleteCommentLogs(request, response);
                break;
            case "/log/post/search":
                handleGetDeletePostLogsByCondition(request, response);
                break;
            case "/log/comment/search":
                handleGetDeleteCommentLogsByCondition(request, response);
                break;
                
            // 키보드 용어 페이지 관리
            case "/guide":
                handleGetGuides(request, response);
                break;
            case "/guide/category":
                handleGetGuideCategories(request, response);
                break;
                
            // 키보드 DB 관리
            case "/keyboard":
                handleGetKeyboardInfos(request, response);
                break;
            case "/keyboard/switch":
                handleGetKeyboardSwitchCategories(request, response);
                break;
            case "/keyboard/layout":
                handleGetKeyboardLayoutCategories(request, response);
                break;
            case "/keyboard/connect":
                handleGetKeyboardConnectCategories(request, response);
                break;
            case "/keyboard/tag":
                handleGetKeyboardTags(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
    
    /**
     * POST 요청 처리
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        // 경로에 따른 처리
        switch (pathInfo) {
            // 불량 회원 관리
            case "/user/penalty/update":
                handleUpdateUserPenaltyStatusByUserId(request, response);
                break;
                
            // 신고 처리
            case "/report/update":
                handleUpdateReportStatus(request, response);
                break;
            case "/report/penalty":
                handleApplyPenalty(request, response);
                break;
                
            // 게시물 관리
            case "/log/post/recover":
                handleRecoverDeletedPost(request, response);
                break;
            case "/log/comment/recover":
                handleRecoverDeletedComment(request, response);
                break;
            case "/log/attach/recover":
                handleRecoverDeletedAttach(request, response);
                break;
                
            // 키보드 용어 페이지 관리
            case "/guide/add":
                handleAddGuide(request, response);
                break;
            case "/guide/update":
                handleUpdateGuide(request, response);
                break;
            case "/guide/delete":
                handleDeleteGuide(request, response);
                break;
            case "/guide/category/add":
                handleAddGuideCategory(request, response);
                break;
            case "/guide/category/update":
                handleUpdateGuideCategory(request, response);
                break;
            case "/guide/category/delete":
                handleDeleteGuideCategory(request, response);
                break;
                
            // 키보드 DB 관리
            case "/keyboard/add":
                handleAddKeyboardInfo(request, response);
                break;
            case "/keyboard/update":
                handleUpdateKeyboardInfo(request, response);
                break;
            case "/keyboard/delete":
                handleDeleteKeyboardInfo(request, response);
                break;
            case "/keyboard/switch/add":
                handleAddKeyboardSwitchCategory(request, response);
                break;
            case "/keyboard/switch/update":
                handleUpdateKeyboardSwitchCategory(request, response);
                break;
            case "/keyboard/switch/delete":
                handleDeleteKeyboardSwitchCategory(request, response);
                break;
            case "/keyboard/layout/add":
                handleAddKeyboardLayoutCategory(request, response);
                break;
            case "/keyboard/layout/update":
                handleUpdateKeyboardLayoutCategory(request, response);
                break;
            case "/keyboard/layout/delete":
                handleDeleteKeyboardLayoutCategory(request, response);
                break;
            case "/keyboard/connect/add":
                handleAddKeyboardConnectCategory(request, response);
                break;
            case "/keyboard/connect/update":
                handleUpdateKeyboardConnectCategory(request, response);
                break;
            case "/keyboard/connect/delete":
                handleDeleteKeyboardConnectCategory(request, response);
                break;
            case "/keyboard/tag/add":
                handleAddKeyboardTag(request, response);
                break;
            case "/keyboard/tag/update":
                handleUpdateKeyboardTag(request, response);
                break;
            case "/keyboard/tag/delete":
                handleDeleteKeyboardTag(request, response);
                break;
            case "/keyboard/tag/confirm":
                handleConfirmKeyboardTag(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
    
    //----------------------------------------
    // 불량 회원 관리 메서드
    //----------------------------------------
    
    /**
     * 불량 이용자 제재 내역 확인
     */
    private void handleGetAllUserPenalty(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<AdminUserPenaltyDTO> penaltyList = userService.getAllUserPenalty();
        request.setAttribute("penaltyList", penaltyList);
        request.getRequestDispatcher("/view/pages/admin/user/penalty.jsp").forward(request, response);
    }
    
    /**
     * 이용자 이름으로 제재 내역 검색
     */
    private void handleGetUserPenaltyByUserId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userIdStr = request.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/user/penalty");
            return;
        }
        
        long userId = Long.parseLong(userIdStr);
        List<AdminUserPenaltyDTO> penaltyList = userService.getUserPenaltyByUserId(userId);
        request.setAttribute("penaltyList", penaltyList);
        request.setAttribute("userId", userId);
        request.getRequestDispatcher("/view/pages/admin/user/penalty.jsp").forward(request, response);
    }
    
    /**
     * 이용자의 패널티 상태 변경 (제재 내용 해제)
     */
    private void handleUpdateUserPenaltyStatusByUserId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String penaltyIdStr = request.getParameter("penaltyId");
        String newStatus = request.getParameter("status");
        
        if (penaltyIdStr == null || newStatus == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long penaltyId = Long.parseLong(penaltyIdStr);
        boolean success = userService.updateUserPenaltyStatusByUserId(penaltyId, newStatus);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/user/penalty");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "패널티 상태 변경에 실패했습니다.");
        }
    }
    
    //----------------------------------------
    // 신고 처리 메서드
    //----------------------------------------
    
    /**
     * 신고 내역 확인
     */
    private void handleGetAllReport(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<AdminReportDTO> reportList = reportService.getAllReport();
        request.setAttribute("reportList", reportList);
        request.getRequestDispatcher("/view/pages/admin/report/list.jsp").forward(request, response);
    }
    
    /**
     * 신고 내역 검색
     */
    private void handleGetReportsByCondition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String status = request.getParameter("status");
        String targetType = request.getParameter("targetType");
        
        List<AdminReportDTO> reportList = reportService.getReportsByCondition(status, targetType);
        request.setAttribute("reportList", reportList);
        request.setAttribute("status", status);
        request.setAttribute("targetType", targetType);
        request.getRequestDispatcher("/view/pages/admin/report/list.jsp").forward(request, response);
    }
    
    /**
     * 신고 상태 업데이트
     */
    private void handleUpdateReportStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reportIdStr = request.getParameter("reportId");
        String status = request.getParameter("status");
        
        if (reportIdStr == null || status == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long reportId = Long.parseLong(reportIdStr);
        boolean success = reportService.updateReportStatus(reportId, status);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/report");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "신고 상태 변경에 실패했습니다.");
        }
    }
    
    /**
     * 불량 이용자 제재 처리
     */
    private void handleApplyPenalty(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reportIdStr = request.getParameter("reportId");
        String userIdStr = request.getParameter("userId");
        String reason = request.getParameter("reason");
        String duration = request.getParameter("duration");
        String adminIdStr = request.getParameter("adminId");
        
        if (reportIdStr == null || userIdStr == null || reason == null || duration == null || adminIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long reportId = Long.parseLong(reportIdStr);
        long userId = Long.parseLong(userIdStr);
        long adminId = Long.parseLong(adminIdStr);
        
        // 현재 날짜 설정
        java.util.Date today = new java.util.Date();
        Date startDate = new Date(today.getTime());
        
        // 종료 날짜 계산 (duration에 따라)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(today);
        
        switch(duration) {
            case "1일":
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                break;
            case "3일":
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 3);
                break;
            case "7일":
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 7);
                break;
            case "30일":
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 30);
                break;
            case "영구":
                calendar.add(java.util.Calendar.YEAR, 100); // 사실상 영구적
                break;
        }
        
        Date endDate = new Date(calendar.getTimeInMillis());
        
        // 패널티 객체 생성
        AdminUserPenaltyDTO penalty = new AdminUserPenaltyDTO();
        penalty.setPenaltyReason(reason);
        penalty.setPenaltyStartDate(startDate);
        penalty.setPenaltyEndDate(endDate);
        penalty.setPenaltyStatus("활성");
        penalty.setPenaltyDuration(duration);
        penalty.setUserUid(userId);
        penalty.setAdminUid(adminId);
        
        // 패널티 적용 및 신고 상태 변경
        boolean success = reportService.applyPenaltyToUser(penalty, reportId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/report");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "패널티 적용에 실패했습니다.");
        }
    }
    
    //----------------------------------------
    // 게시물 관리 메서드
    //----------------------------------------
    
    /**
     * 전체 게시글 삭제 내역 조회
     */
    private void handleGetAllDeletePostLogs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<AdminDeleteLogDTO> logList = logService.getAllDeletePostLogs();
        request.setAttribute("logList", logList);
        request.getRequestDispatcher("/view/pages/admin/log/post.jsp").forward(request, response);
    }
    
    /**
     * 전체 댓글 삭제 내역 조회
     */
    private void handleGetAllDeleteCommentLogs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<AdminDeleteLogDTO> logList = logService.getAllDeleteCommentLogs();
        request.setAttribute("logList", logList);
        request.getRequestDispatcher("/view/pages/admin/log/comment.jsp").forward(request, response);
    }
    
    /**
     * 삭제 게시글 내역 검색
     */
    private void handleGetDeletePostLogsByCondition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String boardType = request.getParameter("boardType");
        String keyword = request.getParameter("keyword");
        
        List<AdminDeleteLogDTO> logList = logService.getDeletePostLogsByCondition(boardType, keyword);
        request.setAttribute("logList", logList);
        request.setAttribute("boardType", boardType);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/view/pages/admin/log/post.jsp").forward(request, response);
    }
    
    /**
     * 삭제 댓글 내역 검색
     */
    private void handleGetDeleteCommentLogsByCondition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String boardType = request.getParameter("boardType");
        String keyword = request.getParameter("keyword");
        
        List<AdminDeleteLogDTO> logList = logService.getDeleteCommentLogsByCondition(boardType, keyword);
        request.setAttribute("logList", logList);
        request.setAttribute("boardType", boardType);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/view/pages/admin/log/comment.jsp").forward(request, response);
    }
    
    /**
     * 삭제 게시글 복원
     */
    private void handleRecoverDeletedPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String boardType = request.getParameter("boardType");
        String postIdStr = request.getParameter("postId");
        
        if (boardType == null || postIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long postId = Long.parseLong(postIdStr);
        boolean success = logService.recoverDeletedPost(boardType, postId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/log/post");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게시글 복원에 실패했습니다.");
        }
    }
    
    /**
     * 삭제 댓글 복원
     */
    private void handleRecoverDeletedComment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String boardType = request.getParameter("boardType");
        String commentIdStr = request.getParameter("commentId");
        
        if (boardType == null || commentIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long commentId = Long.parseLong(commentIdStr);
        boolean success = logService.recoverDeletedComment(boardType, commentId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/log/comment");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "댓글 복원에 실패했습니다.");
        }
    }
    
    /**
     * 삭제 첨부파일 복원
     */
    private void handleRecoverDeletedAttach(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String boardType = request.getParameter("boardType");
        String attachIdStr = request.getParameter("attachId");
        
        if (boardType == null || attachIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long attachId = Long.parseLong(attachIdStr);
        boolean success = logService.recoverDeletedAttach(boardType, attachId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/log/post");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "첨부파일 복원에 실패했습니다.");
        }
    }
    
    //----------------------------------------
    // 키보드 용어 페이지 관리 메서드
    //----------------------------------------
    
    /**
     * 키보드 용어 목록 조회
     */
    private void handleGetGuides(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<GuideDTO> guideList = guideService.getAllGuides();
        request.setAttribute("guideList", guideList);
        request.getRequestDispatcher("/view/pages/admin/guide/list.jsp").forward(request, response);
    }
    
    /**
     * 키보드 용어 카테고리 목록 조회
     */
    private void handleGetGuideCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> categoryList = guideService.getAllGuideCategories();
        request.setAttribute("categoryList", categoryList);
        request.getRequestDispatcher("/view/pages/admin/guide/category.jsp").forward(request, response);
    }
    
    /**
     * 키보드 용어 등록
     */
    private void handleAddGuide(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String term = request.getParameter("term");
        String description = request.getParameter("description");
        String url = request.getParameter("url");
        String category = request.getParameter("category");
        
        if (term == null || description == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        GuideDTO guide = new GuideDTO();
        guide.setTerm(term);
        guide.setDescription(description);
        guide.setUrl(url);
        guide.setCategory(category);
        
        boolean success = guideService.addGuide(guide);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/guide");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "용어 등록에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 용어 수정
     */
    private void handleUpdateGuide(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String guideIdStr = request.getParameter("guideId");
        String term = request.getParameter("term");
        String description = request.getParameter("description");
        String url = request.getParameter("url");
        String category = request.getParameter("category");
        
        if (guideIdStr == null || term == null || description == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long guideId = Long.parseLong(guideIdStr);
        
        GuideDTO guide = new GuideDTO();
        guide.setId(guideId);
        guide.setTerm(term);
        guide.setDescription(description);
        guide.setUrl(url);
        guide.setCategory(category);
        
        boolean success = guideService.updateGuide(guide);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/guide");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "용어 수정에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 용어 삭제
     */
    private void handleDeleteGuide(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String guideIdStr = request.getParameter("guideId");
        
        if (guideIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long guideId = Long.parseLong(guideIdStr);
        boolean success = guideService.deleteGuide(guideId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/guide");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "용어 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 용어 카테고리 입력
     */
    private void handleAddGuideCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter("categoryName");
        
        if (categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        boolean success = guideService.addGuideCategory(categoryName);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/guide/category");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 등록에 실패했습니다.");
        }
    }
    
    /**
     * 용어 카테고리 수정
     */
    private void handleUpdateGuideCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String oldCategoryName = request.getParameter("oldCategoryName");
        String newCategoryName = request.getParameter("newCategoryName");
        
        if (oldCategoryName == null || newCategoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        boolean success = guideService.updateGuideCategory(oldCategoryName, newCategoryName);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/guide/category");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 수정에 실패했습니다.");
        }
    }
    
    /**
     * 용어 카테고리 삭제
     */
    private void handleDeleteGuideCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter("categoryName");
        
        if (categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        boolean success = guideService.deleteGuideCategory(categoryName);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/guide/category");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 삭제에 실패했습니다.");
        }
    }
    
    //----------------------------------------
    // 키보드 DB 관리 메서드
    //----------------------------------------
    
    /**
     * 키보드 정보 목록 조회
     */
    private void handleGetKeyboardInfos(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<KeyboardInfoDTO> keyboardList = keyboardService.getAllKeyboardInfos();
        request.setAttribute("keyboardList", keyboardList);
        request.getRequestDispatcher("/view/pages/admin/keyboard/list.jsp").forward(request, response);
    }
    
    /**
     * 키보드 축 카테고리 목록 조회
     */
    private void handleGetKeyboardSwitchCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<KeyboardCategoryDTO> categoryList = keyboardService.getAllSwitchCategories();
        request.setAttribute("categoryList", categoryList);
        request.getRequestDispatcher("/view/pages/admin/keyboard/switch.jsp").forward(request, response);
    }
    
    /**
     * 키보드 배열 카테고리 목록 조회
     */
    private void handleGetKeyboardLayoutCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<KeyboardCategoryDTO> categoryList = keyboardService.getAllLayoutCategories();
        request.setAttribute("categoryList", categoryList);
        request.getRequestDispatcher("/view/pages/admin/keyboard/layout.jsp").forward(request, response);
    }
    
    /**
     * 키보드 연결방식 카테고리 목록 조회
     */
    private void handleGetKeyboardConnectCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<KeyboardCategoryDTO> categoryList = keyboardService.getAllConnectCategories();
        request.setAttribute("categoryList", categoryList);
        request.getRequestDispatcher("/view/pages/admin/keyboard/connect.jsp").forward(request, response);
    }
    
    /**
     * 키보드 태그 목록 조회
     */
    private void handleGetKeyboardTags(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<KeyboardTagDTO> tagList = keyboardService.getAllKeyboardTags();
        request.setAttribute("tagList", tagList);
        request.getRequestDispatcher("/view/pages/admin/keyboard/tag.jsp").forward(request, response);
    }
    
    /**
     * 키보드 정보 등록
     */
    private void handleAddKeyboardInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 요청에서 필요한 파라미터 추출
        KeyboardInfoDTO keyboard = extractKeyboardInfoFromRequest(request);
        
        boolean success = keyboardService.addKeyboardInfo(keyboard);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "키보드 정보 등록에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 정보 수정
     */
    private void handleUpdateKeyboardInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 요청에서 필요한 파라미터 추출
        KeyboardInfoDTO keyboard = extractKeyboardInfoFromRequest(request);
        String keyboardIdStr = request.getParameter("keyboardId");
        
        if (keyboardIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        keyboard.setId(Long.parseLong(keyboardIdStr));
        boolean success = keyboardService.updateKeyboardInfo(keyboard);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "키보드 정보 수정에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 정보 삭제
     */
    private void handleDeleteKeyboardInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyboardIdStr = request.getParameter("keyboardId");
        
        if (keyboardIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long keyboardId = Long.parseLong(keyboardIdStr);
        boolean success = keyboardService.deleteKeyboardInfo(keyboardId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "키보드 정보 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 축 카테고리 입력
     */
    private void handleAddKeyboardSwitchCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter("categoryName");
        
        if (categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardCategoryDTO category = new KeyboardCategoryDTO();
        category.setKeyboardCategoryName(categoryName);
        category.setType("switch");
        
        boolean success = keyboardService.addKeyboardCategory(category);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/switch");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 등록에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 축 카테고리 수정
     */
    private void handleUpdateKeyboardSwitchCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryIdStr = request.getParameter("categoryId");
        String categoryName = request.getParameter("categoryName");
        
        if (categoryIdStr == null || categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardCategoryDTO category = new KeyboardCategoryDTO();
        category.setKeyboardCategoryUid(Long.parseLong(categoryIdStr));
        category.setKeyboardCategoryName(categoryName);
        category.setType("switch");
        
        boolean success = keyboardService.updateKeyboardCategory(category);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/switch");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 수정에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 축 카테고리 삭제
     */
    private void handleDeleteKeyboardSwitchCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryIdStr = request.getParameter("categoryId");
        
        if (categoryIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long categoryId = Long.parseLong(categoryIdStr);
        boolean success = keyboardService.deleteKeyboardCategory(categoryId, "switch");
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/switch");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 배열 카테고리 입력
     */
    private void handleAddKeyboardLayoutCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter("categoryName");
        
        if (categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardCategoryDTO category = new KeyboardCategoryDTO();
        category.setKeyboardCategoryName(categoryName);
        category.setType("layout");
        
        boolean success = keyboardService.addKeyboardCategory(category);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/layout");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 등록에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 배열 카테고리 수정
     */
    private void handleUpdateKeyboardLayoutCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryIdStr = request.getParameter("categoryId");
        String categoryName = request.getParameter("categoryName");
        
        if (categoryIdStr == null || categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardCategoryDTO category = new KeyboardCategoryDTO();
        category.setKeyboardCategoryUid(Long.parseLong(categoryIdStr));
        category.setKeyboardCategoryName(categoryName);
        category.setType("layout");
        
        boolean success = keyboardService.updateKeyboardCategory(category);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/layout");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 수정에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 배열 카테고리 삭제
     */
    private void handleDeleteKeyboardLayoutCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryIdStr = request.getParameter("categoryId");
        
        if (categoryIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long categoryId = Long.parseLong(categoryIdStr);
        boolean success = keyboardService.deleteKeyboardCategory(categoryId, "layout");
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/layout");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 연결방식 카테고리 입력
     */
    private void handleAddKeyboardConnectCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryName = request.getParameter("categoryName");
        
        if (categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardCategoryDTO category = new KeyboardCategoryDTO();
        category.setKeyboardCategoryName(categoryName);
        category.setType("connect");
        
        boolean success = keyboardService.addKeyboardCategory(category);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/connect");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 등록에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 연결방식 카테고리 수정
     */
    private void handleUpdateKeyboardConnectCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryIdStr = request.getParameter("categoryId");
        String categoryName = request.getParameter("categoryName");
        
        if (categoryIdStr == null || categoryName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long categoryId = Long.parseLong(categoryIdStr);
        String description = request.getParameter("description"); // 선택적 파라미터
        boolean success = keyboardService.updateKeyboardCategory(categoryId, categoryName, description, "connect");
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/connect");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 수정에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 연결방식 카테고리 삭제
     */
    private void handleDeleteKeyboardConnectCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryIdStr = request.getParameter("categoryId");
        
        if (categoryIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long categoryId = Long.parseLong(categoryIdStr);
        boolean success = keyboardService.deleteKeyboardCategory(categoryId, "connect");
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/connect");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카테고리 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 태그 입력
     */
    private void handleAddKeyboardTag(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tagName = request.getParameter("tagName");
        
        if (tagName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardTagDTO tag = new KeyboardTagDTO();
        tag.setName(tagName);
        tag.setStatus("대기");
        
        boolean success = keyboardService.addKeyboardTag(tag);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/tag");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "태그 등록에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 태그 수정
     */
    private void handleUpdateKeyboardTag(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tagIdStr = request.getParameter("tagId");
        String tagName = request.getParameter("tagName");
        
        if (tagIdStr == null || tagName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        KeyboardTagDTO tag = new KeyboardTagDTO();
        tag.setId(Long.parseLong(tagIdStr));
        tag.setName(tagName);
        
        boolean success = keyboardService.updateKeyboardTag(tag);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/tag");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "태그 수정에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 태그 삭제(비활성화)
     */
    private void handleDeleteKeyboardTag(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tagIdStr = request.getParameter("tagId");
        
        if (tagIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long tagId = Long.parseLong(tagIdStr);
        boolean success = keyboardService.deleteKeyboardTag(tagId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/tag");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "태그 삭제에 실패했습니다.");
        }
    }
    
    /**
     * 키보드 태그 승인
     */
    private void handleConfirmKeyboardTag(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tagIdStr = request.getParameter("tagId");
        
        if (tagIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "필수 파라미터가 누락되었습니다.");
            return;
        }
        
        long tagId = Long.parseLong(tagIdStr);
        boolean success = keyboardService.confirmKeyboardTag(tagId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/keyboard/tag");
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "태그 승인에 실패했습니다.");
        }
    }
    
    /**
     * 요청에서 키보드 정보를 추출하는 헬퍼 메서드
     */
    private KeyboardInfoDTO extractKeyboardInfoFromRequest(HttpServletRequest request) {
        KeyboardInfoDTO keyboard = new KeyboardInfoDTO();
        
        keyboard.setName(request.getParameter("name"));
        keyboard.setManufacturer(request.getParameter("manufacturer"));
        keyboard.setType(request.getParameter("type"));
        keyboard.setSwitchType(request.getParameter("switchType"));
        keyboard.setLayout(request.getParameter("layout"));
        keyboard.setPrice(Integer.parseInt(request.getParameter("price")));
        keyboard.setReleaseDate(Date.valueOf(request.getParameter("releaseDate")));
        keyboard.setDescription(request.getParameter("description"));
        keyboard.setImageUrl(request.getParameter("imageUrl"));
        keyboard.setConnectionType(request.getParameter("connectionType"));
        
        // 태그 ID 목록 처리
        String[] tagIds = request.getParameterValues("tagIds");
        if (tagIds != null) {
            List<Long> tagIdList = new ArrayList<>();
            for (String id : tagIds) {
                tagIdList.add(Long.parseLong(id));
            }
            // 실제로는 TagID의 리스트를 Tag 이름 리스트로 변환해야 합니다.
            // 임시 처리: Long을 String으로 변환
            List<String> tagNames = new ArrayList<>();
            for (Long id : tagIdList) {
                tagNames.add(id.toString());
            }
            keyboard.setTags(tagNames);
        }
        
        return keyboard;
    }
}
