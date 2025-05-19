package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dto.keyboard.KeyboardCategoryDTO;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardTagDTO;
import util.db.DBConnectionUtil;
import util.db.TransactionHelper;

/**
 * 관리자용 키보드 정보 관리 DAO 클래스
 */
public class AdminKeyboardDAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    
    /**
     * 모든 키보드 정보를 조회합니다.
     */
    public List<KeyboardInfoDTO> getAllKeyboardInfos() throws SQLException {
        List<KeyboardInfoDTO> keyboardList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_information ORDER BY keyboard_information_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                KeyboardInfoDTO keyboard = new KeyboardInfoDTO();
                keyboard.setKeyboardId(rs.getLong("keyboard_information_uid"));
                keyboard.setName(rs.getString("keyboard_information_name"));
                keyboard.setPrice(rs.getInt("keyboard_information_price"));
                // 카테고리 ID 설정
                long categoryId = rs.getLong("keyboard_category_uid");
                keyboard.setCategoryId(categoryId);
                
                // 태그 정보 조회
                keyboard.setTagIds(getTagIdsByKeyboardId(keyboard.getKeyboardId()));
                
                keyboardList.add(keyboard);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return keyboardList;
    }
    
    /**
     * 키보드 정보를 등록합니다.
     */
    public boolean addKeyboardInfo(KeyboardInfoDTO keyboard) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean success = false;
          try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 키보드 정보 등록
            String sql = "INSERT INTO keyboard_information (keyboard_information_name, keyboard_information_price, keyboard_category_uid) " +
                         "VALUES (?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, keyboard.getName());
            pstmt.setInt(2, keyboard.getPrice());
            pstmt.setLong(3, keyboard.getCategoryId());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 생성된 키보드 ID 가져오기
                rs = pstmt.getGeneratedKeys();
                long keyboardId = 0;
                if (rs.next()) {
                    keyboardId = rs.getLong(1);
                }
                
                // 태그 매핑 등록
                if (keyboardId > 0 && keyboard.getTagIds() != null && !keyboard.getTagIds().isEmpty()) {
                    addKeyboardTagMappings(conn, keyboardId, keyboard.getTagIds());
                }
                
                success = TransactionHelper.commit(conn);
            }
        } catch (SQLException e) {
            TransactionHelper.rollback(conn);
            throw e;
        } finally {
            TransactionHelper.setAutoCommit(conn, true);
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return success;
    }
    
    /**
     * 키보드 정보를 수정합니다.
     */
    public boolean updateKeyboardInfo(KeyboardInfoDTO keyboard) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 키보드 정보 수정
            String sql = "UPDATE keyboard_information SET keyboard_information_name = ?, keyboard_information_price = ?, " +
                         "keyboard_category_uid = ? " +
                         "WHERE keyboard_information_uid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, keyboard.getName());
            pstmt.setInt(2, keyboard.getPrice());
            pstmt.setLong(3, keyboard.getCategoryId());
            pstmt.setLong(4, keyboard.getId());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 기존 태그 매핑 삭제 후 새로 등록
                if (keyboard.getTagIds() != null) {
                    deleteKeyboardTagMappings(conn, keyboard.getId());
                    if (!keyboard.getTagIds().isEmpty()) {
                        addKeyboardTagMappings(conn, keyboard.getId(), keyboard.getTagIds());
                    }
                }
                
                conn.commit();
                success = true;
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
            DBConnectionUtil.close(null, pstmt, conn);
        }
        
        return success;
    }
    
    /**
     * 키보드 정보를 삭제합니다.
     */
    public boolean deleteKeyboardInfo(long keyboardId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        
        try {
            conn = DBConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 태그 매핑 삭제
            deleteKeyboardTagMappings(conn, keyboardId);
            
            // 키보드 정보 삭제
            String sql = "DELETE FROM keyboard_information WHERE keyboard_information_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, keyboardId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                conn.commit();
                success = true;
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
            DBConnectionUtil.close(null, pstmt, conn);
        }
        
        return success;
    }
    
    /**
     * 모든 키보드 축 카테고리를 조회합니다.
     */
    public List<KeyboardCategoryDTO> getAllSwitchCategories() throws SQLException {
        return getAllCategoriesByType("switch");
    }
    
    /**
     * 모든 키보드 배열 카테고리를 조회합니다.
     */
    public List<KeyboardCategoryDTO> getAllLayoutCategories() throws SQLException {
        return getAllCategoriesByType("layout");
    }
    
    /**
     * 모든 키보드 연결방식 카테고리를 조회합니다.
     */
    public List<KeyboardCategoryDTO> getAllConnectCategories() throws SQLException {
        return getAllCategoriesByType("connect");
    }
    
    /**
     * 타입별 카테고리 목록을 조회합니다.
     */
    private List<KeyboardCategoryDTO> getAllCategoriesByType(String type) throws SQLException {
        List<KeyboardCategoryDTO> categoryList = new ArrayList<>();
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_category WHERE category_type = ? ORDER BY keyboard_category_name";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                KeyboardCategoryDTO category = new KeyboardCategoryDTO();
                category.setKeyboardCategoryUid(rs.getLong("keyboard_category_uid"));
                category.setKeyboardCategoryName(rs.getString("keyboard_category_name"));
                category.setType(rs.getString("category_type"));
                
                categoryList.add(category);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return categoryList;
    }
    
    /**
     * 키보드 카테고리를 추가합니다.
     */
    public boolean addKeyboardCategory(KeyboardCategoryDTO category) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "INSERT INTO keyboard_category (keyboard_category_name, category_type) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getKeyboardCategoryName());
            pstmt.setString(2, category.getType());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 키보드 카테고리를 수정합니다.
     */
    public boolean updateKeyboardCategory(KeyboardCategoryDTO category) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE keyboard_category SET keyboard_category_name = ?, category_type = ? WHERE keyboard_category_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getKeyboardCategoryName());
            pstmt.setString(2, category.getType());
            pstmt.setLong(3, category.getKeyboardCategoryUid());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 키보드 카테고리를 삭제합니다.
     */
    public boolean deleteKeyboardCategory(long categoryId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "DELETE FROM keyboard_category WHERE keyboard_category_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, categoryId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 모든 키보드 카테고리를 조회합니다.
     */
    public List<KeyboardCategoryDTO> getAllKeyboardCategories() throws SQLException {
        List<KeyboardCategoryDTO> categories = new ArrayList<>();
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_category ORDER BY keyboard_category_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                KeyboardCategoryDTO category = new KeyboardCategoryDTO();
                category.setKeyboardCategoryUid(rs.getLong("keyboard_category_uid"));
                category.setKeyboardCategoryName(rs.getString("keyboard_category_name"));
                
                categories.add(category);
            }
            
            return categories;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 특정 ID의 키보드 카테고리를 조회합니다.
     */
    public KeyboardCategoryDTO getKeyboardCategoryById(long categoryId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_category WHERE keyboard_category_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, categoryId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                KeyboardCategoryDTO category = new KeyboardCategoryDTO();
                category.setKeyboardCategoryUid(rs.getLong("keyboard_category_uid"));
                category.setKeyboardCategoryName(rs.getString("keyboard_category_name"));
                
                return category;
            }
            
            return null;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 모든 키보드 태그를 조회합니다.
     */
    public List<KeyboardTagDTO> getAllKeyboardTags() throws SQLException {
        List<KeyboardTagDTO> tags = new ArrayList<>();
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_tag ORDER BY tag_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                KeyboardTagDTO tag = new KeyboardTagDTO();
                tag.setTagId(rs.getLong("tag_uid"));
                tag.setTagName(rs.getString("tag_name"));
                tag.setTagType(rs.getString("tag_approve"));
                
                tags.add(tag);
            }
            
            return tags;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 키보드 태그를 추가합니다.
     */
    public boolean addKeyboardTag(KeyboardTagDTO tag) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "INSERT INTO keyboard_tag (tag_name, tag_approve) VALUES (?, 'approved')";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, tag.getTagName());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 키보드 태그를 수정합니다.
     */
    public boolean updateKeyboardTag(KeyboardTagDTO tag) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE keyboard_tag SET tag_name = ? WHERE tag_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tag.getTagName());
            pstmt.setLong(2, tag.getTagId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 키보드 태그를 삭제합니다.
     */
    public boolean deleteKeyboardTag(long tagId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "DELETE FROM keyboard_tag WHERE tag_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, tagId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 특정 ID의 키보드 태그를 조회합니다.
     */
    public KeyboardTagDTO getKeyboardTagById(long tagId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_tag WHERE tag_uid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, tagId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                KeyboardTagDTO tag = new KeyboardTagDTO();
                tag.setTagId(rs.getLong("tag_uid"));
                tag.setTagName(rs.getString("tag_name"));
                tag.setTagType(rs.getString("tag_approve"));
                
                return tag;
            }
            
            return null;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }
    
    /**
     * 키보드 태그를 승인합니다.
     */
    public boolean confirmKeyboardTag(long tagId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE keyboard_tag SET tag_approve = 'approved' WHERE tag_uid = ?";
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setLong(1, tagId);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(null, pstmt, conn);
        }
    }
    
    /**
     * 키보드 ID로 태그 ID 목록을 조회합니다.
     */
    private List<Long> getTagIdsByKeyboardId(long keyboardId) throws SQLException {
        List<Long> tagIds = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT tag_uid FROM keyboard_taglist WHERE keyboard_information_uid = ?";
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setLong(1, keyboardId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tagIds.add(rs.getLong("tag_uid"));
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
        
        return tagIds;
    }
    
    /**
     * 키보드와 태그 매핑을 추가합니다.
     */
    private void addKeyboardTagMappings(Connection conn, long keyboardId, List<Long> tagIds) throws SQLException {
        PreparedStatement pstmt = null;
        
        try {
            String sql = "INSERT INTO keyboard_taglist (tag_type, tag_uid, keyboard_information_uid) VALUES ('admin', ?, ?)";
            pstmt = conn.prepareStatement(sql);
            
            for (Long tagId : tagIds) {
                pstmt.setLong(1, tagId);
                pstmt.setLong(2, keyboardId);
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }
    
    /**
     * 키보드 태그 매핑을 삭제합니다.
     */
    private void deleteKeyboardTagMappings(Connection conn, long keyboardId) throws SQLException {
        PreparedStatement pstmt = null;
        
        try {
            String sql = "DELETE FROM keyboard_taglist WHERE keyboard_information_uid = ?";
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setLong(1, keyboardId);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }
}
