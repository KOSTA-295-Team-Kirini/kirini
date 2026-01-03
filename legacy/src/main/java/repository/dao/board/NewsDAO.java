package repository.dao.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import dto.board.NewsDTO;
import dto.board.NewsCommentDTO;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

/**
 * 키보드 소식 게시판 DAO 클래스
 */
public class NewsDAO {
    // 클래스 레벨 멤버 변수 제거 - 각 메서드에서 로컬 변수로 사용
    // private Connection conn = null;
    // private PreparedStatement pstmt = null;
    // private ResultSet rs = null;

    private static final Logger logger = LoggerConfig.getLogger(NewsDAO.class);

    // DB 연결 가져오기
    private Connection getConnection() throws SQLException {
        return DBConnectionUtil.getConnection();
    }

    // 자원 해제 메서드 - 더 이상 사용하지 않음 (각 메서드에서 직접 리소스를 해제)
    @Deprecated
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ResultSet에서 DTO 객체 생성 유틸리티 메서드
    private NewsDTO createNewsFromResultSet(ResultSet rs) throws SQLException {
        NewsDTO news = new NewsDTO();

        news.setNewsId(rs.getLong("news_uid"));
        news.setNewsTitle(rs.getString("news_title"));
        news.setNewsContents(rs.getString("news_contents"));
        news.setNewsRead(rs.getInt("news_read"));
        news.setNewsRecommend(rs.getInt("news_recommend"));

        Timestamp writetime = rs.getTimestamp("news_writetime");
        if (writetime != null) {
            news.setNewsWritetime(writetime.toLocalDateTime());
        }

        Timestamp modifyTime = rs.getTimestamp("news_modify_time");
        if (modifyTime != null) {
            news.setNewsModifyTime(modifyTime.toLocalDateTime());
        }

        news.setNewsAuthorIp(rs.getString("news_author_ip"));
        // news_notify 컬럼이 테이블에 없어서 기본값 설정
        news.setNewsNotify("common");
        news.setNewsDeleted(rs.getString("news_deleted"));
        news.setUserId(rs.getLong("user_uid"));

        return news;
    }

