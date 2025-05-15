package presentation.controller.page.board;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import business.service.user.UserService;
import business.service.board.BoardService;
import business.service.database.KeyboardInfoService;
import dto.user.UserDTO;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.board.PostDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.SecurityUtil;

/**
 * 마이페이지 컨트롤러
 * 사용자 정보 및 활동 내역 관리 기능 제공
 */
public class UserpageController implements Controller {
    private final UserService userService;
    private final KeyboardInfoService keyboardInfoService;
    private final BoardService boardService;
    
    public UserpageController() {
        this.userService = new UserService();
        this.keyboardInfoService = new KeyboardInfoService();
        this.boardService = new BoardService();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            // 기본: 사용자 정보 페이지
            getMyUserInfo(request, response);
            return;
        }
        
        switch (action) {
            case "info":
                getMyUserInfo(request, response);
                break;
            case "scraps":
                getAllMyScraps(request, response);
                break;
            case "posts":
                getAllMyPosts(request, response);
                break;
            case "scores":
                getAllMyKeyboardScores(request, response);
                break;
            default:
                getMyUserInfo(request, response);
                break;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            if (isAjaxRequest(request)) {
                sendJsonResponse(response, false, "로그인이 필요한 기능입니다.");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "요청된 작업이 없습니다.");
            return;
        }
        
        switch (action) {
            case "update":
                updateMyUserInfo(request, response);
                break;
            case "delete":
                requestDeleteMyUserInfo(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 작업입니다.");
                break;
        }
    }

