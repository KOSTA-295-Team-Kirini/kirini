package repository.dao.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dto.board.FreeboardDTO;
import util.db.DBConnectionUtil;

// 예시 커스텀 예외 클래스
public class FreeboardException extends Exception {
    private String code;
    
    public FreeboardException(String message) {
        super(message);
    }
    
    public FreeboardException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}

// 유틸리티 클래스 추가
public class SecurityUtil {
    public static String escapeXSS(String value) {
        if (value == null) return "";
        
        return value.replaceAll("&", "&amp;")
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("/", "&#x2F;");
    }
}

public class FreeboardDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;

    // 공지사항 목록을 위한 캐싱 메서드
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY = 5 * 60 * 1000; // 5분

    public List<FreeboardDTO> getNoticeList() throws SQLException {
        String cacheKey = "notice_list";
        CacheItem cacheItem = (CacheItem) cache.get(cacheKey);
        
        if (cacheItem != null && !cacheItem.isExpired()) {
            return (List<FreeboardDTO>) cacheItem.getData();
        }
        
        // 캐시에 없으면 DB에서 조회
        List<FreeboardDTO> noticeList = new ArrayList<>();
        String sql = "SELECT f.*, u.user_name FROM freeboard f " +
                    "JOIN user u ON f.user_uid = u.user_uid " +
                    "WHERE f.freeboard_deleted = 'maintained' AND f.freeboard_notify = 'notification' " +
                    "ORDER BY f.freeboard_writetime DESC";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FreeboardDTO post = createFreeboardFromResultSet(rs);
                post.setUserName(rs.getString("user_name"));
                noticeList.add(post);
            }
        } finally {
            closeResources();
        }
        
        // 결과를 캐시에 저장
        cache.put(cacheKey, new CacheItem(noticeList, CACHE_EXPIRY));
        return noticeList;
    }
    
    // DB 연결 가져오기
    private Connection getConnection() throws SQLException {
        return DBConnectionUtil.getConnection();
    }
    
    // 자원 해제 메서드
    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // ResultSet에서 DTO 객체 생성 유틸리티 메서드
    private FreeboardDTO createFreeboardFromResultSet(ResultSet rs) throws SQLException {
        FreeboardDTO freeboard = new FreeboardDTO();
        
        freeboard.setFreeboardUid(rs.getLong("freeboard_uid"));
        freeboard.setFreeboardTitle(rs.getString("freeboard_title"));
        freeboard.setFreeboardContents(rs.getString("freeboard_contents"));
        freeboard.setFreeboardRead(rs.getInt("freeboard_read"));
        freeboard.setFreeboardRecommend(rs.getInt("freeboard_recommend"));
        
        Timestamp writetime = rs.getTimestamp("freeboard_writetime");
        if (writetime != null) {
            freeboard.setFreeboardWritetime(writetime.toLocalDateTime());
        }
        
        Timestamp modifyTime = rs.getTimestamp("freeboard_modify_time");
        if (modifyTime != null) {
            freeboard.setFreeboardModifyTime(modifyTime.toLocalDateTime());
        }
        
        freeboard.setFreeboardAuthorIp(rs.getString("freeboard_author_ip"));
        freeboard.setFreeboardNotify(rs.getString("freeboard_notify"));
        freeboard.setFreeboardDeleted(rs.getString("freeboard_deleted"));
        freeboard.setUserUid(rs.getLong("user_uid"));
        
        return freeboard;
    }
    
    // 게시글 등록
    public boolean postFreeboard(FreeboardDTO post) throws SQLException {
        // XSS 방지 처리
        post.setFreeboardTitle(SecurityUtil.escapeXSS(post.getFreeboardTitle()));
        post.setFreeboardContents(SecurityUtil.escapeXSS(post.getFreeboardContents()));
        
        String sql = "INSERT INTO freeboard (freeboard_title, freeboard_contents, freeboard_read, " + 
                     "freeboard_recommend, freeboard_writetime, freeboard_author_ip, " +
                     "freeboard_notify, freeboard_deleted, user_uid) " +
                     "VALUES (?, ?, 0, 0, NOW(), ?, ?, ?, ?)";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, post.getFreeboardTitle());
            pstmt.setString(2, post.getFreeboardContents());
            pstmt.setString(3, post.getFreeboardAuthorIp());
            pstmt.setString(4, post.getFreeboardNotify());
            pstmt.setString(5, post.getFreeboardDeleted());
            pstmt.setLong(6, post.getUserUid());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 생성된 기본 키(ID) 가져오기
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        post.setFreeboardUid(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } finally {
            closeResources();
        }
    }
    
    // 모든 게시글 조회 (페이징 포함)
    public List<FreeboardDTO> getAllFreeboards(int page, int pageSize) throws SQLException {
        List<FreeboardDTO> freeboard = new ArrayList<>();
        String sql = "SELECT f.*, u.user_name, " +
                    "(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count " +
                    "FROM freeboard f " +
                    "JOIN user u ON f.user_uid = u.user_uid " +
                    "WHERE f.freeboard_deleted = 'maintained' " +
                    "ORDER BY f.freeboard_notify DESC, f.freeboard_writetime DESC " +
                    "LIMIT ? OFFSET ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (page - 1) * pageSize);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FreeboardDTO post = createFreeboardFromResultSet(rs);
                // 추가 정보 설정
                post.setUserName(rs.getString("user_name"));
                post.setCommentCount(rs.getInt("comment_count"));
                freeboard.add(post);
            }
            
            return freeboard;
        } finally {
            closeResources();
        }
    }
    
    // 기본 모든 게시글 조회 (페이징 없음 - 오버로딩)
    public List<FreeboardDTO> getAllFreeboards() throws SQLException {
        return getAllFreeboards(1, 100); // 기본값으로 첫 페이지, 100개 항목
    }
    
    // ID로 게시글 조회
    public FreeboardDTO getFreeboardById(long postId) throws SQLException {
        FreeboardDTO post = null;
        String sql = "SELECT f.*, u.user_name, " + 
                    "(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count " +
                    "FROM freeboard f " +
                    "JOIN user u ON f.user_uid = u.user_uid " +
                    "WHERE f.freeboard_uid = ? AND f.freeboard_deleted = 'maintained'";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                post = createFreeboardFromResultSet(rs);
                post.setUserName(rs.getString("user_name"));
                post.setCommentCount(rs.getInt("comment_count"));
                
                // 조회수 증가
                updateReadCount(postId);
            }
            
            return post;
        } finally {
            closeResources();
        }
    }
    
    // 조회수 증가
    private void updateReadCount(long postId) throws SQLException {
        String sql = "UPDATE freeboard SET freeboard_read = freeboard_read + 1 WHERE freeboard_uid = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.executeUpdate();
        }
    }
    
    // 게시글 수정
    public boolean updateFreeboardById(FreeboardDTO post) throws SQLException {
        String sql = "UPDATE freeboard SET freeboard_title = ?, freeboard_contents = ?, " +
                    "freeboard_modify_time = NOW() WHERE freeboard_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, post.getFreeboardTitle());
            pstmt.setString(2, post.getFreeboardContents());
            pstmt.setLong(3, post.getFreeboardUid());
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    // 게시글 삭제 (소프트 삭제)
    public boolean deleteFreeboardById(long postId) throws SQLException {
        String sql = "UPDATE freeboard SET freeboard_deleted = 'deleted' WHERE freeboard_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    // 게시글 숨김 처리
    public boolean hideFreeboardById(long postId, String hideReason) throws SQLException {
        // 실제로는 freeboard_deleted를 'hidden'으로 설정하고 이유를 로그 테이블에 기록
        String sql = "UPDATE freeboard SET freeboard_deleted = 'deleted' WHERE freeboard_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 로그 테이블에 숨김 이유 기록 (log_delete_post 테이블 활용)
                logPostDeletion(postId, hideReason);
                return true;
            }
            return false;
        } finally {
            closeResources();
        }
    }
    
    // 게시글 삭제 로그 기록
    private void logPostDeletion(long postId, String reason) throws SQLException {
        String sql = "INSERT INTO log_delete_post (delete_target_type, delete_target_id, " +
                    "delete_reason, delete_admin_id, delete_date) " +
                    "VALUES ('freeboard', ?, ?, ?, NOW())";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            pstmt.setString(2, reason);
            // 현재 세션의 관리자 ID 가져오기 (컨텍스트에서 가져오거나 파라미터로 받아야 함)
            pstmt.setLong(3, 0); // 임시 값, 실제 구현에서는 관리자 ID 설정
            
            pstmt.executeUpdate();
        } finally {
            closeResources();
        }
    }
    
    // 공지사항 지정/해제
    public boolean setNoticeById(long postId, boolean isNotice) throws SQLException {
        String notifyValue = isNotice ? "notification" : "common";
        String sql = "UPDATE freeboard SET freeboard_notify = ? WHERE freeboard_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, notifyValue);
            pstmt.setLong(2, postId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    // 총 게시물 수 조회 (페이징용)
    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM freeboard WHERE freeboard_deleted = 'maintained'";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 게시글 신고
     */
    public boolean reportFreeboard(long postId, long reporterId, String reason, String category) 
            throws SQLException, FreeboardException {
        // 게시글 존재 여부 확인
        FreeboardDTO post = getFreeboardById(postId);
        if (post == null) {
            throw new FreeboardException("ERR_POST_NOT_FOUND", "신고하려는 게시글이 존재하지 않습니다.");
        }
        
        // 중복 신고 확인
        String checkSql = "SELECT COUNT(*) FROM report WHERE report_target_type = 'freeboard' " +
                         "AND report_target_id = ? AND report_user_id = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setLong(1, postId);
            pstmt.setLong(2, reporterId);
            
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new FreeboardException("ERR_ALREADY_REPORTED", "이미 신고한 게시글입니다.");
            }
            
            // 기존 신고 로직 수행
            String sql = "INSERT INTO report (report_target_type, report_target_id, report_user_id, " +
                         "report_reason, report_category, report_date, report_status) " +
                         "VALUES ('freeboard', ?, ?, ?, ?, NOW(), 'pending')";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, postId);
            pstmt.setLong(2, reporterId);
            pstmt.setString(3, reason);
            pstmt.setString(4, category);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 이용자 신고
     */
    public boolean reportUser(long targetUserId, long reporterId, String reason, String category) throws SQLException {
        String sql = "INSERT INTO report (report_target_type, report_target_id, report_user_id, " +
                     "report_reason, report_category, report_date, report_status) " +
                     "VALUES ('user', ?, ?, ?, ?, NOW(), 'pending')";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, targetUserId);
            pstmt.setLong(2, reporterId);
            pstmt.setString(3, reason);
            pstmt.setString(4, category);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 이용자 제재
     */
    public boolean penalizeUser(long targetUserId, long adminId, String reason, 
                               String category, String penaltyType, int duration) throws SQLException {
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 1. 신고 기록 추가
            String reportSql = "INSERT INTO report (report_target_type, report_target_id, report_user_id, " +
                     "report_reason, report_category, report_date, report_status) " +
                     "VALUES ('user', ?, ?, ?, ?, NOW(), 'pending')";
            
            pstmt = conn.prepareStatement(reportSql);
            pstmt.setLong(1, targetUserId);
            pstmt.setLong(2, adminId);
            pstmt.setString(3, reason);
            pstmt.setString(4, category);
            
            boolean reportResult = pstmt.executeUpdate() > 0;
            
            if (!reportResult) {
                conn.rollback();
                return false;
            }
            
            // 2. 제재 정보 추가
            String penaltySql = "INSERT INTO user_penalty (user_uid, penalty_type, penalty_reason, " +
                                "penalty_start_date, penalty_end_date, penalty_admin_id) " +
                                "VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? DAY), ?)";
            
            pstmt = conn.prepareStatement(penaltySql);
            pstmt.setLong(1, targetUserId);
            pstmt.setString(2, penaltyType);
            pstmt.setString(3, reason);
            pstmt.setInt(4, duration);
            pstmt.setLong(5, adminId);
            
            boolean penaltyResult = pstmt.executeUpdate() > 0;
            
            if (!penaltyResult) {
                conn.rollback();
                return false;
            }
            
            // 3. 사용자 상태 업데이트
            String statusSql = "UPDATE user SET user_status = ? WHERE user_uid = ?";
            
            pstmt = conn.prepareStatement(statusSql);
            pstmt.setString(1, "restricted");
            pstmt.setLong(2, targetUserId);
            
            boolean statusResult = pstmt.executeUpdate() > 0;
            
            if (!statusResult) {
                conn.rollback();
                return false;
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeResources();
        }
    }
    
    /**
     * 사용자 상태 업데이트 (제재 적용)
     */
    private boolean updateUserStatus(long userId, String status) throws SQLException {
        String sql = "UPDATE user SET user_status = ? WHERE user_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, userId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 첨부파일 삭제
     */
    public boolean deleteAttachByFilename(long postId, String filename, String reason, long adminId) throws SQLException {
        // 1. 로그 테이블에 삭제 이유 기록
        String logSql = "INSERT INTO log_delete_post (delete_target_type, delete_target_id, " +
                       "delete_file_name, delete_reason, delete_admin_id, delete_date) " +
                       "VALUES ('freeboard_attach', ?, ?, ?, ?, NOW())";
        
        // 2. 첨부파일 테이블에서 파일 정보 삭제
        String deleteSql = "DELETE FROM freeboard_attach WHERE freeboard_uid = ? AND file_name = ?";
        
        boolean logSuccess = false;
        boolean deleteSuccess = false;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 로그 기록
            pstmt = conn.prepareStatement(logSql);
            pstmt.setLong(1, postId);
            pstmt.setString(2, filename);
            pstmt.setString(3, reason);
            pstmt.setLong(4, adminId);
            
            logSuccess = pstmt.executeUpdate() > 0;
            
            // 파일 정보 삭제
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setLong(1, postId);
            pstmt.setString(2, filename);
            
            deleteSuccess = pstmt.executeUpdate() > 0;
            
            if (logSuccess && deleteSuccess) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            closeResources();
        }
    }
    
    /**
     * 게시글 검색
     */
    public List<FreeboardDTO> searchFreeboards(String keyword, String searchType, int page, int pageSize) throws SQLException {
        List<FreeboardDTO> searchResults = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        
        sql.append("SELECT f.*, u.user_name, ");
        sql.append("(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count ");
        sql.append("FROM freeboard f ");
        sql.append("JOIN user u ON f.user_uid = u.user_uid ");
        sql.append("WHERE f.freeboard_deleted = 'maintained' ");
        
        // 검색 조건 추가
        if (searchType.equals("title")) {
            sql.append("AND f.freeboard_title LIKE ? ");
        } else if (searchType.equals("content")) {
            sql.append("AND f.freeboard_contents LIKE ? ");
        } else if (searchType.equals("author")) {
            sql.append("AND u.user_name LIKE ? ");
        } else {
            sql.append("AND (f.freeboard_title LIKE ? OR f.freeboard_contents LIKE ?) ");
        }
        
        sql.append("ORDER BY f.freeboard_notify DESC, f.freeboard_writetime DESC ");
        sql.append("LIMIT ? OFFSET ?");
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            if (searchType.equals("title") || searchType.equals("content") || searchType.equals("author")) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setInt(2, pageSize);
                pstmt.setInt(3, (page - 1) * pageSize);
            } else {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
                pstmt.setInt(3, pageSize);
                pstmt.setInt(4, (page - 1) * pageSize);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FreeboardDTO post = createFreeboardFromResultSet(rs);
                post.setUserName(rs.getString("user_name"));
                post.setCommentCount(rs.getInt("comment_count"));
                searchResults.add(post);
            }
            
            return searchResults;
        } finally {
            closeResources();
        }
    }
    
    // 새로운 커서 기반 페이징 메서드 추가
    public List<FreeboardDTO> getNextFreeboards(long lastPostId, int pageSize) throws SQLException {
        List<FreeboardDTO> freeboard = new ArrayList<>();
        String sql = "SELECT f.*, u.user_name, " +
                    "(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count " +
                    "FROM freeboard f " +
                    "JOIN user u ON f.user_uid = u.user_uid " +
                    "WHERE f.freeboard_deleted = 'maintained' " +
                    "AND f.freeboard_uid < ? " +  // 커서 조건
                    "ORDER BY f.freeboard_uid DESC " +
                    "LIMIT ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, lastPostId);  // 마지막으로 본 게시글 ID
            pstmt.setInt(2, pageSize);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FreeboardDTO post = createFreeboardFromResultSet(rs);
                post.setUserName(rs.getString("user_name"));
                post.setCommentCount(rs.getInt("comment_count"));
                freeboard.add(post);
            }
            
            return freeboard;
        } finally {
            closeResources();
        }
    }
    
    // 간단한 목록 조회용 메서드 추가 (작성자 이름 없이)
    public List<FreeboardDTO> getSimpleFreeboardList(int page, int pageSize) throws SQLException {
        List<FreeboardDTO> freeboard = new ArrayList<>();
        String sql = "SELECT f.* FROM freeboard f " +
                    "WHERE f.freeboard_deleted = 'maintained' " +
                    "ORDER BY f.freeboard_notify DESC, f.freeboard_writetime DESC " +
                    "LIMIT ? OFFSET ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (page - 1) * pageSize);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FreeboardDTO post = createFreeboardFromResultSet(rs);
                // 작성자 정보 조회 없이 기본 데이터만 설정
                freeboard.add(post);
            }
            
            return freeboard;
        } finally {
            closeResources();
        }
    }
}