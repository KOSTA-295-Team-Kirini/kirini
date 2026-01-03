package business.service.admin;

import java.sql.SQLException;
import java.util.List;

import dto.keyboard.GuideDTO;
import repository.dao.admin.AdminGuideDAO;

/**
 * 관리자용 키보드 용어집 관리 서비스 클래스
 */
public class AdminGuideService {

    private AdminGuideDAO guideDAO;

    public AdminGuideService() {
        guideDAO = new AdminGuideDAO();
    }

    /**
     * 키보드 용어를 등록합니다.
     *
     * @param guide 등록할 용어 정보
     * @return 등록 성공 여부
     */
    public boolean addGuide(GuideDTO guide) {
        try {
            return guideDAO.addGuide(guide);
        } catch (SQLException e) {
            System.err.println("키보드 용어 등록 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 용어를 수정합니다.
     *
     * @param guide 수정할 용어 정보
     * @return 수정 성공 여부
     */
    public boolean updateGuide(GuideDTO guide) {
        try {
            return guideDAO.updateGuide(guide);
        } catch (SQLException e) {
            System.err.println("키보드 용어 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 용어를 삭제합니다.
     *
     * @param guideId 삭제할 용어 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteGuide(long guideId) {
        try {
            return guideDAO.deleteGuide(guideId);
        } catch (SQLException e) {
            System.err.println("키보드 용어 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 모든 키보드 용어를 조회합니다.
     *
     * @return 용어 목록
     */
    public List<GuideDTO> getAllGuides() {
        try {
            return guideDAO.getAllGuides();
        } catch (SQLException e) {
            System.err.println("키보드 용어 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 용어 카테고리를 추가합니다.
     *
     * @param categoryName 카테고리 이름
     * @return 추가 성공 여부
     */
    public boolean addGuideCategory(String categoryName) {
        try {
            return guideDAO.addGuideCategory(categoryName);
        } catch (SQLException e) {
            System.err.println("용어 카테고리 추가 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 용어 카테고리를 수정합니다.
     *
     * @param oldCategoryName 기존 카테고리 이름
     * @param newCategoryName 새 카테고리 이름
     * @return 수정 성공 여부
     */
    public boolean updateGuideCategory(String oldCategoryName, String newCategoryName) {
        try {
            return guideDAO.updateGuideCategory(oldCategoryName, newCategoryName);
        } catch (SQLException e) {
            System.err.println("용어 카테고리 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 용어 카테고리를 삭제합니다.
     *
     * @param categoryName 카테고리 이름
     * @return 삭제 성공 여부
     */
    public boolean deleteGuideCategory(String categoryName) {
        try {
            return guideDAO.deleteGuideCategory(categoryName);
        } catch (SQLException e) {
            System.err.println("용어 카테고리 삭제 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 모든 용어 카테고리를 조회합니다.
     *
     * @return 카테고리 이름 목록
     */
    public List<String> getAllGuideCategories() {
        try {
            return guideDAO.getAllGuideCategories();
        } catch (SQLException e) {
            System.err.println("카테고리 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
