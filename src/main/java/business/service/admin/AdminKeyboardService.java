package business.service.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.keyboard.KeyboardCategoryDTO;
import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardTagDTO;
import repository.dao.admin.AdminKeyboardDAO;
import repository.dao.database.KeyboardInfoDAO;

/**
 * 관리자용 키보드 정보 관리 서비스 클래스
 * DAO 패턴을 사용하여 데이터 액세스 로직과 비즈니스 로직을 분리합니다.
 */
public class AdminKeyboardService {
    
    private AdminKeyboardDAO adminKeyboardDAO;
    private KeyboardInfoDAO keyboardInfoDAO;
    
    public AdminKeyboardService() {
        adminKeyboardDAO = new AdminKeyboardDAO();
        keyboardInfoDAO = new KeyboardInfoDAO();
    }
    
    /**
     * 모든 키보드 정보를 조회합니다.
     * 
     * @return 키보드 정보 목록
     */
    public List<KeyboardInfoDTO> getAllKeyboardInfos() {
        try {
            return adminKeyboardDAO.getAllKeyboardInfos();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 모든 키보드 스위치 카테고리를 조회합니다.
     * 
     * @return 스위치 카테고리 목록
     */
    public List<KeyboardCategoryDTO> getAllSwitchCategories() {
        try {
            return adminKeyboardDAO.getAllSwitchCategories();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 모든 키보드 레이아웃 카테고리를 조회합니다.
     * 
     * @return 레이아웃 카테고리 목록
     */
    public List<KeyboardCategoryDTO> getAllLayoutCategories() {
        try {
            return adminKeyboardDAO.getAllLayoutCategories();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 모든 키보드 연결방식 카테고리를 조회합니다.
     * 
     * @return 연결방식 카테고리 목록
     */
    public List<KeyboardCategoryDTO> getAllConnectCategories() {
        try {
            return adminKeyboardDAO.getAllConnectCategories();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 키보드 정보를 등록합니다.
     * 
     * @param keyboardInfo 등록할 키보드 정보
     * @return 등록 성공 여부
     */
    public boolean addKeyboardInfo(KeyboardInfoDTO keyboardInfo) {
        try {
            return adminKeyboardDAO.addKeyboardInfo(keyboardInfo);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 정보를 수정합니다.
     * 
     * @param keyboardInfo 수정할 키보드 정보
     * @return 수정 성공 여부
     */
    public boolean updateKeyboardInfo(KeyboardInfoDTO keyboardInfo) {
        try {
            return adminKeyboardDAO.updateKeyboardInfo(keyboardInfo);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 정보를 삭제합니다.
     * 
     * @param keyboardId 삭제할 키보드 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteKeyboardInfo(long keyboardId) {
        try {
            return adminKeyboardDAO.deleteKeyboardInfo(keyboardId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 카테고리를 추가합니다.
     * 
     * @param category 추가할 카테고리 정보
     * @return 추가 성공 여부
     */
    public boolean addKeyboardCategory(KeyboardCategoryDTO category) {
        try {
            return adminKeyboardDAO.addKeyboardCategory(category);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 카테고리를 수정합니다.
     * 
     * @param category 수정할 카테고리 정보
     * @return 수정 성공 여부
     */
    public boolean updateKeyboardCategory(KeyboardCategoryDTO category) {
        try {
            return adminKeyboardDAO.updateKeyboardCategory(category);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 카테고리를 수정합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param categoryName 카테고리 이름
     * @param description 카테고리 설명 (선택적)
     * @param type 카테고리 타입
     * @return 수정 성공 여부
     */
    public boolean updateKeyboardCategory(long categoryId, String categoryName, String description, String type) {
        KeyboardCategoryDTO category = new KeyboardCategoryDTO();
        category.setKeyboardCategoryUid(categoryId);
        category.setKeyboardCategoryName(categoryName);
        category.setDescription(description);
        category.setType(type);
        
        try {
            return adminKeyboardDAO.updateKeyboardCategory(category);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 카테고리를 삭제합니다.
     * 
     * @param categoryId 삭제할 카테고리 ID
     * @param type 카테고리 타입
     * @return 삭제 성공 여부
     */
    public boolean deleteKeyboardCategory(long categoryId, String type) {
        try {
            return adminKeyboardDAO.deleteKeyboardCategory(categoryId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 태그를 추가합니다.
     * 
     * @param tag 추가할 태그 정보
     * @return 추가 성공 여부
     */
    public boolean addKeyboardTag(KeyboardTagDTO tag) {
        try {
            return adminKeyboardDAO.addKeyboardTag(tag);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 태그를 수정합니다.
     * 
     * @param tag 수정할 태그 정보
     * @return 수정 성공 여부
     */
    public boolean updateKeyboardTag(KeyboardTagDTO tag) {
        try {
            return adminKeyboardDAO.updateKeyboardTag(tag);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 태그를 삭제합니다.
     * 
     * @param tagId 삭제할 태그 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteKeyboardTag(long tagId) {
        try {
            return adminKeyboardDAO.deleteKeyboardTag(tagId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 키보드 태그를 승인합니다.
     * 
     * @param tagId 승인할 태그 ID
     * @return 승인 성공 여부
     */
    public boolean confirmKeyboardTag(long tagId) {
        try {
            return adminKeyboardDAO.confirmKeyboardTag(tagId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 모든 키보드 태그를 조회합니다.
     * 
     * @return 태그 목록
     */
    public List<KeyboardTagDTO> getAllKeyboardTags() {
        try {
            return adminKeyboardDAO.getAllKeyboardTags();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
