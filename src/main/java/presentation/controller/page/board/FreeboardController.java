package presentation.controller.page;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import business.service.board.FreeboardService;
import dto.board.FreeboardDTO;
import presentation.controller.Controller;
import util.file.FileUtil;

public class FreeboardController implements Controller {

    private FreeboardService freeboardService;
    
    public FreeboardController() {
        freeboardService = new FreeboardService();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            // 기본 자유게시판 페이지 표시
            showFreeboardList(request, response);
            return;
        }
        
        switch (action) {
            case "list":
                showFreeboardList(request, response);
                break;
            case "view":
                viewFreeboardDetail(request, response);
                break;
            case "write":
                showWriteForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/error");
                break;
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
            return;
        }
        
        // AJAX 요청 처리
        if (request.getHeader("X-Requested-With") != null && 
            request.getHeader("X-Requested-With").equals("XMLHttpRequest")) {
            
            JsonObject result = new JsonObject();
            
            switch (action) {
                case "getAllFreeboard":
                    result = getAllFreeboard(request);
                    break;
                case "postFreeboard":
                    result = postFreeboard(request);
                    break;
                case "setNoticeById":
                    result = setNoticeById(request);
                    break;
                case "hideFreeboardById":
                    result = hideFreeboardById(request);
                    break;
                case "reportFreeboardById":
                    result = reportFreeboardById(request);
                    break;
                case "reportUserById":
                    result = reportUserById(request);
                    break;
                case "deleteFreeboardAttachByFilename":
                    result = deleteFreeboardAttachByFilename(request);
                    break;
                case "updateFreeboardById":
                    result = updateFreeboardById(request);
                    break;
                case "deleteFreeboardById":
                    result = deleteFreeboardById(request);
                    break;
                default:
                    result.addProperty("success", false);
                    result.addProperty("message", "잘못된 요청입니다.");
                    break;
            }
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(result.toString());
            out.flush();
            return;
        }
        
        // 일반 폼 제출 처리
        switch (action) {
            case "post":
                processPostFreeboard(request, response);
                break;
            case "update":
                processUpdateFreeboard(request, response);
                break;
            case "delete":
                processDeleteFreeboard(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/freeboard");
                break;
        }
    }
    
    /* ---------- View 관련 메소드 ---------- */
    
    // 게시글 목록 페이지 표시
    private void showFreeboardList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int page = 1;
        int pageSize = 10;
        
        try {
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
        } catch (NumberFormatException e) {
            page = 1;
        }
        
        try {
            List<FreeboardDTO> posts = freeboardService.getAllFreeboards(page, pageSize);
            int totalPosts = freeboardService.getTotalPostsCount();
            int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
            
            request.setAttribute("posts", posts);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.getRequestDispatcher("/view/pages/board/freeboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
    
    // 게시글 상세 보기 페이지 표시
    private void viewFreeboardDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String postIdStr = request.getParameter("id");
        
        if (postIdStr == null || postIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
            return;
        }
        
        try {
            long postId = Long.parseLong(postIdStr);
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            
            if (post == null) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=notfound");
                return;
            }
            
            // 조회수 증가
            freeboardService.increaseViewCount(postId);
            
            request.setAttribute("post", post);
            request.getRequestDispatcher("/view/pages/board/freeboard_detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
    
    // 게시글 작성 폼 표시
    private void showWriteForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard?action=write");
            return;
        }
        
        request.getRequestDispatcher("/view/pages/board/freeboard_write.jsp").forward(request, response);
    }
    
    // 게시글 수정 폼 표시
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard");
            return;
        }
        
        String postIdStr = request.getParameter("id");
        
        if (postIdStr == null || postIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
            return;
        }
        
        try {
            long postId = Long.parseLong(postIdStr);
            long userId = (long) session.getAttribute("userId");
            String userAuthority = (String) session.getAttribute("userAuthority");
            
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            
            if (post == null) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=notfound");
                return;
            }
            
