package repository.dao.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.board.FreeboardDTO;
import util.db.DBConnectionUtil;

public class FreeboardDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    
    // 게시글 등록
    public boolean postFreeboard(FreeboardDTO post) throws SQLException {
        // 구현...
        return false;
    }
    
    // 모든 게시글 조회
    public List<FreeboardDTO> getAllFreeboards() throws SQLException {
        // 구현...
        return null;
    }
    
    // ID로 게시글 조회
    public FreeboardDTO getFreeboardById(long postId) throws SQLException {
        // 구현...
        return null;
    }
    
    // 게시글 수정
    public boolean updateFreeboardById(FreeboardDTO post) throws SQLException {
        // 구현...
        return false;
    }
    
    // 게시글 삭제
    public boolean deleteFreeboardById(long postId) throws SQLException {
        // 구현...
        return false;
    }
    
    // 게시글 숨김 처리
    public boolean hideFreeboardById(long postId, String hideReason) throws SQLException {
        // 구현...
        return false;
    }
    
    // 공지사항 지정/해제
    public boolean setNoticeById(long postId, boolean isNotice) throws SQLException {
        // 구현...
        return false;
    }
}