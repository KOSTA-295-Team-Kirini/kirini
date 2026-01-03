package business.service.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dto.board.PostDTO;
import repository.dao.board.FreeboardDAO;
import util.db.DBConnectionUtil;

public class BoardService {
    private FreeboardDAO freeboardDAO;

    public BoardService() {
        this.freeboardDAO = new FreeboardDAO();
    }

    /**
     * 사용자 ID로 게시글 목록 조회
     * 여러 게시판 DAO를 활용하여 PostDTO 형태로 변환하여 반환
     */
    public List<PostDTO> getPostsByUserId(long userId, String boardType, int page, int pageSize) throws SQLException {
        List<PostDTO> results = new ArrayList<>();

        // 페이지네이션을 위한 오프셋 계산
        int offset = (page - 1) * pageSize;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();

            String sql;
            if ("all".equals(boardType)) {
                // 모든 게시판의 게시글 조회 (freeboard, news, notice, inquiry, chatboard 포함)
                sql = "SELECT p.post_id, p.board_type, p.title, p.content, p.read_count, p.recommend_count, " +
                        "p.write_time, p.modify_time, p.status, p.user_id " +
                        "FROM (" +
                        "   SELECT freeboard_uid AS post_id, 'freeboard' AS board_type, freeboard_title AS title, freeboard_contents AS content, " +
                        "   freeboard_read AS read_count, freeboard_recommend AS recommend_count, freeboard_writetime AS write_time, " +
                        "   freeboard_modify_time AS modify_time, freeboard_deleted AS status, user_uid AS user_id " +
                        "   FROM freeboard WHERE user_uid = ? AND freeboard_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT news_uid AS post_id, 'news' AS board_type, news_title AS title, news_contents AS content, " +
                        "   news_read AS read_count, news_recommend AS recommend_count, news_writetime AS write_time, " +
                        "   news_modify_time AS modify_time, news_deleted AS status, user_uid AS user_id " +
                        "   FROM news WHERE user_uid = ? AND news_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT notice_uid AS post_id, 'notice' AS board_type, notice_title AS title, notice_contents AS content, " +
                        "   notice_read AS read_count, notice_recommend AS recommend_count, notice_writetime AS write_time, " +
                        "   notice_modify_time AS modify_time, notice_deleted AS status, user_uid AS user_id " +
                        "   FROM notice WHERE user_uid = ? AND notice_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT inquiry_uid AS post_id, 'inquiry' AS board_type, inquiry_title AS title, inquiry_contents AS content, " +
                        "   0 AS read_count, 0 AS recommend_count, inquiry_writetime AS write_time, " + // inquiry에는 read_count, recommend_count 없음
                        "   inquiry_modify_time AS modify_time, inquiry_deleted AS status, user_uid AS user_id " +
                        "   FROM inquiry WHERE user_uid = ? AND inquiry_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT chatboard_uid AS post_id, 'chatboard' AS board_type, chatboard_title AS title, NULL AS content, " + // chatboard에는 content 없음
                        "   0 AS read_count, 0 AS recommend_count, chatboard_writetime AS write_time, " + // chatboard에는 read_count, recommend_count 없음
                        "   chatboard_modify_time AS modify_time, chatboard_deleted AS status, user_uid AS user_id " +
                        "   FROM chatboard WHERE user_uid = ? AND chatboard_deleted <> 'deleted' " +
                        ") p " +
                        "ORDER BY p.write_time DESC " +
                        "LIMIT ? OFFSET ?";

                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, userId);
                pstmt.setLong(2, userId);
                pstmt.setLong(3, userId);
                pstmt.setLong(4, userId); // for inquiry
                pstmt.setLong(5, userId); // for chatboard
                pstmt.setInt(6, pageSize);
                pstmt.setInt(7, offset);
            } else {
                // 특정 게시판의 게시글만 조회 (동적 컬럼명 사용)
                String tableName = boardType;
                String idColumn, titleColumn, contentColumnForSelect, readCountColumnForSelect, recommendCountColumnForSelect;
                String writeTimeColumn, modifyTimeColumn, deletedColumnName;

                switch (boardType) {
                    case "freeboard":
                        idColumn = "freeboard_uid";
                        titleColumn = "freeboard_title";
                        contentColumnForSelect = "freeboard_contents";
                        readCountColumnForSelect = "freeboard_read";
                        recommendCountColumnForSelect = "freeboard_recommend";
                        writeTimeColumn = "freeboard_writetime";
                        modifyTimeColumn = "freeboard_modify_time";
                        deletedColumnName = "freeboard_deleted";
                        break;
                    case "news":
                        idColumn = "news_uid";
                        titleColumn = "news_title";
                        contentColumnForSelect = "news_contents";
                        readCountColumnForSelect = "news_read";
                        recommendCountColumnForSelect = "news_recommend";
                        writeTimeColumn = "news_writetime";
                        modifyTimeColumn = "news_modify_time";
                        deletedColumnName = "news_deleted";
                        break;
                    case "notice":
                        idColumn = "notice_uid";
                        titleColumn = "notice_title";
                        contentColumnForSelect = "notice_contents";
                        readCountColumnForSelect = "notice_read";
                        recommendCountColumnForSelect = "notice_recommend";
                        writeTimeColumn = "notice_writetime";
                        modifyTimeColumn = "notice_modify_time";
                        deletedColumnName = "notice_deleted";
                        break;
                    case "inquiry":
                        idColumn = "inquiry_uid";
                        titleColumn = "inquiry_title";
                        contentColumnForSelect = "inquiry_contents";
                        readCountColumnForSelect = "0";
                        recommendCountColumnForSelect = "0"; // Literal values
                        writeTimeColumn = "inquiry_writetime";
                        modifyTimeColumn = "inquiry_modify_time";
                        deletedColumnName = "inquiry_deleted";
                        break;
                    case "chatboard":
                        idColumn = "chatboard_uid";
                        titleColumn = "chatboard_title";
                        contentColumnForSelect = "NULL"; // Literal NULL
                        readCountColumnForSelect = "0";
                        recommendCountColumnForSelect = "0"; // Literal values
                        writeTimeColumn = "chatboard_writetime";
                        modifyTimeColumn = "chatboard_modify_time";
                        deletedColumnName = "chatboard_deleted";
                        break;
                    default:
                        throw new SQLException("Unsupported board type: " + boardType);
                }

                sql = String.format(
                        "SELECT %s AS post_id, ? AS board_type, %s AS title, %s AS content, " +
                                "%s AS read_count, %s AS recommend_count, %s AS write_time, " +
                                "%s AS modify_time, %s AS status, user_uid AS user_id " +
                                "FROM %s WHERE user_uid = ? AND %s <> 'deleted' " +
                                "ORDER BY %s DESC LIMIT ? OFFSET ?",
                        idColumn, titleColumn, contentColumnForSelect,
                        readCountColumnForSelect, recommendCountColumnForSelect, writeTimeColumn,
                        modifyTimeColumn, deletedColumnName,
                        tableName, deletedColumnName, writeTimeColumn
                );

                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, boardType);
                pstmt.setLong(2, userId);
                pstmt.setInt(3, pageSize);
                pstmt.setInt(4, offset);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                PostDTO post = new PostDTO();
                post.setPostId(rs.getLong("post_id"));
                post.setBoardType(rs.getString("board_type"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setReadCount(rs.getInt("read_count"));
                post.setRecommendCount(rs.getInt("recommend_count"));

                // Timestamp를 LocalDateTime으로 변환
                Timestamp writeTime = rs.getTimestamp("write_time");
                if (writeTime != null) {
                    post.setWriteTime(writeTime.toLocalDateTime());
                }

                Timestamp modifyTime = rs.getTimestamp("modify_time");
                if (modifyTime != null) {
                    post.setModifyTime(modifyTime.toLocalDateTime());
                }

                post.setStatus(rs.getString("status"));
                post.setUserId(rs.getLong("user_id"));

                results.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("사용자의 게시글을 조회하는 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 리소스 해제
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    /**
     * 사용자 ID로 게시글 총 개수 조회
     */
    public int getTotalPostCountByUserId(long userId, String boardType) throws SQLException {
        int totalCount = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();

            String sql;
            if ("all".equals(boardType)) {
                // 모든 게시판의 게시글 수 조회 (freeboard, news, notice, inquiry, chatboard 포함)
                sql = "SELECT COUNT(*) AS total FROM (" +
                        "   SELECT freeboard_uid FROM freeboard WHERE user_uid = ? AND freeboard_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT news_uid FROM news WHERE user_uid = ? AND news_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT notice_uid FROM notice WHERE user_uid = ? AND notice_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT inquiry_uid FROM inquiry WHERE user_uid = ? AND inquiry_deleted <> 'deleted' " +
                        "   UNION ALL " +
                        "   SELECT chatboard_uid FROM chatboard WHERE user_uid = ? AND chatboard_deleted <> 'deleted' " +
                        ") p";

                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, userId);
                pstmt.setLong(2, userId);
                pstmt.setLong(3, userId);
                pstmt.setLong(4, userId); // for inquiry
                pstmt.setLong(5, userId); // for chatboard
            } else {
                // 특정 게시판의 게시글 수만 조회 (동적 컬럼명 사용)
                String tableName = boardType;
                String idColumn; // UID 컬럼
                String deletedColumnName; // 삭제 상태 컬럼

                switch (boardType) {
                    case "freeboard":
                        idColumn = "freeboard_uid";
                        deletedColumnName = "freeboard_deleted";
                        break;
                    case "news":
                        idColumn = "news_uid";
                        deletedColumnName = "news_deleted";
                        break;
                    case "notice":
                        idColumn = "notice_uid";
                        deletedColumnName = "notice_deleted";
                        break;
                    case "inquiry":
                        idColumn = "inquiry_uid";
                        deletedColumnName = "inquiry_deleted";
                        break;
                    case "chatboard":
                        idColumn = "chatboard_uid";
                        deletedColumnName = "chatboard_deleted";
                        break;
                    default:
                        throw new SQLException("Unsupported board type: " + boardType);
                }
                // COUNT(idColumn) 또는 COUNT(*) 사용 가능. 여기서는 idColumn을 명시.
                sql = String.format("SELECT COUNT(%s) AS total FROM %s WHERE user_uid = ? AND %s <> 'deleted'",
                        idColumn, tableName, deletedColumnName);

                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, userId);
            }

            rs = pstmt.executeQuery();

            if (rs.next()) {
                totalCount = rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("사용자의 게시글 수를 조회하는 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 리소스 해제
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return totalCount;
    }

    /**
     * 게시판 종류 목록 조회
     */
    public List<String> getBoardTypes() {
        List<String> boardTypes = new ArrayList<>();
        boardTypes.add("freeboard");
        boardTypes.add("news");
        boardTypes.add("notice");
        boardTypes.add("inquiry");
        boardTypes.add("chatboard"); // chatboard 추가
        return boardTypes;
    }
}