package repository.dao.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dto.board.AnswerDTO;
import dto.board.AttachmentDTO;
import dto.board.QuestionDTO;
import dto.user.UserDTO;
import util.db.DBConnectionUtil;

public class QuestionDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    
    // DB 연결 메서드
    private Connection getConnection() throws SQLException {
        return DBConnectionUtil.getConnection();
    }
    
    // 자원 해제 메서드
    private void closeResources() {
        DBConnectionUtil.close(rs, pstmt, conn);
    }
    
    /**
     * 질문 ID로 질문 정보 조회
     */
    public QuestionDTO getQuestionById(long questionId) throws SQLException {
        String sql = "SELECT q.*, u.user_name " +
                    "FROM inquiry q " +
                    "JOIN user u ON q.user_uid = u.user_uid " +
                    "WHERE q.inquiry_uid = ? AND q.inquiry_deleted = 'maintained'";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToQuestion(rs);
            }
            return null;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 질문 생성
     */
    public boolean createQuestion(QuestionDTO question) throws SQLException {
        String sql = "INSERT INTO inquiry (inquiry_title, inquiry_contents, inquiry_writetime, " +
                    "inquiry_modify_time, inquiry_author_ip, inquiry_deleted, user_uid, " +
                    "inquiry_category, inquiry_read_status) " +
                    "VALUES (?, ?, NOW(), NOW(), ?, 'maintained', ?, ?, 'unread')";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getAuthorIp());
            pstmt.setLong(4, question.getUserUid());
            pstmt.setString(5, question.getCategory());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // 생성된 ID 가져오기
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    question.setQuestionId(rs.getLong(1));
                }
                return true;
            }
            return false;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 질문 수정
     */
    public boolean updateQuestion(QuestionDTO question, long modifierId, String modifierAuthority) throws SQLException {
        String sql = "UPDATE inquiry SET inquiry_title = ?, inquiry_contents = ?, " +
                    "inquiry_modify_time = NOW(), inquiry_category = ? " +
                    "WHERE inquiry_uid = ?";
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getCategory());
            pstmt.setLong(4, question.getQuestionId());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 수정 로그 저장
                String logSql = "INSERT INTO log_modify_post (log_modify_boardtype, log_modify_date, " +
                               "log_modify_post_uid, user_uid) " +
                               "VALUES ('inquiry', NOW(), ?, ?)";
                
                pstmt = conn.prepareStatement(logSql);
                pstmt.setLong(1, question.getQuestionId());
                pstmt.setLong(2, modifierId);
                
                pstmt.executeUpdate();
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
     * 질문 삭제 (소프트 삭제)
     */
    public boolean deleteQuestion(long questionId, long deleterId, String reason) throws SQLException {
        String sql = "UPDATE inquiry SET inquiry_deleted = 'deleted' WHERE inquiry_uid = ?";
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 삭제 로그 저장
                String logSql = "INSERT INTO log_delete_post (log_delete_boardtype, log_delete_date, " +
                               "log_deleted_post_uid, user_uid) " +
                               "VALUES ('inquiry', NOW(), ?, ?)";
                
                pstmt = conn.prepareStatement(logSql);
                pstmt.setLong(1, questionId);
                pstmt.setLong(2, deleterId);
                
                pstmt.executeUpdate();
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
     * 조회수 증가
     */
    public boolean increaseViewCount(long questionId) throws SQLException {
        String sql = "UPDATE inquiry SET inquiry_read_status = 'read' WHERE inquiry_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 모든 질문 목록 조회 (페이징)
     */
    public List<QuestionDTO> getAllQuestions(int page, int pageSize) throws SQLException {
        int offset = (page - 1) * pageSize;
        String sql = "SELECT q.*, u.user_name " +
                     "FROM inquiry q " +
                     "JOIN user u ON q.user_uid = u.user_uid " +
                     "WHERE q.inquiry_parent_uid IS NULL " +
                     "AND q.inquiry_deleted = 'maintained' " +
                     "ORDER BY q.inquiry_writetime DESC " +
                     "LIMIT ? OFFSET ?";
        
        List<QuestionDTO> questions = new ArrayList<>();
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                QuestionDTO question = mapResultSetToQuestion(rs);
                questions.add(question);
            }
            
            return questions;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 전체 질문 수 조회
     */
    public int getTotalQuestions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inquiry WHERE inquiry_parent_uid IS NULL " +
                     "AND inquiry_deleted = 'maintained'";
        
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
     * 특정 사용자의 질문 목록 조회 (페이징)
     */
    public List<QuestionDTO> getQuestionsByUserId(long userId, int page, int pageSize) throws SQLException {
        int offset = (page - 1) * pageSize;
        String sql = "SELECT q.*, u.user_name " +
                     "FROM inquiry q " +
                     "JOIN user u ON q.user_uid = u.user_uid " +
                     "WHERE q.inquiry_parent_uid IS NULL " +
                     "AND q.inquiry_deleted = 'maintained' " +
                     "AND q.user_uid = ? " +
                     "ORDER BY q.inquiry_writetime DESC " +
                     "LIMIT ? OFFSET ?";
        
        List<QuestionDTO> questions = new ArrayList<>();
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                QuestionDTO question = mapResultSetToQuestion(rs);
                questions.add(question);
            }
            
            return questions;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 특정 사용자의 전체 질문 수 조회
     */
    public int getTotalQuestionsByUserId(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inquiry WHERE inquiry_parent_uid IS NULL " +
                     "AND inquiry_deleted = 'maintained' AND user_uid = ?";
        
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
     * 질문에 대한 답변 목록 조회
     */
    public List<AnswerDTO> getAnswersByQuestionId(long questionId) throws SQLException {
        String sql = "SELECT a.*, u.user_name " +
                     "FROM inquiry a " +
                     "JOIN user u ON a.user_uid = u.user_uid " +
                     "WHERE a.inquiry_parent_uid = ? " +
                     "AND a.inquiry_deleted = 'maintained' " +
                     "ORDER BY a.inquiry_writetime ASC";
        
        List<AnswerDTO> answers = new ArrayList<>();
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AnswerDTO answer = mapResultSetToAnswer(rs);
                answers.add(answer);
            }
            
            return answers;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 답변 ID로 답변 정보 조회
     */
    public AnswerDTO getAnswerById(long answerId) throws SQLException {
        String sql = "SELECT a.*, u.user_name " +
                     "FROM inquiry a " +
                     "JOIN user u ON a.user_uid = u.user_uid " +
                     "WHERE a.inquiry_uid = ? " +
                     "AND a.inquiry_deleted = 'maintained'";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, answerId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAnswer(rs);
            }
            return null;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 답변 생성
     */
    public boolean createAnswer(AnswerDTO answer) throws SQLException {
        String sql = "INSERT INTO inquiry (inquiry_title, inquiry_contents, inquiry_writetime, " +
                    "inquiry_modify_time, inquiry_author_ip, inquiry_deleted, user_uid, " +
                    "inquiry_parent_uid, inquiry_category, inquiry_read_status) " +
                    "VALUES ('답변', ?, NOW(), NOW(), ?, 'maintained', ?, ?, 'question', 'read')";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, answer.getContent());
            pstmt.setString(2, answer.getAuthorIp());
            pstmt.setLong(3, answer.getUserUid());
            pstmt.setLong(4, answer.getQuestionId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // 생성된 ID 가져오기
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    answer.setAnswerId(rs.getLong(1));
                }
                return true;
            }
            return false;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 답변 수정
     */
    public boolean updateAnswer(AnswerDTO answer, long modifierId, String modifierAuthority) throws SQLException {
        String sql = "UPDATE inquiry SET inquiry_contents = ?, inquiry_modify_time = NOW() " +
                    "WHERE inquiry_uid = ?";
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, answer.getContent());
            pstmt.setLong(2, answer.getAnswerId());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 수정 로그 저장
                String logSql = "INSERT INTO log_modify_comment (log_modify_boardtype, log_modify_date, " +
                              "log_modify_comment_uid, user_uid) " +
                              "VALUES ('inquiry', NOW(), ?, ?)";
                
                pstmt = conn.prepareStatement(logSql);
                pstmt.setLong(1, answer.getAnswerId());
                pstmt.setLong(2, modifierId);
                
                pstmt.executeUpdate();
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
     * 답변 삭제 (소프트 삭제)
     */
    public boolean deleteAnswer(long answerId, long deleterId, String reason) throws SQLException {
        String sql = "UPDATE inquiry SET inquiry_deleted = 'deleted' WHERE inquiry_uid = ?";
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, answerId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 삭제 로그 저장
                String logSql = "INSERT INTO log_delete_comment (log_delete_boardtype, log_delete_date, " +
                              "log_deleted_comment_uid, user_uid) " +
                              "VALUES ('inquiry', NOW(), ?, ?)";
                
                pstmt = conn.prepareStatement(logSql);
                pstmt.setLong(1, answerId);
                pstmt.setLong(2, deleterId);
                
                pstmt.executeUpdate();
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
     * 첨부파일 추가
     */
    public boolean addAttachment(long questionId, String fileName, String filePath, long fileSize) throws SQLException {
        String sql = "INSERT INTO freeboard_attach (freeboard_uid, file_name, file_path, file_size, upload_date) " +
                    "VALUES (?, ?, ?, ?, NOW())";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            pstmt.setString(2, fileName);
            pstmt.setString(3, filePath);
            pstmt.setLong(4, fileSize);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 질문에 대한 첨부파일 목록 조회
     */
    public List<AttachmentDTO> getAttachmentsByQuestionId(long questionId) throws SQLException {
        String sql = "SELECT * FROM freeboard_attach WHERE freeboard_uid = ?";
        
        List<AttachmentDTO> attachments = new ArrayList<>();
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, questionId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AttachmentDTO attachment = new AttachmentDTO();
                attachment.setAttachId(rs.getLong("attach_uid"));
                attachment.setPostId(rs.getLong("freeboard_uid"));
                attachment.setFileName(rs.getString("file_name"));
                attachment.setFilePath(rs.getString("file_path"));
                attachment.setFileSize(rs.getLong("file_size"));
                attachment.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
                attachments.add(attachment);
            }
            
            return attachments;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 첨부파일 ID로 첨부파일 정보 조회
     */
    public AttachmentDTO getAttachmentById(long attachId) throws SQLException {
        String sql = "SELECT * FROM freeboard_attach WHERE attach_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, attachId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                AttachmentDTO attachment = new AttachmentDTO();
                attachment.setAttachId(rs.getLong("attach_uid"));
                attachment.setPostId(rs.getLong("freeboard_uid"));
                attachment.setFileName(rs.getString("file_name"));
                attachment.setFilePath(rs.getString("file_path"));
                attachment.setFileSize(rs.getLong("file_size"));
                attachment.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
                return attachment;
            }
            return null;
        } finally {
            closeResources();
        }
    }
    
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    public UserDTO getUserById(long userId) throws SQLException {
        String sql = "SELECT * FROM user WHERE user_uid = ?";
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                UserDTO user = new UserDTO();
                user.setUserId(rs.getLong("user_uid"));
                user.setUsername(rs.getString("user_id"));
                user.setNickname(rs.getString("user_name"));
                user.setEmail(rs.getString("user_email"));
                user.setUserAuthority(rs.getString("user_authority"));
                return user;
            }
            return null;
        } finally {
            closeResources();
        }
    }
    
    /**
     * ResultSet에서 QuestionDTO 객체로 매핑
     */
    private QuestionDTO mapResultSetToQuestion(ResultSet rs) throws SQLException {
        QuestionDTO question = new QuestionDTO();
        question.setQuestionId(rs.getLong("inquiry_uid"));
        question.setTitle(rs.getString("inquiry_title"));
        question.setContent(rs.getString("inquiry_contents"));
        question.setAuthorIp(rs.getString("inquiry_author_ip"));
        question.setUserUid(rs.getLong("user_uid"));
        question.setUserName(rs.getString("user_name"));
        question.setCategory(rs.getString("inquiry_category"));
        question.setStatus(rs.getString("inquiry_deleted"));
        question.setViewCount(rs.getString("inquiry_read_status").equals("read") ? 1 : 0);
        
        Timestamp createdAt = rs.getTimestamp("inquiry_writetime");
        if (createdAt != null) {
            question.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("inquiry_modify_time");
        if (updatedAt != null) {
            question.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return question;
    }
    
    /**
     * ResultSet에서 AnswerDTO 객체로 매핑
     */
    private AnswerDTO mapResultSetToAnswer(ResultSet rs) throws SQLException {
        AnswerDTO answer = new AnswerDTO();
        answer.setAnswerId(rs.getLong("inquiry_uid"));
        answer.setQuestionId(rs.getLong("inquiry_parent_uid"));
        answer.setContent(rs.getString("inquiry_contents"));
        answer.setAuthorIp(rs.getString("inquiry_author_ip"));
        answer.setUserUid(rs.getLong("user_uid"));
        answer.setUserName(rs.getString("user_name"));
        answer.setStatus(rs.getString("inquiry_deleted"));
        
        Timestamp createdAt = rs.getTimestamp("inquiry_writetime");
        if (createdAt != null) {
            answer.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("inquiry_modify_time");
        if (updatedAt != null) {
            answer.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return answer;
    }
}