    /**
     * 내 정보 읽어오기
     */
    private void getMyUserInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 최신 사용자 정보 조회 (DB에서 갱신된 정보 확인)
            UserDTO updatedUser = userService.getUserById(userId);
            if (updatedUser == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "사용자 정보를 찾을 수 없습니다.");
                return;
            }
            
            // 세션 정보 업데이트
            session.setAttribute("user", updatedUser);
            
            // 요청 속성 설정
            request.setAttribute("user", updatedUser);
            request.setAttribute("activeTab", "info");
            
            // 마이페이지로 포워딩
            request.getRequestDispatcher("/view/pages/mypage.html").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 내 정보 수정하기
     */
    private void updateMyUserInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 최신 사용자 정보 조회
            UserDTO updatedUser = userService.getUserById(userId);
            if (updatedUser == null) {
                sendJsonResponse(response, false, "사용자 정보를 찾을 수 없습니다.");
                return;
            }
            
            // 수정할 정보 가져오기
            String nickname = request.getParameter("nickname");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String passwordConfirm = request.getParameter("passwordConfirm");
            String bio = request.getParameter("bio");
            
            // 닉네임 변경 처리
            if (nickname != null && !nickname.trim().isEmpty() && !nickname.equals(updatedUser.getUserName())) {
                // 닉네임 중복 확인
                if (userService.checkDuplicateNickname(nickname)) {
                    sendJsonResponse(response, false, "이미 사용 중인 닉네임입니다.");
                    return;
                }
                updatedUser.setUserName(nickname);
            }
            
            // 이메일 변경 처리
            if (email != null && !email.trim().isEmpty() && !email.equals(updatedUser.getUserEmail())) {
                // 이메일 중복 확인
                if (userService.checkDuplicateEmail(email)) {
                    sendJsonResponse(response, false, "이미 사용 중인 이메일입니다.");
                    return;
                }
                updatedUser.setUserEmail(email);
            }
            
            // 비밀번호 변경 처리
            if (password != null && !password.trim().isEmpty()) {
                if (!password.equals(passwordConfirm)) {
                    sendJsonResponse(response, false, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                    return;
                }
                
                // 비밀번호 유효성 검사
                if (!userService.checkPasswordValidation(password)) {
                    sendJsonResponse(response, false, "비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다.");
                    return;
                }
                
                // 비밀번호 암호화 및 설정
                String hashedPassword = SecurityUtil.hashPassword(password);
                updatedUser.setUserPassword(hashedPassword);
            }
            
            // 자기소개 변경 처리
            if (bio != null) {
                updatedUser.setUserIntroduce(bio);
            }
            
            // 사용자 정보 업데이트
            boolean success = userService.updateUser(updatedUser);
            
            if (success) {
                // 세션 정보 업데이트
                session.setAttribute("user", updatedUser);
                sendJsonResponse(response, true, "사용자 정보가 성공적으로 업데이트되었습니다.");
            } else {
                sendJsonResponse(response, false, "사용자 정보 업데이트에 실패했습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 회원 탈퇴 요청
     */
    private void requestDeleteMyUserInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 비밀번호 확인
            String password = request.getParameter("password");
            
            if (password == null || password.trim().isEmpty()) {
                sendJsonResponse(response, false, "비밀번호를 입력해주세요.");
                return;
            }
            
            // 비밀번호 검증
            boolean passwordValid = userService.verifyPassword(userId, password);
            
            if (!passwordValid) {
                sendJsonResponse(response, false, "비밀번호가 일치하지 않습니다.");
                return;
            }
            
            // 탈퇴 사유 (선택사항)
            String reason = request.getParameter("reason");
            
            // 회원 탈퇴 요청 처리
            boolean success = userService.requestDeleteUser(userId, reason);
            
            if (success) {
                // 세션 무효화
                session.invalidate();
                sendJsonResponse(response, true, "회원 탈퇴가 처리되었습니다. 그동안 이용해주셔서 감사합니다.");
            } else {
                sendJsonResponse(response, false, "회원 탈퇴 처리에 실패했습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 스크랩한 키보드 확인
     */
    private void getAllMyScraps(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 페이징 처리
            int page = 1;
            int pageSize = 8; // 한 페이지에 표시할 스크랩 수
            
            if (request.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(request.getParameter("page"));
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 기본값 사용
                }
            }
            
            // 스크랩 목록 조회
            List<KeyboardInfoDTO> scraps = keyboardInfoService.getScrapsByUserId(userId, page, pageSize);
            int totalScraps = keyboardInfoService.getTotalScrapCountByUserId(userId);
            
            // 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalScraps / pageSize);
            
            // 요청 속성 설정
            request.setAttribute("user", user);
            request.setAttribute("scraps", scraps);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("activeTab", "scraps");
            
            // 마이페이지로 포워딩
            request.getRequestDispatcher("/view/pages/mypage.html").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 내가 쓴 글 내역 확인
     */
    private void getAllMyPosts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 페이징 처리
            int page = 1;
            int pageSize = 10; // 한 페이지에 표시할 게시글 수
            
            if (request.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(request.getParameter("page"));
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 기본값 사용
                }
            }
            
            // 게시판 종류 필터링
            String boardType = request.getParameter("boardType");
            if (boardType == null) {
                boardType = "all"; // 기본값: 모든 게시판
            }
            
            // 내가 쓴 글 목록 조회
            List<PostDTO> posts = boardService.getPostsByUserId(userId, boardType, page, pageSize);
            int totalPosts = boardService.getTotalPostCountByUserId(userId, boardType);
            
            // 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
            
            // 게시판 종류 목록
            List<String> boardTypes = boardService.getBoardTypes();
            
            // 요청 속성 설정
            request.setAttribute("user", user);
            request.setAttribute("posts", posts);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("selectedBoardType", boardType);
            request.setAttribute("boardTypes", boardTypes);
            request.setAttribute("activeTab", "posts");
            
            // 마이페이지로 포워딩
            request.getRequestDispatcher("/view/pages/mypage.html").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 내 별점 내역 확인
     */
    private void getAllMyKeyboardScores(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");
            long userId = user.getUserUid();
            
            // 페이징 처리
            int page = 1;
            int pageSize = 10; // 한 페이지에 표시할 별점 수
            
            if (request.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(request.getParameter("page"));
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 기본값 사용
                }
            }
            
            // 정렬 기준
            String sortBy = request.getParameter("sortBy");
            if (sortBy == null) {
                sortBy = "date"; // 기본값: 날짜순
            }
            
            // 별점 내역 조회
            List<KeyboardScoreDTO> scores = keyboardInfoService.getScoresByUserId(userId, sortBy, page, pageSize);
            int totalScores = keyboardInfoService.getTotalScoreCountByUserId(userId);
            
            // 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalScores / pageSize);
            
            // 요청 속성 설정
            request.setAttribute("user", user);
            request.setAttribute("scores", scores);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("activeTab", "scores");
            
            // 마이페이지로 포워딩
            request.getRequestDispatcher("/view/pages/mypage.html").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * AJAX 요청인지 확인
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": " + success + ", \"message\": \"" + message + "\"}");
    }
}