            // 작성자 본인이거나 관리자/매니저만 수정 가능
            if (post.getUserUid() != userId && !"admin".equals(userAuthority) && !"armband".equals(userAuthority)) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=unauthorized");
                return;
            }
            
            request.setAttribute("post", post);
            request.getRequestDispatcher("/view/pages/board/freeboard_edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
    
    /* ---------- AJAX 처리 메소드 ---------- */
    
    // 게시글 목록 조회 (AJAX)
    private JsonObject getAllFreeboard(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            int page = 1;
            int pageSize = 10;
            
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }
            
            if (request.getParameter("pageSize") != null) {
                pageSize = Integer.parseInt(request.getParameter("pageSize"));
            }
            
            List<FreeboardDTO> posts = freeboardService.getAllFreeboards(page, pageSize);
            int totalPosts = freeboardService.getTotalPostsCount();
            int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
            
            JsonArray postsArray = new JsonArray();
            
            for (FreeboardDTO post : posts) {
                JsonObject postObj = new JsonObject();
                postObj.addProperty("id", post.getFreeboardUid());
                postObj.addProperty("title", post.getFreeboardTitle());
                postObj.addProperty("author", post.getUserName());
                postObj.addProperty("date", post.getFreeboardWritetime().toString());
                postObj.addProperty("views", post.getFreeboardViewcnt());
                postObj.addProperty("isNotice", "notification".equals(post.getFreeboardNotify()));
                
                postsArray.add(postObj);
            }
            
            result.addProperty("success", true);
            result.add("posts", postsArray);
            result.addProperty("currentPage", page);
            result.addProperty("totalPages", totalPages);
            result.addProperty("totalPosts", totalPosts);
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "게시글 목록을 불러오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 게시글 등록 (AJAX)
    private JsonObject postFreeboard(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.addProperty("success", false);
            result.addProperty("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            long userId = (long) session.getAttribute("userId");
            String clientIp = request.getRemoteAddr();
            
            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "제목과 내용을 모두 입력해주세요.");
                return result;
            }
            
            FreeboardDTO post = new FreeboardDTO();
            post.setFreeboardTitle(title);
            post.setFreeboardContents(content);
            post.setUserUid(userId);
            post.setFreeboardAuthorIp(clientIp);
            
            boolean success = freeboardService.postFreeboard(post);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "게시글이 등록되었습니다.");
                result.addProperty("postId", post.getFreeboardUid());
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "게시글 등록에 실패했습니다.");
            }
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "게시글 등록 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 공지사항 지정/해제 (AJAX)
    private JsonObject setNoticeById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 관리자/매니저 권한 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userAuthority") == null || 
            (!session.getAttribute("userAuthority").equals("admin") && 
             !session.getAttribute("userAuthority").equals("armband"))) {
            
            result.addProperty("success", false);
            result.addProperty("message", "권한이 없습니다.");
            return result;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            String noticeStatusStr = request.getParameter("isNotice");
            
            if (postIdStr == null || noticeStatusStr == null) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long postId = Long.parseLong(postIdStr);
            boolean isNotice = Boolean.parseBoolean(noticeStatusStr);
            
            boolean success = freeboardService.setNoticeById(postId, isNotice);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", isNotice ? "공지사항으로 지정되었습니다." : "공지사항 지정이 해제되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "처리 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 게시글 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 게시글 숨김 처리 (AJAX)
    private JsonObject hideFreeboardById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 관리자/매니저 권한 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userAuthority") == null || 
            (!session.getAttribute("userAuthority").equals("admin") && 
             !session.getAttribute("userAuthority").equals("armband"))) {
            
            result.addProperty("success", false);
            result.addProperty("message", "권한이 없습니다.");
            return result;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            String hideReason = request.getParameter("reason");
            
            if (postIdStr == null || hideReason == null || hideReason.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            boolean success = freeboardService.hideFreeboardById(postId, hideReason);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "게시글이 숨김 처리되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "숨김 처리 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 게시글 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 게시글 신고 (AJAX)
    private JsonObject reportFreeboardById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.addProperty("success", false);
            result.addProperty("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            String reportReason = request.getParameter("reason");
            long reporterUid = (long) session.getAttribute("userId");
            
            if (postIdStr == null || reportReason == null || reportReason.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            // 신고 대상의 게시글 정보 조회
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            if (post == null) {
                result.addProperty("success", false);
                result.addProperty("message", "존재하지 않는 게시글입니다.");
                return result;
            }
            
            // 자신의 게시글을 신고할 수 없음
            if (post.getUserUid() == reporterUid) {
                result.addProperty("success", false);
                result.addProperty("message", "자신의 게시글은 신고할 수 없습니다.");
                return result;
            }
            
            boolean success = freeboardService.reportFreeboardById(postId, post.getUserUid(), reporterUid, reportReason);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "게시글이 신고되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "신고 처리 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 게시글 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 사용자 신고 (AJAX)
    private JsonObject reportUserById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.addProperty("success", false);
            result.addProperty("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            String targetUserIdStr = request.getParameter("userId");
            String reportReason = request.getParameter("reason");
            long reporterUid = (long) session.getAttribute("userId");
            
            if (targetUserIdStr == null || reportReason == null || reportReason.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long targetUserId = Long.parseLong(targetUserIdStr);
            
            // 자신을 신고할 수 없음
            if (targetUserId == reporterUid) {
                result.addProperty("success", false);
                result.addProperty("message", "자신을 신고할 수 없습니다.");
                return result;
            }
            
            boolean success = freeboardService.reportUserById(targetUserId, reporterUid, reportReason);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "사용자가 신고되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "신고 처리 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 사용자 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 첨부파일 삭제 (AJAX)
    private JsonObject deleteFreeboardAttachByFilename(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.addProperty("success", false);
            result.addProperty("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            String filename = request.getParameter("filename");
            long userId = (long) session.getAttribute("userId");
            String userAuthority = (String) session.getAttribute("userAuthority");
            
            if (postIdStr == null || filename == null || filename.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            // 게시글 소유자 또는 관리자/매니저 권한 확인
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            if (post == null) {
                result.addProperty("success", false);
                result.addProperty("message", "존재하지 않는 게시글입니다.");
                return result;
            }
            
            boolean isAuthorized = post.getUserUid() == userId || 
                                  "admin".equals(userAuthority) || 
                                  "armband".equals(userAuthority);
                                  
            if (!isAuthorized) {
                result.addProperty("success", false);
                result.addProperty("message", "파일 삭제 권한이 없습니다.");
                return result;
            }
            
            boolean success = freeboardService.deleteFreeboardAttachByFilename(postId, filename);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "첨부파일이 삭제되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "첨부파일 삭제 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 게시글 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 게시글 수정 (AJAX)
    private JsonObject updateFreeboardById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.addProperty("success", false);
            result.addProperty("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            long userId = (long) session.getAttribute("userId");
            String userAuthority = (String) session.getAttribute("userAuthority");
            String clientIp = request.getRemoteAddr();
            
            if (postIdStr == null || title == null || title.trim().isEmpty() || 
                content == null || content.trim().isEmpty()) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            // 게시글 조회
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            if (post == null) {
                result.addProperty("success", false);
                result.addProperty("message", "존재하지 않는 게시글입니다.");
                return result;
            }
            
            // 권한 체크: 본인 글이거나 관리자/매니저인 경우만 수정 가능
            boolean isAuthorized = post.getUserUid() == userId || 
                                  "admin".equals(userAuthority) || 
                                  "armband".equals(userAuthority);
                                  
            if (!isAuthorized) {
                result.addProperty("success", false);
                result.addProperty("message", "게시글 수정 권한이 없습니다.");
                return result;
            }
            
            // 게시글 정보 업데이트
            post.setFreeboardTitle(title);
            post.setFreeboardContents(content);
            post.setFreeboardAuthorIp(clientIp);
            
            boolean success = freeboardService.updateFreeboardById(post);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "게시글이 수정되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "게시글 수정 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 게시글 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // 게시글 삭제 (AJAX)
    private JsonObject deleteFreeboardById(HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            result.addProperty("success", false);
            result.addProperty("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            long userId = (long) session.getAttribute("userId");
            String userAuthority = (String) session.getAttribute("userAuthority");
            
            if (postIdStr == null) {
                result.addProperty("success", false);
                result.addProperty("message", "필수 파라미터가 누락되었습니다.");
                return result;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            // 게시글 조회
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            if (post == null) {
                result.addProperty("success", false);
                result.addProperty("message", "존재하지 않는 게시글입니다.");
                return result;
            }
            
            // 권한 체크: 본인 글이거나 관리자/매니저인 경우만 삭제 가능
            boolean isAuthorized = post.getUserUid() == userId || 
                                  "admin".equals(userAuthority) || 
                                  "armband".equals(userAuthority);
                                  
            if (!isAuthorized) {
                result.addProperty("success", false);
                result.addProperty("message", "게시글 삭제 권한이 없습니다.");
                return result;
            }
            
            boolean success = freeboardService.deleteFreeboardById(postId);
            
            if (success) {
                result.addProperty("success", true);
                result.addProperty("message", "게시글이 삭제되었습니다.");
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "게시글 삭제 중 오류가 발생했습니다.");
            }
        } catch (NumberFormatException e) {
            result.addProperty("success", false);
            result.addProperty("message", "잘못된 게시글 ID 형식입니다.");
        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /* ---------- 폼 처리 메소드 ---------- */
    
    // 게시글 등록 처리 (폼 제출)
    private void processPostFreeboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard?action=write");
            return;
        }
        
        try {
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            long userId = (long) session.getAttribute("userId");
            String clientIp = request.getRemoteAddr();
            
            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=write&error=empty");
                return;
            }
            
            FreeboardDTO post = new FreeboardDTO();
            post.setFreeboardTitle(title);
            post.setFreeboardContents(content);
            post.setUserUid(userId);
            post.setFreeboardAuthorIp(clientIp);
            
            boolean success = freeboardService.postFreeboard(post);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + post.getFreeboardUid());
            } else {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=write&error=failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
    
    // 게시글 수정 처리 (폼 제출)
    private void processUpdateFreeboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard");
            return;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            long userId = (long) session.getAttribute("userId");
            String userAuthority = (String) session.getAttribute("userAuthority");
            String clientIp = request.getRemoteAddr();
            
            if (postIdStr == null || title == null || title.trim().isEmpty() || 
                content == null || content.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=empty");
                return;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            // 게시글 조회
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            if (post == null) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=notfound");
                return;
            }
            
            // 권한 체크: 본인 글이거나 관리자/매니저인 경우만 수정 가능
            boolean isAuthorized = post.getUserUid() == userId || 
                                  "admin".equals(userAuthority) || 
                                  "armband".equals(userAuthority);
                                  
            if (!isAuthorized) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=unauthorized");
                return;
            }
            
            // 게시글 정보 업데이트
            post.setFreeboardTitle(title);
            post.setFreeboardContents(content);
            post.setFreeboardAuthorIp(clientIp);
            
            boolean success = freeboardService.updateFreeboardById(post);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + postId);
            } else {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=edit&id=" + postId + "&error=failed");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
    
    // 게시글 삭제 처리 (폼 제출)
    private void processDeleteFreeboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=freeboard");
            return;
        }
        
        try {
            String postIdStr = request.getParameter("postId");
            long userId = (long) session.getAttribute("userId");
            String userAuthority = (String) session.getAttribute("userAuthority");
            
            if (postIdStr == null) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=empty");
                return;
            }
            
            long postId = Long.parseLong(postIdStr);
            
            // 게시글 조회
            FreeboardDTO post = freeboardService.getFreeboardById(postId);
            if (post == null) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=notfound");
                return;
            }
            
            // 권한 체크: 본인 글이거나 관리자/매니저인 경우만 삭제 가능
            boolean isAuthorized = post.getUserUid() == userId || 
                                  "admin".equals(userAuthority) || 
                                  "armband".equals(userAuthority);
                                  
            if (!isAuthorized) {
                response.sendRedirect(request.getContextPath() + "/freeboard?error=unauthorized");
                return;
            }
            
            boolean success = freeboardService.deleteFreeboardById(postId);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/freeboard?deleted=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/freeboard?action=view&id=" + postId + "&error=delete_failed");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/freeboard");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
