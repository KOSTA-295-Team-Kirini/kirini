package presentation.controller.page;

import java.io.IOException;
import java.util.List;

import business.service.admin.AdminGlossaryService;
import business.service.admin.AdminKeyboardService;
import business.service.admin.AdminLogService;
import business.service.admin.AdminPenaltyService;
import business.service.admin.AdminReportService;
import dto.board.ReportDTO;
import dto.keyboard.GuideDTO;
import dto.user.UserPenaltyDTO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AdminPageController implements Controller {
    private AdminPenaltyService penaltyService;
    private AdminReportService reportService;
    private AdminKeyboardService keyboardService;
    private AdminLogService logService;
    private AdminGlossaryService glossaryService;

    public AdminPageController() {
        penaltyService = new AdminPenaltyService();
        reportService = new AdminReportService();
        keyboardService = new AdminKeyboardService();
        logService = new AdminLogService();
        glossaryService = new AdminGlossaryService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // 관리자 인증 확인
        HttpSession session = request.getSession();
        if (session.getAttribute("userAuthority") == null || 
            !session.getAttribute("userAuthority").equals("admin")) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=admin");
            return;
        }
        
        if (action == null) {
            showAdminDashboard(request, response);
            return;
        }
        
        switch (action) {
            // 불량 회원 관리
            case "getAllUserPenalty":
                getAllUserPenalty(request, response);
                break;
            case "getUserPenaltyByUserId":
                getUserPenaltyByUserId(request, response);
                break;
                
            // 신고 처리
            case "getAllReport":
                getAllReport(request, response);
                break;
            case "getReportsByCondition":
                getReportsByCondition(request, response);
                break;
                
            // 게시물 관리
            case "getAllDeletePostLogs":
                getAllDeletePostLogs(request, response);
                break;
            case "getAllDeleteCommentLogs":
                getAllDeleteCommentLogs(request, response);
                break;
            case "getDeletePostLogsByCondition":
                getDeletePostLogsByCondition(request, response);
                break;
            case "getDeleteCommentLogsByCondition":
                getDeleteCommentLogsByCondition(request, response);
                break;
                
            // 키보드 용어 관리
            case "manageGuides":
                manageGuides(request, response);
                break;
                
            // 키보드 DB 관리
            case "manageKeyboardInfo":
                manageKeyboardInfo(request, response);
                break;
            case "manageKeyboardCategories":
                manageKeyboardCategories(request, response);
                break;
            case "manageKeyboardTags":
                manageKeyboardTags(request, response);
                break;
                
            default:
                showAdminDashboard(request, response);
                break;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // 관리자 인증 확인
        HttpSession session = request.getSession();
        if (session.getAttribute("userAuthority") == null || 
            !session.getAttribute("userAuthority").equals("admin")) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=admin");
            return;
        }
        
        switch (action) {
            // 불량 회원 관리
            case "updateUserPenaltyStatus":
                updateUserPenaltyStatus(request, response);
                break;
                
            // 게시물 관리
            case "recoverDeletedPost":
                recoverDeletedPost(request, response);
                break;
            case "recoverDeletedComment":
                recoverDeletedComment(request, response);
                break;
                
            // 키보드 용어 관리
            case "addGuide":
                addGuide(request, response);
                break;
            case "updateGuide":
                updateGuide(request, response);
                break;
            case "deleteGuide":
                deleteGuide(request, response);
                break;
            case "addGuideCategory":
                addGuideCategory(request, response);
                break;
            case "updateGuideCategory":
                updateGuideCategory(request, response);
                break;
            case "deleteGuideCategory":
                deleteGuideCategory(request, response);
                break;
                
            // 키보드 DB 관리
            case "addKeyboardInfo":
                addKeyboardInfo(request, response);
                break;
            case "updateKeyboardInfo":
                updateKeyboardInfo(request, response);
                break;
            case "deleteKeyboardInfo":
                deleteKeyboardInfo(request, response);
                break;
            case "addKeyboardCategory":
                addKeyboardCategory(request, response);
                break;
            case "updateKeyboardCategory":
                updateKeyboardCategory(request, response);
                break;
            case "deleteKeyboardCategory":
                deleteKeyboardCategory(request, response);
                break;
            case "addKeyboardTag":
                addKeyboardTag(request, response);
                break;
            case "updateKeyboardTag":
                updateKeyboardTag(request, response);
                break;
            case "deleteKeyboardTag":
                deleteKeyboardTag(request, response);
                break;
            case "confirmKeyboardTag":
                confirmKeyboardTag(request, response);
                break;
                
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                break;
        }
    }
    
    // 관리자 대시보드 표시
    private void showAdminDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/dashboard.jsp");
        dispatcher.forward(request, response);
    }
    
    // === 불량 회원 관리 ===
    
    private void getAllUserPenalty(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<UserPenaltyDTO> penalties = penaltyService.getAllUserPenalties();
        request.setAttribute("penalties", penalties);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/user/penalties.jsp");
        dispatcher.forward(request, response);
    }
    
    private void getUserPenaltyByUserId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long userId = Long.parseLong(request.getParameter("userId"));
        List<UserPenaltyDTO> penalties = penaltyService.getUserPenaltiesByUserId(userId);
        
        request.setAttribute("penalties", penalties);
        request.setAttribute("userId", userId);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/user/user_penalties.jsp");
        dispatcher.forward(request, response);
    }
    
    private void updateUserPenaltyStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long penaltyId = Long.parseLong(request.getParameter("penaltyId"));
        String status = request.getParameter("status");
        
        boolean success = penaltyService.updateUserPenaltyStatus(penaltyId, status);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin?action=getAllUserPenalty&result=success");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin?action=getAllUserPenalty&result=fail");
        }
    }
    
    // === 신고 관리 ===
    
    private void getAllReport(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<ReportDTO> reports = reportService.getAllReports();
        request.setAttribute("reports", reports);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/report/reports.jsp");
        dispatcher.forward(request, response);
    }
    
    private void getReportsByCondition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String status = request.getParameter("status");
        String targetType = request.getParameter("targetType");
        
        List<ReportDTO> reports = reportService.getReportsByCondition(status, targetType);
        
        request.setAttribute("reports", reports);
        request.setAttribute("status", status);
        request.setAttribute("targetType", targetType);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/report/filtered_reports.jsp");
        dispatcher.forward(request, response);
    }
    
    // === 게시물 관리 ===
    
    private void getAllDeletePostLogs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<LogDeletePostDTO> logs = logService.getAllDeletePostLogs();
        request.setAttribute("logs", logs);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/log/deleted_posts.jsp");
        dispatcher.forward(request, response);
    }
    
    private void getAllDeleteCommentLogs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<LogDeleteCommentDTO> logs = logService.getAllDeleteCommentLogs();
        request.setAttribute("logs", logs);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/log/deleted_comments.jsp");
        dispatcher.forward(request, response);
    }
    
    private void getDeletePostLogsByCondition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String boardType = request.getParameter("boardType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        
        List<LogDeletePostDTO> logs = logService.getDeletePostLogsByCondition(boardType, startDate, endDate);
        
        request.setAttribute("logs", logs);
        request.setAttribute("boardType", boardType);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/log/filtered_deleted_posts.jsp");
        dispatcher.forward(request, response);
    }
    
    private void getDeleteCommentLogsByCondition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 구현...
    }
    
    private void recoverDeletedPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long logId = Long.parseLong(request.getParameter("logId"));
        long postId = Long.parseLong(request.getParameter("postId"));
        String boardType = request.getParameter("boardType");
        
        boolean success = logService.recoverDeletedPost(logId, postId, boardType);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin?action=getAllDeletePostLogs&result=success");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin?action=getAllDeletePostLogs&result=fail");
        }
    }
    
    private void recoverDeletedComment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 구현...
    }
    
    // === 키보드 용어 관리 ===
    
    private void manageGuides(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<GuideDTO> guides = glossaryService.getAllGuides();
        request.setAttribute("guides", guides);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/pages/admin/glossary/guides.jsp");
        dispatcher.forward(request, response);
    }
    
    private void addGuide(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String term = request.getParameter("term");
        String description = request.getParameter("description");
        String category = request.getParameter("category");
        
        GuideDTO guide = new GuideDTO();
        guide.setTerm(term);
        guide.setDescription(description);
        guide.setCategory(category);
        
        boolean success = glossaryService.addGuide(guide);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin?action=manageGuides&result=success");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin?action=manageGuides&result=fail");
        }
    }
    
    private void updateGuide(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 구현...
    }
    
    private void deleteGuide(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 구현...
    }
    
    // 추가 메소드들...
}
