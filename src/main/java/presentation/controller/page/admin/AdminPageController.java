package presentation.controller.page.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.sql.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import business.service.admin.AdminGlossaryService;
import business.service.admin.AdminKeyboardService;
import business.service.admin.AdminLogService;
import business.service.admin.AdminPenaltyService;
import business.service.admin.AdminReportService;
import dto.admin.AdminUserPenaltyDTO;
import dto.admin.AdminReportDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;

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
        // 추가 보안 검사
        if (!checkAdminAuth(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            // 관리자 메인 페이지 표시
            request.getRequestDispatcher("/view/pages/admin/dashboard.jsp").forward(request, response);
            return;
        }

        // action 파라미터에 따라 적절한 메소드 호출
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JsonObject result = new JsonObject();

            switch (action) {
                // 불량 회원 관리
                case "getAllUserPenalty":
                    result = getAllUserPenalty(request);
                    break;
                case "getUserPenaltyByUserId":
                    result = getUserPenaltyByUserId(request);
                    break;
                case "updateUserPenaltyStatus":
                    result = updateUserPenaltyStatusByUserId(request);
                    break;

                // 신고 처리
                case "getAllReport":
                    result = getAllReport(request);
                    break;
                case "getReportsByCondition":
                    result = getReportsByCondition(request);
                    break;
                case "processReport":
                    result = processReport(request);
                    break;

                // 게시물 관리
                case "getAllDeletePostLogs":
                    result = getAllDeletePostLogs(request);
                    break;
                case "getAllDeleteCommentLogs":
                    result = getAllDeleteCommentLogs(request);
                    break;
                case "recoverDeletedPost":
                    result = recoverDeletedPost(request);
                    break;
                case "recoverDeletedComment":
                    result = recoverDeletedComment(request);
                    break;

                // 키보드 용어 관리
                case "addGuide":
                    result = addGuide(request);
                    break;
                case "updateGuide":
                    result = updateGuide(request);
                    break;
                case "deleteGuide":
                    result = deleteGuide(request);
                    break;

                // 키보드 DB 관리
                case "addKeyboardInfo":
                    result = addKeyboardInfo(request);
                    break;
                case "updateKeyboardInfo":
                    result = updateKeyboardInfo(request);
                    break;
                case "deleteKeyboardInfo":
                    result = deleteKeyboardInfo(request);
                    break;

                default:
                    result.addProperty("success", false);
                    result.addProperty("message", "지원하지 않는 작업입니다.");
            }

            out.print(result.toString());

        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "서버 오류가 발생했습니다: " + e.getMessage());
            out.print(error.toString());
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    // 추가 보안 검사 메소드
    private boolean checkAdminAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return false;
        }

        // 관리자 권한 확인
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            response.sendRedirect(request.getContextPath() + "/view/pages/403.html");
            return false;
        }

        return true;
    }

    /* ---------- 불량 회원 관리 ---------- */

    private JsonObject getAllUserPenalty(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            List<AdminUserPenaltyDTO> penalties = penaltyService.getAllUserPenalties();
            
            JsonArray penaltyArray = new JsonArray();
            for (AdminUserPenaltyDTO penalty : penalties) {
                JsonObject penaltyJson = new JsonObject();
                penaltyJson.addProperty("penaltyUid", penalty.getPenaltyUid());
                penaltyJson.addProperty("userUid", penalty.getUserUid());
                penaltyJson.addProperty("username", penalty.getUsername());
                penaltyJson.addProperty("reason", penalty.getPenaltyReason());
                penaltyJson.addProperty("startDate", penalty.getPenaltyStartDate().toString());
                
                if (penalty.getPenaltyEndDate() != null) {
                    penaltyJson.addProperty("endDate", penalty.getPenaltyEndDate().toString());
                } else {
                    penaltyJson.addProperty("endDate", "");
                }
                
                penaltyJson.addProperty("status", penalty.getPenaltyStatus());
                penaltyJson.addProperty("duration", penalty.getPenaltyDuration());
                penaltyArray.add(penaltyJson);
            }
            
            result.addProperty("success", true);
            result.add("penalties", penaltyArray);
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "패널티 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    private JsonObject getUserPenaltyByUserId(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            String userUidStr = request.getParameter("userUid");
            if (userUidStr == null || userUidStr.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "사용자 ID가 제공되지 않았습니다.");
                return result;
            }
            
            long userUid;
            try {
                userUid = Long.parseLong(userUidStr);
            } catch (NumberFormatException e) {
                result.addProperty("success", false);
                result.addProperty("message", "유효하지 않은 사용자 ID 형식입니다.");
                return result;
            }
            
            List<AdminUserPenaltyDTO> penalties = penaltyService.getUserPenaltiesByUserId(userUid);
            
            JsonArray penaltyArray = new JsonArray();
            for (AdminUserPenaltyDTO penalty : penalties) {
                JsonObject penaltyJson = new JsonObject();
                penaltyJson.addProperty("penaltyUid", penalty.getPenaltyUid());
                penaltyJson.addProperty("userUid", penalty.getUserUid());
                penaltyJson.addProperty("username", penalty.getUsername());
                penaltyJson.addProperty("reason", penalty.getPenaltyReason());
                penaltyJson.addProperty("startDate", penalty.getPenaltyStartDate().toString());
                
                if (penalty.getPenaltyEndDate() != null) {
                    penaltyJson.addProperty("endDate", penalty.getPenaltyEndDate().toString());
                } else {
                    penaltyJson.addProperty("endDate", "");
                }
                
                penaltyJson.addProperty("status", penalty.getPenaltyStatus());
                penaltyJson.addProperty("duration", penalty.getPenaltyDuration());
                penaltyArray.add(penaltyJson);
            }
            
            result.addProperty("success", true);
            result.add("penalties", penaltyArray);
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "패널티 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    private JsonObject updateUserPenaltyStatusByUserId(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            String penaltyUidStr = request.getParameter("penaltyUid");
            String penaltyStatus = request.getParameter("status");
            
            if (penaltyUidStr == null || penaltyStatus == null) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long penaltyUid;
            try {
                penaltyUid = Long.parseLong(penaltyUidStr);
            } catch (NumberFormatException e) {
                result.addProperty("success", false);
                result.addProperty("message", "유효하지 않은 패널티 ID 형식입니다.");
                return result;
            }
            
            if (!penaltyStatus.equals("active") && !penaltyStatus.equals("inactive")) {
                result.addProperty("success", false);
                result.addProperty("message", "유효하지 않은 상태값입니다. 'active' 또는 'inactive'만 허용됩니다.");
                return result;
            }
            
            boolean updated = penaltyService.updateUserPenaltyStatus(penaltyUid, penaltyStatus);
            
            if (updated) {
                result.addProperty("success", true);
                result.addProperty("message", "패널티 상태가 성공적으로 업데이트되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "패널티 상태 업데이트에 실패했습니다.");
            }
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "패널티 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }

    /* ---------- 신고 처리 ---------- */

    /**
     * 모든 신고 내역 조회
     */
    private JsonObject getAllReport(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            List<AdminReportDTO> reports = reportService.getAllReports();
            
            if (reports != null) {
                JsonArray reportArray = new JsonArray();
                
                for (AdminReportDTO report : reports) {
                    JsonObject reportObj = new JsonObject();
                    reportObj.addProperty("reportUid", report.getReportUid());
                    reportObj.addProperty("reportUserUid", report.getReportUserUid());
                    reportObj.addProperty("reporterUsername", report.getReporterUsername());
                    reportObj.addProperty("targetUserUid", report.getTargetUserUid());
                    reportObj.addProperty("targetUsername", report.getTargetUsername());
                    reportObj.addProperty("reportTargetType", report.getReportTargetType());
                    reportObj.addProperty("reportReason", report.getReportReason());
                    reportObj.addProperty("reportStatus", report.getReportStatus());
                    reportObj.addProperty("reportCreatetime", report.getReportCreatetime().toString());
                    
                    reportArray.add(reportObj);
                }
                
                result.addProperty("success", true);
                result.add("reports", reportArray);
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "신고 내역을 가져오는데 실패했습니다.");
            }
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 조건별 신고 내역 검색
     */
    private JsonObject getReportsByCondition(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            String status = request.getParameter("status");
            String targetType = request.getParameter("targetType");
            
            List<ReportDTO> reports = reportService.getReportsByCondition(status, targetType);
            
            if (reports != null) {
                JsonArray reportArray = new JsonArray();
                
                for (ReportDTO report : reports) {
                    JsonObject reportObj = new JsonObject();
                    reportObj.addProperty("reportUid", report.getReportUid());
                    reportObj.addProperty("reportUserUid", report.getReportUserUid());
                    reportObj.addProperty("reporterUsername", report.getReporterUsername());
                    reportObj.addProperty("targetUserUid", report.getTargetUserUid());
                    reportObj.addProperty("targetUsername", report.getTargetUsername());
                    reportObj.addProperty("reportTargetType", report.getReportTargetType());
                    reportObj.addProperty("reportReason", report.getReportReason());
                    reportObj.addProperty("reportStatus", report.getReportStatus());
                    reportObj.addProperty("reportCreatetime", report.getReportCreatetime().toString());
                    
                    reportArray.add(reportObj);
                }
                
                result.addProperty("success", true);
                result.add("reports", reportArray);
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "신고 내역을 가져오는데 실패했습니다.");
            }
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * 신고 처리 및 패널티 부여
     */
    private JsonObject processReport(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            // 필수 파라미터 확인
            String reportUidStr = request.getParameter("reportUid");
            String targetUserUidStr = request.getParameter("targetUserUid");
            String penaltyReason = request.getParameter("penaltyReason");
            String penaltyDuration = request.getParameter("penaltyDuration");
            String penaltyEndDateStr = request.getParameter("penaltyEndDate");
            String adminUidStr = request.getParameter("adminUid");
            
            // 파라미터 검증
            if (reportUidStr == null || targetUserUidStr == null || penaltyReason == null || 
                penaltyDuration == null || adminUidStr == null) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            // 파라미터 변환
            long reportUid = Long.parseLong(reportUidStr);
            long targetUserUid = Long.parseLong(targetUserUidStr);
            long adminUid = Long.parseLong(adminUidStr);
            
            // 패널티 종료일 처리 (temporary인 경우만 필요)
            Date penaltyEndDate = null;
            if ("temporary".equals(penaltyDuration) && penaltyEndDateStr != null && !penaltyEndDateStr.trim().isEmpty()) {
                penaltyEndDate = Date.valueOf(penaltyEndDateStr);
            }
            
            // 패널티 부여
            boolean success = reportService.processReportAndApplyPenalty(
                reportUid, targetUserUid, penaltyReason, penaltyDuration, penaltyEndDate, adminUid);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "신고 처리 및 패널티 부여가 완료되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "신고 처리 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    /* ---------- 추가 메소드들 (보일러플레이트 코드 생략) ---------- */

    // 게시물 관리 메소드들
    private JsonObject getAllDeletePostLogs(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject getAllDeleteCommentLogs(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject recoverDeletedPost(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject recoverDeletedComment(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    // 키보드 용어 관리 메소드들
    private JsonObject addGuide(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject updateGuide(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject deleteGuide(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    // 키보드 DB 관리 메소드들
    private JsonObject addKeyboardInfo(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject updateKeyboardInfo(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }

    private JsonObject deleteKeyboardInfo(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 구현 생략
        return result;
    }
}
