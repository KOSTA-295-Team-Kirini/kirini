package repository.dao.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.keyboard.KeyboardTagDTO;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

/**
 * 키보드 정보 DAO 클래스
 */
public class KeyboardInfoDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;

    private static final Logger logger = LoggerConfig.getLogger(KeyboardInfoDAO.class);

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

    /**
     * 키보드 정보 목록 조회 (페이징 처리)
     */
    public List<KeyboardInfoDTO> getAllKeyboardInfos(int page, int pageSize) throws SQLException {
        List<KeyboardInfoDTO> keyboardList = new ArrayList<>();
        String sql = "SELECT k.*, " +
                "(SELECT AVG(score_value) FROM keyboard_score WHERE keyboard_uid = k.keyboard_uid) AS avg_score " +
                "FROM keyboard_information k " + // keyboard_info -> keyboard_information
                "ORDER BY k.keyboard_name " +
                "LIMIT ? OFFSET ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (page - 1) * pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                KeyboardInfoDTO keyboard = createKeyboardFromResultSet(rs);
                keyboardList.add(keyboard);
            }

            // 각 키보드의 태그 정보 가져오기
            for (KeyboardInfoDTO keyboard : keyboardList) {
                List<String> tags = getKeyboardTags(keyboard.getKeyboardId());
                keyboard.setTags(tags);
            }

            return keyboardList;
        } finally {
            closeResources();
        }
    }

    /**
     * 키보드 ID로 상세 정보 조회
     */
    public KeyboardInfoDTO getKeyboardInfoById(long keyboardId) throws SQLException {
        String sql = "SELECT k.*, " +
                "(SELECT AVG(score_value) FROM keyboard_score WHERE keyboard_uid = k.keyboard_uid) AS avg_score " +
                "FROM keyboard_information k " + // keyboard_info -> keyboard_information
                "WHERE k.keyboard_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                KeyboardInfoDTO keyboard = createKeyboardFromResultSet(rs);

                // 키보드의 태그 정보 가져오기
                List<String> tags = getKeyboardTags(keyboard.getKeyboardId());
                keyboard.setTags(tags);

                return keyboard;
            }

            return null;
        } finally {
            closeResources();
        }
    }

    /**
     * 키보드의 태그 목록 조회
     */
    private List<String> getKeyboardTags(long keyboardId) throws SQLException {
        List<String> tags = new ArrayList<>();
        String sql = "SELECT t.tag_name " +
                "FROM keyboard_tag t " +
                "JOIN keyboard_tag_relation r ON t.tag_uid = r.tag_uid " +
                "WHERE r.keyboard_uid = ? " +
                "ORDER BY r.vote_count DESC, t.tag_name";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, keyboardId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("tag_name"));
                }
            }
        }

        return tags;
    }

    /**
     * ResultSet에서 키보드 정보 객체 생성
     */
    private KeyboardInfoDTO createKeyboardFromResultSet(ResultSet rs) throws SQLException {
        KeyboardInfoDTO keyboard = new KeyboardInfoDTO();

        keyboard.setKeyboardId(rs.getLong("keyboard_uid"));
        keyboard.setName(rs.getString("keyboard_name"));
        keyboard.setManufacturer(rs.getString("keyboard_manufacturer"));
        keyboard.setDescription(rs.getString("keyboard_description"));
        keyboard.setImageUrl(rs.getString("keyboard_image_url"));
        keyboard.setSwitchType(rs.getString("keyboard_switch_type"));
        keyboard.setLayoutType(rs.getString("keyboard_layout_type"));
        keyboard.setConnectType(rs.getString("keyboard_connect_type"));

        // 평균 점수 설정
        double avgScore = rs.getDouble("avg_score");
        keyboard.setAverageScore(avgScore > 0 ? avgScore : 0);

        return keyboard;
    }

    /**
     * 조건별 키보드 검색
     */
    public List<KeyboardInfoDTO> searchKeyboardInfosByCondition(String keyword, String manufacturer,
                                                                String switchType, String layoutType, String connectType, int page, int pageSize) throws SQLException {
        List<KeyboardInfoDTO> searchResults = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT k.*, ");
        sql.append("(SELECT AVG(score_value) FROM keyboard_score WHERE keyboard_uid = k.keyboard_uid) AS avg_score ");
        sql.append("FROM keyboard_information k "); // keyboard_info -> keyboard_information
        sql.append("WHERE 1=1 ");

        // 검색 조건 추가
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (k.keyboard_name LIKE ? OR k.keyboard_description LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            sql.append("AND k.keyboard_manufacturer = ? ");
            params.add(manufacturer);
        }

        if (switchType != null && !switchType.trim().isEmpty()) {
            sql.append("AND k.keyboard_switch_type = ? ");
            params.add(switchType);
        }

        if (layoutType != null && !layoutType.trim().isEmpty()) {
            sql.append("AND k.keyboard_layout_type = ? ");
            params.add(layoutType);
        }

        if (connectType != null && !connectType.trim().isEmpty()) {
            sql.append("AND k.keyboard_connect_type = ? ");
            params.add(connectType);
        }

        sql.append("ORDER BY k.keyboard_name ");
        sql.append("LIMIT ? OFFSET ?");

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            // 파라미터 설정
            int paramIndex = 1;
            for (Object param : params) {
                pstmt.setObject(paramIndex++, param);
            }

            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex, (page - 1) * pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                KeyboardInfoDTO keyboard = createKeyboardFromResultSet(rs);
                searchResults.add(keyboard);
            }

            // 각 키보드의 태그 정보 가져오기
            for (KeyboardInfoDTO keyboard : searchResults) {
                List<String> tags = getKeyboardTags(keyboard.getKeyboardId());
                keyboard.setTags(tags);
            }

            return searchResults;
        } finally {
            closeResources();
        }
    }

    /**
     * 한줄평(코멘트) 추가
     */
    public boolean addKeyboardComment(KeyboardScoreDTO comment) throws SQLException {
        String sql = "INSERT INTO keyboard_score " +
                "(keyboard_uid, user_uid, score_value, score_review, score_created_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, comment.getKeyboardId());
            pstmt.setLong(2, comment.getUserId());
            pstmt.setInt(3, comment.getScoreValue());
            pstmt.setString(4, comment.getReview());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }

    /**
     * 키보드 한줄평 목록 조회
     */
    public List<KeyboardScoreDTO> getKeyboardComments(long keyboardId) throws SQLException {
        List<KeyboardScoreDTO> commentList = new ArrayList<>();
        String sql = "SELECT s.*, u.user_name " +
                "FROM keyboard_score s " +
                "JOIN user u ON s.user_uid = u.user_uid " +
                "WHERE s.keyboard_uid = ? " +
                "ORDER BY s.score_created_at DESC";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                KeyboardScoreDTO comment = new KeyboardScoreDTO();
                comment.setScoreId(rs.getLong("score_uid"));
                comment.setKeyboardId(rs.getLong("keyboard_uid"));
                comment.setUserId(rs.getLong("user_uid"));
                comment.setUserName(rs.getString("user_name"));
                comment.setScoreValue(rs.getInt("score_value"));
                comment.setReview(rs.getString("score_review"));

                Timestamp createdAt = rs.getTimestamp("score_created_at");
                if (createdAt != null) {
                    comment.setCreatedAt(createdAt.toLocalDateTime());
                }

                commentList.add(comment);
            }

            return commentList;
        } finally {
            closeResources();
        }
    }

    /**
     * 한줄평 삭제 (본인 또는 관리자)
     */
    public boolean deleteKeyboardCommentById(long commentId, long userId, boolean isAdmin) throws SQLException {
        String sql;

        if (isAdmin) {
            // 관리자는 모든 한줄평 삭제 가능
            sql = "DELETE FROM keyboard_score WHERE score_uid = ?";
        } else {
            // 일반 사용자는 자신의 한줄평만 삭제 가능
            sql = "DELETE FROM keyboard_score WHERE score_uid = ? AND user_uid = ?";
        }

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, commentId);

            if (!isAdmin) {
                pstmt.setLong(2, userId);
            }

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }

    /**
     * 키보드 스크랩 (즐겨찾기)
     */
    public boolean scrapKeyboardInfo(long keyboardId, long userId) throws SQLException {
        // 이미 스크랩한 경우 확인
        if (hasAlreadyScrapped(keyboardId, userId)) {
            return cancelKeyboardScrap(keyboardId, userId);
        }

        String sql = "INSERT INTO scrap " + // keyboard_scrap -> scrap
                "(keyboard_uid, user_uid, scrap_date) " +
                "VALUES (?, ?, NOW())";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, userId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }

    /**
     * 키보드 스크랩 취소
     */
    private boolean cancelKeyboardScrap(long keyboardId, long userId) throws SQLException {
        String sql = "DELETE FROM scrap " + // keyboard_scrap -> scrap
                "WHERE keyboard_uid = ? AND user_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, userId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }

    /**
     * 이미 스크랩했는지 확인
     */
    public boolean hasAlreadyScrapped(long keyboardId, long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM scrap " + // keyboard_scrap -> scrap
                "WHERE keyboard_uid = ? AND user_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, userId);

            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * 키보드 태그 투표
     */
    public boolean voteKeyboardTag(long keyboardId, long tagId, long userId, String voteType) throws SQLException {
        // 이미 투표했는지 확인
        String voteStatus = getUserTagVoteStatus(keyboardId, tagId, userId);

        if (voteStatus != null) {
            // 같은 투표 타입이면 투표 취소
            if (voteStatus.equals(voteType)) {
                return cancelTagVote(keyboardId, tagId, userId);
            } else {
                // 다른 투표 타입이면 투표 수정
                return updateTagVote(keyboardId, tagId, userId, voteType);
            }
        }

        // 새로운 투표 추가
        String sql = "INSERT INTO keyboard_tag_vote " +
                "(keyboard_uid, tag_uid, user_uid, vote_type, vote_date) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 투표 기록 추가
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, tagId);
            pstmt.setLong(3, userId);
            pstmt.setString(4, voteType);

            int voteResult = pstmt.executeUpdate();

            // 태그 관계 테이블의 투표수 업데이트
            if (voteResult > 0) {
                updateTagRelationVoteCount(keyboardId, tagId);
            }

            conn.commit();
            return voteResult > 0;
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
            closeResources();
        }
    }

    /**
     * 태그 투표 취소
     */
    private boolean cancelTagVote(long keyboardId, long tagId, long userId) throws SQLException {
        String sql = "DELETE FROM keyboard_tag_vote " +
                "WHERE keyboard_uid = ? AND tag_uid = ? AND user_uid = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 투표 기록 삭제
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, tagId);
            pstmt.setLong(3, userId);

            int voteResult = pstmt.executeUpdate();

            // 태그 관계 테이블의 투표수 업데이트
            if (voteResult > 0) {
                updateTagRelationVoteCount(keyboardId, tagId);
            }

            conn.commit();
            return voteResult > 0;
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
            closeResources();
        }
    }

    /**
     * 태그 투표 수정
     */
    private boolean updateTagVote(long keyboardId, long tagId, long userId, String voteType) throws SQLException {
        String sql = "UPDATE keyboard_tag_vote " +
                "SET vote_type = ?, vote_date = NOW() " +
                "WHERE keyboard_uid = ? AND tag_uid = ? AND user_uid = ?";

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 투표 기록 수정
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, voteType);
            pstmt.setLong(2, keyboardId);
            pstmt.setLong(3, tagId);
            pstmt.setLong(4, userId);

            int voteResult = pstmt.executeUpdate();

            // 태그 관계 테이블의 투표수 업데이트
            if (voteResult > 0) {
                updateTagRelationVoteCount(keyboardId, tagId);
            }

            conn.commit();
            return voteResult > 0;
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
            closeResources();
        }
    }

    /**
     * 태그 관계 테이블의 투표수 업데이트
     */
    private void updateTagRelationVoteCount(long keyboardId, long tagId) throws SQLException {
        String sql = "UPDATE keyboard_tag_relation " +
                "SET vote_count = (" +
                "    SELECT COUNT(*) FROM keyboard_tag_vote " +
                "    WHERE keyboard_uid = ? AND tag_uid = ? AND vote_type = 'up'" +
                ") - (" +
                "    SELECT COUNT(*) FROM keyboard_tag_vote " +
                "    WHERE keyboard_uid = ? AND tag_uid = ? AND vote_type = 'down'" +
                ") " +
                "WHERE keyboard_uid = ? AND tag_uid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, tagId);
            pstmt.setLong(3, keyboardId);
            pstmt.setLong(4, tagId);
            pstmt.setLong(5, keyboardId);
            pstmt.setLong(6, tagId);

            pstmt.executeUpdate();
        }
    }

    /**
     * 사용자의 태그 투표 상태 조회
     */
    private String getUserTagVoteStatus(long keyboardId, long tagId, long userId) throws SQLException {
        String sql = "SELECT vote_type FROM keyboard_tag_vote " +
                "WHERE keyboard_uid = ? AND tag_uid = ? AND user_uid = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, tagId);
            pstmt.setLong(3, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("vote_type");
                }
                return null;
            }
        }
    }

    /**
     * 키보드의 태그 목록 조회 (투표수 포함)
     */
    public List<KeyboardTagDTO> getKeyboardTagsWithVotes(long keyboardId, long userId) throws SQLException {
        List<KeyboardTagDTO> tagList = new ArrayList<>();
        String sql = "SELECT t.*, r.vote_count, v.vote_type " +
                "FROM keyboard_tag t " +
                "JOIN keyboard_tag_relation r ON t.tag_uid = r.tag_uid " +
                "LEFT JOIN keyboard_tag_vote v ON t.tag_uid = v.tag_uid AND v.keyboard_uid = ? AND v.user_uid = ? " +
                "WHERE r.keyboard_uid = ? " +
                "ORDER BY r.vote_count DESC, t.tag_name";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, userId);
            pstmt.setLong(3, keyboardId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                KeyboardTagDTO tag = new KeyboardTagDTO();
                tag.setTagId(rs.getLong("tag_uid"));
                tag.setTagName(rs.getString("tag_name"));
                tag.setTagType(rs.getString("tag_type"));
                tag.setVoteCount(rs.getInt("vote_count"));
                tag.setUserVote(rs.getString("vote_type"));

                tagList.add(tag);
            }

            return tagList;
        } finally {
            closeResources();
        }
    }

    /**
     * 태그 제안 (새로운 태그 추가)
     */
    public boolean suggestKeyboardTag(String tagName, long keyboardId, long userId) throws SQLException {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }

        // 이미 존재하는 태그인지 확인
        long existingTagId = getTagIdByName(tagName);
        long tagId;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (existingTagId > 0) {
                // 이미 존재하는 태그인 경우
                tagId = existingTagId;

                // 이미 해당 키보드에 연결되어 있는지 확인
                if (isTagLinkedToKeyboard(keyboardId, tagId)) {
                    // 이미 연결되어 있으면 해당 태그에 바로 투표
                    conn.commit();
                    return voteKeyboardTag(keyboardId, tagId, userId, "up");
                }
            } else {
                // 새로운 태그 생성
                String insertTagSql = "INSERT INTO keyboard_tag (tag_name, tag_type) VALUES (?, 'user')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertTagSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, tagName.trim());
                    pstmt.executeUpdate();

                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            tagId = generatedKeys.getLong(1);
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }

            // 태그와 키보드 연결
            if (!isTagLinkedToKeyboard(keyboardId, tagId)) {
                String linkTagSql = "INSERT INTO keyboard_tag_relation (keyboard_uid, tag_uid, vote_count) VALUES (?, ?, 0)";
                try (PreparedStatement pstmt = conn.prepareStatement(linkTagSql)) {
                    pstmt.setLong(1, keyboardId);
                    pstmt.setLong(2, tagId);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            // 새로 추가된 태그에 투표
            return voteKeyboardTag(keyboardId, tagId, userId, "up");
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
            closeResources();
        }
    }

    /**
     * 태그 이름으로 ID 조회
     */
    private long getTagIdByName(String tagName) throws SQLException {
        String sql = "SELECT tag_uid FROM keyboard_tag WHERE tag_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tagName.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("tag_uid");
                }
                return 0;
            }
        }
    }

    /**
     * 태그가 키보드와 연결되어 있는지 확인
     */
    private boolean isTagLinkedToKeyboard(long keyboardId, long tagId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM keyboard_tag_relation WHERE keyboard_uid = ? AND tag_uid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, tagId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 별점 추가
     */
    public boolean addKeyboardScore(KeyboardScoreDTO score) throws SQLException {
        // 이미 별점을 등록했는지 확인
        if (hasUserRated(score.getKeyboardId(), score.getUserId())) {
            return updateKeyboardScore(score);
        }

        String sql = "INSERT INTO keyboard_score " +
                "(keyboard_uid, user_uid, score_value, score_review, score_created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, score.getKeyboardId());
            pstmt.setLong(2, score.getUserId());
            pstmt.setInt(3, score.getScoreValue());
            pstmt.setString(4, score.getReview());

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }

    /**
     * 별점 수정
     */
    public boolean updateKeyboardScore(KeyboardScoreDTO score) throws SQLException {
        String sql = "UPDATE keyboard_score " +
                "SET score_value = ?, score_review = ?, score_created_at = NOW() " +
                "WHERE keyboard_uid = ? AND user_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, score.getScoreValue());
            pstmt.setString(2, score.getReview());
            pstmt.setLong(3, score.getKeyboardId());
            pstmt.setLong(4, score.getUserId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }

    /**
     * 사용자가 이미 별점을 등록했는지 확인
     */
    public boolean hasUserRated(long keyboardId, long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM keyboard_score WHERE keyboard_uid = ? AND user_uid = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 사용자 별점 조회
     */
    public KeyboardScoreDTO getUserScore(long keyboardId, long userId) throws SQLException {
        String sql = "SELECT * FROM keyboard_score WHERE keyboard_uid = ? AND user_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            pstmt.setLong(2, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                KeyboardScoreDTO score = new KeyboardScoreDTO();
                score.setScoreId(rs.getLong("score_uid"));
                score.setKeyboardId(rs.getLong("keyboard_uid"));
                score.setUserId(rs.getLong("user_uid"));
                score.setScoreValue(rs.getInt("score_value"));
                score.setReview(rs.getString("score_review"));

                Timestamp createdAt = rs.getTimestamp("score_created_at");
                if (createdAt != null) {
                    score.setCreatedAt(createdAt.toLocalDateTime());
                }

                return score;
            }

            return null;
        } finally {
            closeResources();
        }
    }

    /**
     * 총 키보드 수 조회 (페이징용)
     */
    public int getTotalKeyboardCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM keyboard_information"; // keyboard_info -> keyboard_information

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
     * 검색 조건에 맞는 키보드 수 조회 (페이징용)
     */
    public int getFilteredKeyboardCount(String keyword, String manufacturer,
                                        String switchType, String layoutType, String connectType) throws SQLException {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT COUNT(*) FROM keyboard_information k WHERE 1=1 "); // keyboard_info -> keyboard_information

        // 검색 조건 추가
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (k.keyboard_name LIKE ? OR k.keyboard_description LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            sql.append("AND k.keyboard_manufacturer = ? ");
            params.add(manufacturer);
        }

        if (switchType != null && !switchType.trim().isEmpty()) {
            sql.append("AND k.keyboard_switch_type = ? ");
            params.add(switchType);
        }

        if (layoutType != null && !layoutType.trim().isEmpty()) {
            sql.append("AND k.keyboard_layout_type = ? ");
            params.add(layoutType);
        }

        if (connectType != null && !connectType.trim().isEmpty()) {
            sql.append("AND k.keyboard_connect_type = ? ");
            params.add(connectType);
        }

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            // 파라미터 설정
            int paramIndex = 1;
            for (Object param : params) {
                pstmt.setObject(paramIndex++, param);
            }

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
     * 제조사 목록 조회
     */
    public List<String> getAllManufacturers() throws SQLException {
        List<String> manufacturers = new ArrayList<>();
        String sql = "SELECT DISTINCT keyboard_manufacturer FROM keyboard_information ORDER BY keyboard_manufacturer"; // keyboard_info -> keyboard_information

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                manufacturers.add(rs.getString("keyboard_manufacturer"));
            }

            return manufacturers;
        } finally {
            closeResources();
        }
    }

    /**
     * 스위치 타입 목록 조회
     */
    public List<String> getAllSwitchTypes() throws SQLException {
        List<String> switchTypes = new ArrayList<>();
        String sql = "SELECT DISTINCT keyboard_switch_type FROM keyboard_information ORDER BY keyboard_switch_type"; // keyboard_info -> keyboard_information

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                switchTypes.add(rs.getString("keyboard_switch_type"));
            }

            return switchTypes;
        } finally {
            closeResources();
        }
    }

    /**
     * 레이아웃 타입 목록 조회
     */
    public List<String> getAllLayoutTypes() throws SQLException {
        List<String> layoutTypes = new ArrayList<>();
        String sql = "SELECT DISTINCT keyboard_layout_type FROM keyboard_information ORDER BY keyboard_layout_type"; // keyboard_info -> keyboard_information

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                layoutTypes.add(rs.getString("keyboard_layout_type"));
            }

            return layoutTypes;
        } finally {
            closeResources();
        }
    }

    /**
     * 연결 타입 목록 조회
     */
    public List<String> getAllConnectTypes() throws SQLException {
        List<String> connectTypes = new ArrayList<>();
        String sql = "SELECT DISTINCT keyboard_connect_type FROM keyboard_information ORDER BY keyboard_connect_type"; // keyboard_info -> keyboard_information

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                connectTypes.add(rs.getString("keyboard_connect_type"));
            }

            return connectTypes;
        } finally {
            closeResources();
        }
    }

    /**
     * 사용자가 스크랩한 키보드 목록 조회
     */
    public List<KeyboardInfoDTO> getScrapsByUserId(long userId, int page, int pageSize) throws SQLException {
        List<KeyboardInfoDTO> scrapList = new ArrayList<>();

        String sql = "SELECT k.*, " +
                "(SELECT AVG(score_value) FROM keyboard_score WHERE keyboard_uid = k.keyboard_uid) AS avg_score, " +
                "s.scrap_date " +
                "FROM keyboard_information k " + // keyboard_info -> keyboard_information
                "JOIN scrap s ON k.keyboard_uid = s.keyboard_uid " + // keyboard_scrap -> scrap
                "WHERE s.user_uid = ? " +
                "ORDER BY s.scrap_date DESC " +
                "LIMIT ? OFFSET ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                KeyboardInfoDTO keyboard = createKeyboardFromResultSet(rs);

                // 스크랩 날짜 설정
                keyboard.setScrapDate(rs.getTimestamp("scrap_date"));

                // 키보드의 태그 정보 가져오기
                List<String> tags = getKeyboardTags(keyboard.getKeyboardId());
                keyboard.setTags(tags);

                scrapList.add(keyboard);
            }

            return scrapList;
        } finally {
            closeResources();
        }
    }

    /**
     * 사용자가 스크랩한 키보드 총 개수 조회
     */
    public int getTotalScrapCountByUserId(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) " +
                "FROM scrap " + // keyboard_scrap -> scrap
                "WHERE user_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);

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
     * 사용자가 작성한 별점 목록 조회
     */
    public List<KeyboardScoreDTO> getScoresByUserId(long userId, String sortBy, int page, int pageSize) throws SQLException {
        List<KeyboardScoreDTO> scoreList = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s.*, k.keyboard_name, u.nickname ");
        sql.append("FROM keyboard_score s ");
        sql.append("JOIN keyboard_information k ON s.keyboard_uid = k.keyboard_uid "); // keyboard_info -> keyboard_information
        sql.append("JOIN user u ON s.user_uid = u.user_id ");
        sql.append("WHERE s.user_uid = ? ");

        // 정렬 기준 적용
        if ("date".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY s.score_created_at DESC ");
        } else if ("score".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY s.score_value DESC ");
        } else {
            sql.append("ORDER BY s.score_created_at DESC "); // 기본 정렬 기준
        }

        sql.append("LIMIT ? OFFSET ?");

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                KeyboardScoreDTO score = new KeyboardScoreDTO();
                score.setScoreId(rs.getLong("score_uid"));
                score.setKeyboardId(rs.getLong("keyboard_uid"));
                score.setUserId(rs.getLong("user_uid"));
                score.setScoreValue(rs.getInt("score_value"));
                score.setReview(rs.getString("score_review"));
                // Timestamp를 LocalDateTime으로 변환
                java.sql.Timestamp timestamp = rs.getTimestamp("score_created_at");
                if (timestamp != null) {
                    score.setCreatedAt(timestamp.toLocalDateTime());
                }

                // 키보드 이름과 사용자 닉네임 설정
                score.setUserName(rs.getString("nickname"));
                score.setKeyboardName(rs.getString("keyboard_name"));

                scoreList.add(score);
            }

            return scoreList;
        } finally {
            closeResources();
        }
    }

    /**
     * 사용자가 작성한 별점 총 개수 조회
     */
    public int getTotalScoreCountByUserId(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM keyboard_score WHERE user_uid = ?";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        } finally {
            closeResources();
        }
    }
}
