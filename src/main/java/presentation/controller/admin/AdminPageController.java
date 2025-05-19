package presentation.controller.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import business.service.admin.AdminGuideService;
import business.service.admin.AdminKeyboardService;
import business.service.admin.AdminLogService;
import business.service.admin.AdminReportService;
import business.service.admin.AdminUserService;
import com.google.gson.Gson;
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
import presentation.controller.page.Controller;
import util.logging.LoggerConfig;
import util.web.RequestRouter;

/**
 * 관리자 전용 페이지 컨트롤러
 * 게시판의 콘텐츠 이외의 내용을 관리하기 위한 페이지
 */
@WebServlet({"/admin/*", "/admin.do"})
public class AdminPageController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;

    private AdminUserService userService;
    private AdminReportService reportService;
    private AdminLogService logService;
    private AdminGuideService guideService;
    private AdminKeyboardService keyboardService;
    private util.web.RequestRouter router;
    private final Gson gson = new Gson();

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

        // 라우터 설정
        initRequestRouter();
    }

    /**
     * 요청 라우터 초기화
     */
    private void initRequestRouter() {
        router = new util.web.RequestRouter();        // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "관리자 대시보드");
            return result;
        });

        router.getJson("/user/penalty", (req, res) -> {
            return userService.getAllUserPenalty();
        });

        router.getJson("/user/penalty/search", (req, res) -> {
            String userId = req.getParameter("userId");
            return userService.getUserPenaltyByUserId(Long.parseLong(userId));
        });

        // 신고 관리 라우트
        router.getJson("/report", (req, res) -> {
            return reportService.getAllReport();
        });

        router.getJson("/report/search", (req, res) -> {
            String status = req.getParameter("status");
            String targetType = req.getParameter("targetType");
            return reportService.getReportsByCondition(status, targetType);
        });

        // 삭제 로그 관리 라우트
        router.getJson("/log/post", (req, res) -> {
            return logService.getAllDeletePostLogs();
        });

        router.getJson("/log/comment", (req, res) -> {
            return logService.getAllDeleteCommentLogs();
        });        // POST 요청 JSON 라우터 설정
        router.postJson("/user/penalty/update", (req, res) -> {
            Long userId = Long.parseLong(req.getParameter("userId"));
            Long penaltyId = Long.parseLong(req.getParameter("penaltyId"));
            String action = req.getParameter("action");
            boolean result = userService.updateUserPenaltyStatusByUserId(penaltyId, action);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            return response;
        });

        router.postJson("/report/update", (req, res) -> {
            Long reportId = Long.parseLong(req.getParameter("reportId"));
            String status = req.getParameter("status");
            boolean result = reportService.updateReportStatus(reportId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            return response;
        });
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }    /**
     * GET 요청 처리
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        // .do로 끝나는 요청은 JSON 응답으로 처리
        if (servletPath != null && servletPath.endsWith(".do")) {
            handleJsonRequest(request, response);
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            // 관리자 대시보드 JSON 응답
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "관리자 대시보드");
            sendJsonResponse(response, result);
            return;
        }

        // 경로에 따른 처리
        try {
            switch (pathInfo) {
                // 불량 회원 관리
                case "/user/penalty":
                    sendJsonResponse(response, userService.getAllUserPenalty());
                    break;
                case "/user/penalty/search": {
                    String userIdStr = request.getParameter("userId");
                    if (userIdStr == null || userIdStr.isEmpty()) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "사용자 ID가 필요합니다");
                        return;
                    }
                    long userId = Long.parseLong(userIdStr);
                    sendJsonResponse(response, userService.getUserPenaltyByUserId(userId));
                    break;
                }

                // 신고 처리
                case "/report":
                    sendJsonResponse(response, reportService.getAllReport());
                    break;
                case "/report/search": {
                    String status = request.getParameter("status");
                    String targetType = request.getParameter("targetType");
                    sendJsonResponse(response, reportService.getReportsByCondition(status, targetType));
                    break;
                }

                // 게시물 관리
                case "/log/post":
                    sendJsonResponse(response, logService.getAllDeletePostLogs());
                    break;
                case "/log/comment":
                    sendJsonResponse(response, logService.getAllDeleteCommentLogs());
                    break;
                case "/log/post/search": {
                    String boardType = request.getParameter("boardType");
                    String keyword = request.getParameter("keyword");
                    sendJsonResponse(response, logService.getDeletePostLogsByCondition(boardType, keyword));
                    break;
                }
                case "/log/comment/search": {
                    String boardType = request.getParameter("boardType");
                    String keyword = request.getParameter("keyword");
                    sendJsonResponse(response, logService.getDeleteCommentLogsByCondition(boardType, keyword));
                    break;
                }

                // 키보드 용어 페이지 관리
                case "/guide":
                    sendJsonResponse(response, guideService.getAllGuides());
                    break;
                case "/guide/category":
                    sendJsonResponse(response, guideService.getAllGuideCategories());
                    break;

                // 키보드 DB 관리
                case "/keyboard":
                    sendJsonResponse(response, keyboardService.getAllKeyboardInfos());
                    break;
                case "/keyboard/switch":
                    sendJsonResponse(response, keyboardService.getAllSwitchCategories());
                    break;
                case "/keyboard/layout":
                    sendJsonResponse(response, keyboardService.getAllLayoutCategories());
                    break;
                case "/keyboard/connect":
                    sendJsonResponse(response, keyboardService.getAllConnectCategories());
                    break;
                case "/keyboard/tag":
                    sendJsonResponse(response, keyboardService.getAllKeyboardTags());
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            LoggerConfig.logError(AdminPageController.class, "doGet", "관리자 페이지 GET 요청 처리 중 오류 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * JSON API 요청 처리
     */
    private void handleJsonRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String servletPath = request.getServletPath();

        try {
            // servletPath에서 .do를 제거하고 매핑
            String action = servletPath.substring(0, servletPath.length() - 3);

            switch (action) {
                // 예: /admin/user/penalty.do
                case "/admin/user/penalty":
                    sendJsonResponse(response, userService.getAllUserPenalty());
                    break;

                // 다른 API 엔드포인트들...
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    break;
            }
        } catch (Exception e) {
            LoggerConfig.logError(getClass(), "handleJsonRequest", "JSON 요청 처리 중 오류 발생", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendJsonResponse(response, errorResponse);
        }
    }    /**
     * POST 요청 처리
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        // 라우터로 처리 시도
        if (router.handle(request, response)) {
            return;  // 라우터가 요청을 처리함
        }

        // JSON 응답 객체 준비
        Map<String, Object> result = new HashMap<>();

        try {
            switch (pathInfo) {
                // 불량 회원 관리
                case "/user/penalty/update": {
                    String penaltyIdStr = request.getParameter("penaltyId");
                    String newStatus = request.getParameter("status");

                    if (penaltyIdStr == null || newStatus == null) {
                        result.put("success", false);
                        result.put("message", "필수 파라미터가 누락되었습니다.");
                        response.setStatus(400); // Bad Request
                        sendJsonResponse(response, result);
                        return;
                    }

                    long penaltyId = Long.parseLong(penaltyIdStr);
                    boolean success = userService.updateUserPenaltyStatusByUserId(penaltyId, newStatus);

                    result.put("success", success);
                    if (success) {
                        result.put("message", "패널티 상태가 성공적으로 변경되었습니다.");
                        LoggerConfig.logBusinessAction(AdminPageController.class, "handleUpdateUserPenaltyStatusByUserId",
                                "패널티 상태 변경", "ID: " + penaltyId + ", 상태: " + newStatus, null);
                    } else {
                        result.put("message", "패널티 상태 변경에 실패했습니다.");
                        response.setStatus(500); // Internal Server Error
                    }
                    break;
                }

                // 신고 처리
                case "/report/update": {
                    String reportIdStr = request.getParameter("reportId");
                    String status = request.getParameter("status");

                    if (reportIdStr == null || status == null) {
                        result.put("success", false);
                        result.put("message", "필수 파라미터가 누락되었습니다.");
                        response.setStatus(400);
                        sendJsonResponse(response, result);
                        return;
                    }

                    long reportId = Long.parseLong(reportIdStr);
                    boolean success = reportService.updateReportStatus(reportId, status);

                    result.put("success", success);
                    if (success) {
                        result.put("message", "신고 상태가 성공적으로 변경되었습니다.");
                        LoggerConfig.logBusinessAction(AdminPageController.class, "handleUpdateReportStatus",
                                "신고 상태 변경", "ID: " + reportId + ", 상태: " + status, null);
                    } else {
                        result.put("message", "신고 상태 변경에 실패했습니다.");
                        response.setStatus(500);
                    }
                    break;
                }

                case "/report/penalty": {
                    // 패널티 적용 로직 - 복잡한 처리이므로 기존 핸들러 사용
                    handleApplyPenalty(request, response);
                    return; // 핸들러에서 직접 응답 처리
                }

                // 삭제된 항목 복원
                case "/log/post/recover":
                case "/log/comment/recover":
                case "/log/attach/recover": {
                    String targetType = pathInfo.contains("post") ? "게시글" : (pathInfo.contains("comment") ? "댓글" : "첨부파일");
                    String idParam = pathInfo.contains("post") ? "postId" : (pathInfo.contains("comment") ? "commentId" : "attachId");
                    String idStr = request.getParameter(idParam);
                    String boardType = request.getParameter("boardType");

                    if (idStr == null || boardType == null) {
                        result.put("success", false);
                        result.put("message", "필수 파라미터가 누락되었습니다.");
                        response.setStatus(400);
                        sendJsonResponse(response, result);
                        return;
                    }

                    long id = Long.parseLong(idStr);
                    boolean success = false;

                    if (pathInfo.contains("post")) {
                        success = logService.recoverDeletedPost(boardType, id);
                    } else if (pathInfo.contains("comment")) {
                        success = logService.recoverDeletedComment(boardType, id);
                    } else {
                        success = logService.recoverDeletedAttach(boardType, id);
                    }

                    result.put("success", success);
                    if (success) {
                        result.put("message", targetType + " 복원에 성공했습니다.");
                        LoggerConfig.logBusinessAction(AdminPageController.class, "handleRecover" + targetType,
                                targetType + " 복원", "ID: " + id + ", 게시판: " + boardType, null);
                    } else {
                        result.put("message", targetType + " 복원에 실패했습니다.");
                        response.setStatus(500);
                    }
                    break;
                }

                // 키보드 관련 요청 - 이미 존재하는 핸들러 호출
                case "/keyboard/add":
                case "/keyboard/update":
                case "/keyboard/delete":
                case "/keyboard/switch/add":
                case "/keyboard/switch/update":
                case "/keyboard/switch/delete":
                case "/keyboard/layout/add":
                case "/keyboard/layout/update":
                case "/keyboard/layout/delete":
                case "/keyboard/connect/add":
                case "/keyboard/connect/update":
                case "/keyboard/connect/delete":
                case "/keyboard/tag/add":
                case "/keyboard/tag/update":
                case "/keyboard/tag/delete":
                case "/keyboard/tag/confirm":
                case "/guide/add":
                case "/guide/update":
                case "/guide/delete":
                case "/guide/category/add":
                case "/guide/category/update":
                case "/guide/category/delete":
                    // 복잡한 요청들은 기존 핸들러 메서드 활용
                    // 이후 단계적으로 JSON 응답 방식으로 전환
                    processLegacyPostRequest(request, response, pathInfo);
                    return;

                default:
                    result.put("success", false);
                    result.put("message", "요청한 API 경로가 존재하지 않습니다.");
                    response.setStatus(404);
                    break;
            }

            // 응답 전송
            sendJsonResponse(response, result);

        } catch (Exception e) {
            LoggerConfig.logError(AdminPageController.class, "doPost", "POST 요청 처리 중 오류 발생: " + pathInfo, e);
            result.put("success", false);
            result.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            response.setStatus(500);
            sendJsonResponse(response, result);
        }
    }

    /**
     * 기존 핸들러 메서드를 호출하는 헬퍼 메서드
     */
    private void processLegacyPostRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws ServletException, IOException {
        switch (pathInfo) {
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

        Map<String, Object> result = new HashMap<>();

        if (penaltyIdStr == null || newStatus == null) {
            result.put("success", false);
            result.put("message", "필수 파라미터가 누락되었습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendJsonResponse(response, result);
            return;
        }

        long penaltyId = Long.parseLong(penaltyIdStr);
        boolean success = userService.updateUserPenaltyStatusByUserId(penaltyId, newStatus);

        result.put("success", success);
        if (success) {
            result.put("message", "패널티 상태가 성공적으로 변경되었습니다.");
        } else {
            result.put("message", "패널티 상태 변경에 실패했습니다.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        sendJsonResponse(response, result);
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
