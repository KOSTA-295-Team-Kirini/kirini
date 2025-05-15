package repository.dao.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.keyboard.KeyboardInfoDTO;
import util.db.DBConnectionUtil;

public class KeyboardInfoDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    
    // 키보드 정보 등록
    public boolean addKeyboardInfo(KeyboardInfoDTO keyboard) throws SQLException {
        // 구현...
        return false;
    }
    
    // 모든 키보드 정보 조회
    public List<KeyboardInfoDTO> getAllKeyboardInfos() throws SQLException {
        // 구현...
        return null;
    }
    
    // ID로 키보드 정보 조회
    public KeyboardInfoDTO getKeyboardInfoById(long keyboardId) throws SQLException {
        // 구현...
        return null;
    }
    
    // 키보드 정보 수정
    public boolean updateKeyboardInfo(KeyboardInfoDTO keyboard) throws SQLException {
        // 구현...
        return false;
    }
    
    // 키보드 정보 삭제
    public boolean deleteKeyboardInfo(long keyboardId) throws SQLException {
        // 구현...
        return false;
    }
    
    // 조건별 키보드 검색
    public List<KeyboardInfoDTO> searchKeyboardInfosByCondition(String switchType, String layoutType, String connectType, List<String> tags) throws SQLException {
        // 구현...
        return null;
    }
}