    /**
     * 소식 게시글 등록
     */
    public boolean postNews(NewsDTO news) throws SQLException {
        String sql = "INSERT INTO news (news_title, news_contents, news_read, " +
                "news_recommend, news_writetime, news_author_ip, " +
                "news_deleted, user_uid) " +
                "VALUES (?, ?, 0, 0, NOW(), ?, 'maintained', ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, news.getNewsTitle());
            pstmt.setString(2, news.getNewsContents());
            pstmt.setString(3, news.getNewsAuthorIp());
            pstmt.setLong(4, news.getUserId());

            int result = pstmt.executeUpdate();
            boolean success = result > 0;

            if (success) {
                // 생성된 게시글 ID 가져오기
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        news.setNewsId(generatedKeys.getLong(1));
                    }
                }
                conn.commit();
                logger.info("소식 게시글 등록 성공: ID=" + news.getNewsId() + ", 작성자=" + news.getUserId());
            } else {
                conn.rollback();
                logger.warning("소식 게시글 등록 실패: 영향받은 행 없음");
            }

            return success;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("롤백 중 오류 발생: " + ex.getMessage());
                }
            }

            String errorCode = "DB_ERROR_" + System.currentTimeMillis();
            logger.severe("소식 게시글 등록 중 오류 발생 [" + errorCode + "]: " + e.getMessage());
            logger.severe("SQL 상태: " + e.getSQLState() + ", 에러코드: " + e.getErrorCode());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.severe("AutoCommit 설정 복구 중 오류: " + e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 모든 키보드 소식 조회 (페이징 처리)
     */
    public List<NewsDTO> getAllNews(int page, int pageSize) throws SQLException {
        List<NewsDTO> newsList = new ArrayList<>();
        String sql = "SELECT n.*, u.user_name, " +
                "(SELECT COUNT(*) FROM news_comment nc WHERE nc.news_uid = n.news_uid) AS comment_count " +
                "FROM news n " +
                "JOIN user u ON n.user_uid = u.user_uid " +
                "WHERE n.news_deleted = 'maintained' " +
                "ORDER BY n.news_writetime DESC " +
                "LIMIT ? OFFSET ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (page - 1) * pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                NewsDTO news = createNewsFromResultSet(rs);
                // 추가 정보 설정
                news.setUserName(rs.getString("user_name"));
                news.setCommentCount(rs.getInt("comment_count"));
                newsList.add(news);
            }

            return newsList;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 기본 모든 소식 조회 (페이징 없음 - 오버로딩)
     */
    public List<NewsDTO> getAllNews() throws SQLException {
        return getAllNews(1, 20); // 기본값으로 첫 페이지, 20개 항목
    }

    /**
     * ID로 소식 조회
     */
    public NewsDTO getNewsById(long newsId) throws SQLException {
        NewsDTO news = null;
        String sql = "SELECT n.*, u.user_name, " +
                "(SELECT COUNT(*) FROM news_comment nc WHERE nc.news_uid = n.news_uid) AS comment_count " +
                "FROM news n " +
                "JOIN user u ON n.user_uid = u.user_uid " +
                "WHERE n.news_uid = ? AND n.news_deleted = 'maintained'";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                news = createNewsFromResultSet(rs);
                news.setUserName(rs.getString("user_name"));
                news.setCommentCount(rs.getInt("comment_count"));
            }

            // 리소스를 닫기 전에 조회수 증가
            if (news != null) {
                // 별도의 connection에서 실행
                updateReadCount(newsId);
            }

            return news;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 조회수 증가
     */
    private void updateReadCount(long newsId) throws SQLException {
        String sql = "UPDATE news SET news_read = news_read + 1 WHERE news_uid = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, newsId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 조회수 증가 (세션 기반 중복 방지)
     *
     * @param newsId  뉴스 ID
     * @param request HTTP 요청 객체
     * @throws SQLException SQL 예외 발생 시
     */
    public void updateReadCount(long newsId, jakarta.servlet.http.HttpServletRequest request) throws SQLException {
        // 세션이 없으면 그냥 조회수 증가
        if (request == null) {
            System.out.println("Request 객체가 null이어서 단순 조회수 증가 처리: " + newsId);
            updateReadCount(newsId);
            return;
        }

        // increaseReadCount 파라미터 체크 (false인 경우 증가 안 함)
        String increaseParam = request.getParameter("increaseReadCount");
        if (increaseParam != null && "false".equalsIgnoreCase(increaseParam)) {
            System.out.println("increaseReadCount=false 파라미터로 조회수 증가 생략: " + newsId);
            return;
        }

        jakarta.servlet.http.HttpSession session = request.getSession();
        // 세션 타임아웃 설정 (30분)
        session.setMaxInactiveInterval(1800);

        // 세션에서 방문한 게시글 목록 가져오기
        @SuppressWarnings("unchecked")
        java.util.Set<Long> viewedPosts = (java.util.Set<Long>) session.getAttribute("VIEWED_NEWS_POSTS");

        // 세션에 방문 기록이 없으면 새로 생성
        if (viewedPosts == null) {
            viewedPosts = new java.util.HashSet<>();
            session.setAttribute("VIEWED_NEWS_POSTS", viewedPosts);
            System.out.println("새 세션 생성 및 조회 기록 초기화: " + session.getId());
        }

        // 이미 방문한 게시글이면 조회수 증가 생략
        if (viewedPosts.contains(newsId)) {
            System.out.println("이미 조회한 게시글이므로 조회수 증가 생략: " + newsId + " (세션 ID: " + session.getId() + ")");
            return;
        }

        // 방문 기록에 추가
        viewedPosts.add(newsId);
        System.out.println("조회 기록에 게시글 추가 및 조회수 증가: " + newsId + " (세션 ID: " + session.getId() + ")");

        // 조회수 증가 쿼리 실행
        updateReadCount(newsId);
    }

    /**
     * 소식 수정
     */
    public boolean updateNewsById(NewsDTO news) throws SQLException {
        String sql = "UPDATE news SET news_title = ?, news_contents = ?, " +
                "news_modify_time = NOW() WHERE news_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, news.getNewsTitle());
            pstmt.setString(2, news.getNewsContents());
            pstmt.setLong(3, news.getNewsId());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                // 수정 로그 저장
                logModifyPost(news.getNewsId(), news.getUserId(), "news");
            }

            return result > 0;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 소식 삭제 (소프트 삭제)
     */
    public boolean deleteNewsById(long newsId, long userId) throws SQLException {
        String sql = "UPDATE news SET news_deleted = 'deleted' WHERE news_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                // 삭제 로그 저장
                logDeletePost(newsId, userId, "news");
            }

            return result > 0;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 공지사항으로 지정/해제
     */
    public boolean setNoticeById(long newsId, boolean isNotice) throws SQLException {
        // 데이터베이스에 news_notify 컬럼이 없으므로 구현 생략
        // 향후 데이터베이스 스키마 업데이트가 필요함
        logger.warning("news_notify 컬럼이 데이터베이스에 존재하지 않아 공지 기능을 사용할 수 없습니다.");
        return true; // 구현 불가로 항상 성공 반환
    }

    /**
     * 총 소식 수 조회 (페이징용)
     */
    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM news WHERE news_deleted = 'maintained'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 소식 검색
     */
    public List<NewsDTO> searchNewsBy(String keyword, String searchType, int page, int pageSize) throws SQLException {
        List<NewsDTO> searchResults = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT n.*, u.user_name, ");
        sql.append("(SELECT COUNT(*) FROM news_comment nc WHERE nc.news_uid = n.news_uid) AS comment_count ");
        sql.append("FROM news n ");
        sql.append("JOIN user u ON n.user_uid = u.user_uid ");
        sql.append("WHERE n.news_deleted = 'maintained' ");

        // 검색 조건 추가
        if (searchType.equals("title")) {
            sql.append("AND n.news_title LIKE ? ");
        } else if (searchType.equals("content")) {
            sql.append("AND n.news_contents LIKE ? ");
        } else if (searchType.equals("author")) {
            sql.append("AND u.user_name LIKE ? ");
        } else {
            sql.append("AND (n.news_title LIKE ? OR n.news_contents LIKE ?) ");
        }
        sql.append("ORDER BY n.news_writetime DESC ");
        sql.append("LIMIT ? OFFSET ?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

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
                NewsDTO news = createNewsFromResultSet(rs);
                news.setUserName(rs.getString("user_name"));
                news.setCommentCount(rs.getInt("comment_count"));
                searchResults.add(news);
            }

            return searchResults;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 게시글 추천
     */
    public boolean recommendNewsById(long newsId, long userId) throws SQLException {
        // 이미 추천했는지 확인
        if (hasAlreadyRecommended(newsId, userId, "news")) {
            return false;
        }

        String sql = "UPDATE news SET news_recommend = news_recommend + 1 WHERE news_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                // 추천 로그 기록
                logRecommendation(newsId, userId, "news");
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
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 게시글 추천 취소
     */
    public boolean recommendNewsCancelById(long newsId, long userId) throws SQLException {
        // 추천 여부 확인
        if (!hasAlreadyRecommended(newsId, userId, "news")) {
            return false;
        }

        String sql = "UPDATE news SET news_recommend = news_recommend - 1 WHERE news_uid = ? AND news_recommend > 0";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                // 추천 로그 삭제
                deleteRecommendLog(newsId, userId, "news");
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
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 이미 추천했는지 확인
     */
    private boolean hasAlreadyRecommended(long postId, long userId, String boardType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM log_recommend WHERE log_recommend_boardtype = ? AND log_recommend_post_id = ? AND user_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, postId);
            pstmt.setLong(3, userId);

            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 추천 로그 기록
     */
    private void logRecommendation(long postId, long userId, String boardType) throws SQLException {
        String sql = "INSERT INTO log_recommend (log_recommend_boardtype, log_recommend_post_id, log_recommend_date, user_uid) " +
                "VALUES (?, ?, NOW(), ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, postId);
            pstmt.setLong(3, userId);

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 추천 로그 삭제
     */
    private void deleteRecommendLog(long postId, long userId, String boardType) throws SQLException {
        String sql = "DELETE FROM log_recommend WHERE log_recommend_boardtype = ? AND log_recommend_post_id = ? AND user_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, postId);
            pstmt.setLong(3, userId);

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 게시글 수정 로그 저장
     */
    private void logModifyPost(long postId, long userId, String boardType) throws SQLException {
        String sql = "INSERT INTO log_modify_post " +
                "(log_modify_boardtype, log_modify_date, log_modify_post_uid, user_uid) " +
                "VALUES (?, NOW(), ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, postId);
            pstmt.setLong(3, userId);

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 게시글 삭제 로그 저장
     */
    private void logDeletePost(long postId, long userId, String boardType) throws SQLException {
        String sql = "INSERT INTO log_delete_post " +
                "(log_delete_boardtype, log_delete_date, log_deleted_post_uid, user_uid) " +
                "VALUES (?, NOW(), ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, postId);
            pstmt.setLong(3, userId);

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    // -------------------- 댓글 관련 기능 --------------------

    /**
     * 게시글의 모든 댓글 조회
     */
    public List<NewsCommentDTO> getCommentsByNewsId(long newsId) throws SQLException {
        List<NewsCommentDTO> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.user_name FROM news_comment c " +
                "JOIN user u ON c.user_uid = u.user_uid " +
                "WHERE c.news_uid = ? " +
                "ORDER BY c.news_comment_writetime ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                NewsCommentDTO comment = new NewsCommentDTO();
                comment.setNewsCommentId(rs.getLong("news_comment_uid"));
                comment.setNewsCommentContents(rs.getString("news_comment_contents"));
                comment.setNewsCommentWritetime(rs.getTimestamp("news_comment_writetime").toLocalDateTime());

                if (rs.getTimestamp("news_comment_modifytime") != null) {
                    comment.setNewsCommentModifytime(rs.getTimestamp("news_comment_modifytime").toLocalDateTime());
                }

                comment.setNewsCommentAuthorIp(rs.getString("news_comment_author_ip"));
                comment.setNewsId(rs.getLong("news_uid"));
                comment.setUserId(rs.getLong("user_uid"));
                comment.setUserName(rs.getString("user_name"));

                comments.add(comment);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        return comments;
    }

    /**
     * 새 댓글 등록
     */
    public boolean addNewsComment(NewsCommentDTO comment) throws SQLException {
        String sql = "INSERT INTO news_comment " +
                "(news_comment_contents, news_comment_writetime, news_comment_author_ip, news_uid, user_uid) " +
                "VALUES (?, NOW(), ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, comment.getNewsCommentContents());
            pstmt.setString(2, comment.getNewsCommentAuthorIp());
            pstmt.setLong(3, comment.getNewsId());
            pstmt.setLong(4, comment.getUserId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 댓글 수정
     */
    public boolean updateNewsCommentById(NewsCommentDTO comment, boolean isAdmin) throws SQLException {
        String sql;

        if (isAdmin) {
            // 관리자는 다른 사람의 댓글도 수정 가능
            sql = "UPDATE news_comment SET " +
                    "news_comment_contents = ?, " +
                    "news_comment_modifytime = NOW() " +
                    "WHERE news_comment_uid = ?";
        } else {
            // 일반 사용자는 자신의 댓글만 수정 가능
            sql = "UPDATE news_comment SET " +
                    "news_comment_contents = ?, " +
                    "news_comment_modifytime = NOW() " +
                    "WHERE news_comment_uid = ? AND user_uid = ?";
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, comment.getNewsCommentContents());
            pstmt.setLong(2, comment.getNewsCommentId());

            if (!isAdmin) {
                pstmt.setLong(3, comment.getUserId());
            }

            int result = pstmt.executeUpdate();

            // 수정 로그 저장
            if (result > 0) {
                logModifyComment(comment.getNewsCommentId(), comment.getUserId(), "news");
            }

            return result > 0;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 댓글 삭제
     */
    public boolean deleteNewsCommentById(long commentId, long userId, boolean isAdmin) throws SQLException {
        // 관리자 여부 확인을 위해 댓글 정보 조회
        NewsCommentDTO comment = getCommentById(commentId);
        if (comment == null) {
            return false;
        }

        String sql;

        if (isAdmin) {
            // 관리자는 모든 댓글 삭제 가능
            sql = "DELETE FROM news_comment WHERE news_comment_uid = ?";
        } else {
            // 일반 사용자는 자신의 댓글만 삭제 가능
            sql = "DELETE FROM news_comment WHERE news_comment_uid = ? AND user_uid = ?";
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, commentId);

            if (!isAdmin) {
                pstmt.setLong(2, userId);
            }

            int result = pstmt.executeUpdate();

            // 삭제 로그 저장
            if (result > 0) {
                logDeleteComment(commentId, userId, "news");
            }

            return result > 0;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 댓글 상세 조회
     */
    public NewsCommentDTO getCommentById(long commentId) throws SQLException {
        String sql = "SELECT c.*, u.user_name FROM news_comment c " +
                "JOIN user u ON c.user_uid = u.user_uid " +
                "WHERE c.news_comment_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, commentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                NewsCommentDTO comment = new NewsCommentDTO();
                comment.setNewsCommentId(rs.getLong("news_comment_uid"));
                comment.setNewsCommentContents(rs.getString("news_comment_contents"));
                comment.setNewsCommentWritetime(rs.getTimestamp("news_comment_writetime").toLocalDateTime());

                if (rs.getTimestamp("news_comment_modifytime") != null) {
                    comment.setNewsCommentModifytime(rs.getTimestamp("news_comment_modifytime").toLocalDateTime());
                }

                comment.setNewsCommentAuthorIp(rs.getString("news_comment_author_ip"));
                comment.setNewsId(rs.getLong("news_uid"));
                comment.setUserId(rs.getLong("user_uid"));
                comment.setUserName(rs.getString("user_name"));

                return comment;
            }
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 댓글 수정 로그 저장
     */
    private void logModifyComment(long commentId, long userId, String boardType) throws SQLException {
        String sql = "INSERT INTO log_modify_comment " +
                "(log_modify_boardtype, log_modify_date, log_modify_comment_uid, user_uid) " +
                "VALUES (?, NOW(), ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, commentId);
            pstmt.setLong(3, userId);

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 댓글 삭제 로그 저장
     */
    private void logDeleteComment(long commentId, long userId, String boardType) throws SQLException {
        String sql = "INSERT INTO log_delete_comment " +
                "(log_delete_boardtype, log_delete_date, log_deleted_comment_uid, user_uid) " +
                "VALUES (?, NOW(), ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, boardType);
            pstmt.setLong(2, commentId);
            pstmt.setLong(3, userId);

            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 사용자가 특정 게시글을 추천했는지 확인
     */
    public boolean hasUserRecommended(long newsId, long userId) throws SQLException {
        try {
            return hasAlreadyRecommended(newsId, userId, "news");
        } catch (SQLException e) {
            logger.severe("Failed to check user recommendation: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 게시글의 추천 수 조회
     */
    public int getRecommendCount(long newsId) throws SQLException {
        String sql = "SELECT news_recommend FROM news WHERE news_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("news_recommend");
            }

            return 0;
        } catch (SQLException e) {
            logger.severe("Failed to get recommend count: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * 조회수 증가
     */
    public boolean increaseViewCount(long newsId) throws SQLException {
        String sql = "UPDATE news SET news_read = news_read + 1 WHERE news_uid = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, newsId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            logger.severe("Failed to increase view count: " + e.getMessage());
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }
}
