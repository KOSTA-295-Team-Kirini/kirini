package business.service.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.keyboard.KeyboardTagDTO;
import repository.dao.database.KeyboardInfoDAO;

/**
 * 키보드 정보 서비스 클래스
 */
public class KeyboardInfoService {
    private final KeyboardInfoDAO keyboardInfoDAO;

    public KeyboardInfoService() {
        this.keyboardInfoDAO = new KeyboardInfoDAO();
    }

    /**
     * 모든 키보드 정보 조회
     */
    public List<KeyboardInfoDTO> getAllKeyboardInfos(int page, int pageSize) {
        try {
            return keyboardInfoDAO.getAllKeyboardInfos(page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 키보드 상세 정보 조회
     */
    public KeyboardInfoDTO getKeyboardInfoById(long keyboardId) {
        try {
            return keyboardInfoDAO.getKeyboardInfoById(keyboardId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 키보드 상세 정보 조회 (별칭 메서드 - UserpageController 호환용)
     */
    public KeyboardInfoDTO getKeyboardById(long keyboardId) {
        return getKeyboardInfoById(keyboardId);
    }

    /**
     * 조건별 키보드 검색
     */
    public List<KeyboardInfoDTO> searchKeyboardInfosByCondition(String keyword, String manufacturer,
                                                                String switchType, String layoutType, String connectType, int page, int pageSize) {
        try {
            return keyboardInfoDAO.searchKeyboardInfosByCondition(
                    keyword, manufacturer, switchType, layoutType, connectType, page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 키보드 한줄평 추가
     */
    public boolean addKeyboardComment(KeyboardScoreDTO comment) {
        try {
            return keyboardInfoDAO.addKeyboardComment(comment);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 한줄평 목록 조회
     */
    public List<KeyboardScoreDTO> getKeyboardComments(long keyboardId) {
        try {
            return keyboardInfoDAO.getKeyboardComments(keyboardId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 한줄평 삭제 (관리자)
     */
    public boolean deleteKeyboardCommentById(long commentId, long userId, String userAuthority) {
        try {
            boolean isAdmin = "admin".equals(userAuthority);
            return keyboardInfoDAO.deleteKeyboardCommentById(commentId, userId, isAdmin);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 한줄평 삭제 (사용자 본인)
     */
    public boolean deleteKeyboardCommentById(long commentId, long userId) {
        try {
            return keyboardInfoDAO.deleteKeyboardCommentById(commentId, userId, false);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 스크랩 (즐겨찾기)
     */
    public boolean scrapKeyboardInfo(long keyboardId, long userId) {
        try {
            return keyboardInfoDAO.scrapKeyboardInfo(keyboardId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 이미 스크랩했는지 확인
     */
    public boolean hasAlreadyScrapped(long keyboardId, long userId) {
        try {
            return keyboardInfoDAO.hasAlreadyScrapped(keyboardId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자가 스크랩한 키보드 목록 조회
     */
    public List<KeyboardInfoDTO> getScrapsByUserId(long userId, int page, int pageSize) {
        try {
            return keyboardInfoDAO.getScrapsByUserId(userId, page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 사용자가 스크랩한 키보드 총 개수 조회
     */
    public int getTotalScrapCountByUserId(long userId) {
        try {
            return keyboardInfoDAO.getTotalScrapCountByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 키보드 태그 조회 (투표 정보 포함)
     */
    public List<KeyboardTagDTO> getKeyboardTagsWithVotes(long keyboardId, long userId) {
        try {
            return keyboardInfoDAO.getKeyboardTagsWithVotes(keyboardId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 키보드 태그 투표
     */
    public boolean voteKeyboardTag(long keyboardId, long tagId, long userId, String voteType) {
        try {
            return keyboardInfoDAO.voteKeyboardTag(keyboardId, tagId, userId, voteType);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 태그 제안 (새 태그 추가)
     */
    public boolean suggestKeyboardTag(String tagName, long keyboardId, long userId) {
        try {
            return keyboardInfoDAO.suggestKeyboardTag(tagName, keyboardId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 별점 추가
     */
    public boolean addKeyboardScore(KeyboardScoreDTO score) {
        try {
            return keyboardInfoDAO.addKeyboardScore(score);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 키보드 별점 수정
     */
    public boolean updateKeyboardScore(KeyboardScoreDTO score) {
        try {
            return keyboardInfoDAO.updateKeyboardScore(score);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자 별점 조회
     */
    public KeyboardScoreDTO getUserScore(long keyboardId, long userId) {
        try {
            return keyboardInfoDAO.getUserScore(keyboardId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 사용자가 작성한 키보드 별점 목록 조회
     */
    public List<KeyboardScoreDTO> getScoresByUserId(long userId, String sortBy, int page, int pageSize) {
        try {
            return keyboardInfoDAO.getScoresByUserId(userId, sortBy, page, pageSize);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 사용자가 작성한 키보드 별점 총 개수 조회
     */
    public int getTotalScoreCountByUserId(long userId) {
        try {
            return keyboardInfoDAO.getTotalScoreCountByUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 총 키보드 수 조회 (페이징용)
     */
    public int getTotalKeyboardCount() {
        try {
            return keyboardInfoDAO.getTotalKeyboardCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 검색 조건에 맞는 키보드 수 조회 (페이징용)
     */
    public int getFilteredKeyboardCount(String keyword, String manufacturer,
                                        String switchType, String layoutType, String connectType) {
        try {
            return keyboardInfoDAO.getFilteredKeyboardCount(
                    keyword, manufacturer, switchType, layoutType, connectType);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 제조사 목록 조회
     */
    public List<String> getAllManufacturers() {
        try {
            return keyboardInfoDAO.getAllManufacturers();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 스위치 타입 목록 조회
     */
    public List<String> getAllSwitchTypes() {
        try {
            return keyboardInfoDAO.getAllSwitchTypes();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 레이아웃 타입 목록 조회
     */
    public List<String> getAllLayoutTypes() {
        try {
            return keyboardInfoDAO.getAllLayoutTypes();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 연결 타입 목록 조회
     */
    public List<String> getAllConnectTypes() {
        try {
            return keyboardInfoDAO.getAllConnectTypes();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
