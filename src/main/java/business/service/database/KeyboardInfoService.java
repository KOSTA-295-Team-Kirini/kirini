package business.service.database;

import java.sql.SQLException;
import java.util.List;

import dto.keyboard.KeyboardInfoDTO;
import dto.keyboard.KeyboardScoreDTO;
import dto.keyboard.KeyboardTagDTO;
import repository.dao.database.KeyboardInfoDAO;

public class KeyboardInfoService {
    private KeyboardInfoDAO keyboardInfoDAO;
    
    public KeyboardInfoService() {
        this.keyboardInfoDAO = new KeyboardInfoDAO();
    }
    
    /**
     * 모든 키보드 정보 조회 (페이징 처리)
     */
    public List<KeyboardInfoDTO> getAllKeyboardInfos(int page, int pageSize) throws SQLException {
        return keyboardInfoDAO.getAllKeyboardInfos(page, pageSize);
    }
    
    /**
     * 키보드 정보 총 개수 조회
     */
    public int getTotalKeyboardCount() throws SQLException {
        return keyboardInfoDAO.getTotalKeyboardCount();
    }
    
    /**
     * 카테고리별 키보드 정보 조회
     */
    public List<KeyboardInfoDTO> getKeyboardInfosByCategory(String category, int page, int pageSize) throws SQLException {
        return keyboardInfoDAO.getKeyboardInfosByCategory(category, page, pageSize);
    }
    
    /**
     * 카테고리별 키보드 정보 총 개수 조회
     */
    public int getTotalKeyboardCountByCategory(String category) throws SQLException {
        return keyboardInfoDAO.getTotalKeyboardCountByCategory(category);
    }
    
    /**
     * 모든 카테고리 조회
     */
    public List<String> getAllCategories() throws SQLException {
        return keyboardInfoDAO.getAllCategories();
    }
    
    /**
     * ID로 키보드 정보 조회
     */
    public KeyboardInfoDTO getKeyboardInfoById(long keyboardId) throws SQLException {
        return keyboardInfoDAO.getKeyboardInfoById(keyboardId);
    }
    
    /**
     * 키보드 별점 목록 조회
     */
    public List<KeyboardScoreDTO> getKeyboardScoresByKeyboardId(long keyboardId) throws SQLException {
        return keyboardInfoDAO.getKeyboardScoresByKeyboardId(keyboardId);
    }
    
    /**
     * 키보드 태그 목록 조회
     */
    public List<KeyboardTagDTO> getKeyboardTagsByKeyboardId(long keyboardId) throws SQLException {
        return keyboardInfoDAO.getKeyboardTagsByKeyboardId(keyboardId);
    }
    
    /**
     * 사용자가 키보드를 스크랩했는지 확인
     */
    public boolean isKeyboardScrappedByUser(long keyboardId, long userId) throws SQLException {
        return keyboardInfoDAO.isKeyboardScrappedByUser(keyboardId, userId);
    }
    
    /**
     * 사용자의 키보드 별점 조회
     */
    public KeyboardScoreDTO getUserKeyboardScore(long keyboardId, long userId) throws SQLException {
        return keyboardInfoDAO.getUserKeyboardScore(keyboardId, userId);
    }
    
    /**
     * 키보드 한줄평 추가
     */
    public boolean addKeyboardComment(long keyboardId, long userId, String comment) throws SQLException {
        return keyboardInfoDAO.addKeyboardComment(keyboardId, userId, comment);
    }
    
    /**
     * 사용자가 한줄평 작성자인지 확인
     */
    public boolean isUserCommentOwner(long commentId, long userId) throws SQLException {
        return keyboardInfoDAO.isUserCommentOwner(commentId, userId);
    }
    
    /**
     * 관리자가 한줄평 삭제
     */
    public boolean deleteKeyboardCommentByAdmin(long commentId, long adminId, String reason) throws SQLException {
        return keyboardInfoDAO.deleteKeyboardCommentByAdmin(commentId, adminId, reason);
    }
    
    /**
     * 사용자가 자신의 한줄평 삭제
     */
    public boolean deleteKeyboardCommentByUser(long commentId, long userId) throws SQLException {
        return keyboardInfoDAO.deleteKeyboardCommentByUser(commentId, userId);
    }
    
    /**
     * 키보드 정보 검색
     */
    public List<KeyboardInfoDTO> searchKeyboardInfosByCondition(String keyword, String switchType, 
            String layoutType, String connectType, List<String> tags, int page, int pageSize) throws SQLException {
        return keyboardInfoDAO.searchKeyboardInfosByCondition(keyword, switchType, layoutType, connectType, tags, page, pageSize);
    }
    
    /**
     * 검색 결과 총 개수 조회
     */
    public int getTotalSearchResultCount(String keyword, String switchType, 
            String layoutType, String connectType, List<String> tags) throws SQLException {
        return keyboardInfoDAO.getTotalSearchResultCount(keyword, switchType, layoutType, connectType, tags);
    }
    
    /**
     * 모든 스위치 유형 조회
     */
    public List<String> getAllSwitchTypes() throws SQLException {
        return keyboardInfoDAO.getAllSwitchTypes();
    }
    
    /**
     * 모든 레이아웃 유형 조회
     */
    public List<String> getAllLayoutTypes() throws SQLException {
        return keyboardInfoDAO.getAllLayoutTypes();
    }
    
    /**
     * 모든 연결 유형 조회
     */
    public List<String> getAllConnectTypes() throws SQLException {
        return keyboardInfoDAO.getAllConnectTypes();
    }
    
    /**
     * 인기 태그 조회
     */
    public List<String> getPopularTags(int limit) throws SQLException {
        return keyboardInfoDAO.getPopularTags(limit);
    }
    
    /**
     * 키보드 스크랩
     */
    public boolean scrapKeyboardInfo(long keyboardId, long userId) throws SQLException {
        return keyboardInfoDAO.scrapKeyboardInfo(keyboardId, userId);
    }
    
    /**
     * 키보드 스크랩 취소
     */
    public boolean unsecrapKeyboardInfo(long keyboardId, long userId) throws SQLException {
        return keyboardInfoDAO.unsecrapKeyboardInfo(keyboardId, userId);
    }
    
    /**
     * 사용자의 태그 투표 조회
     */
    public String getUserTagVote(long keyboardId, long tagId, long userId) throws SQLException {
        return keyboardInfoDAO.getUserTagVote(keyboardId, tagId, userId);
    }
    
    /**
     * 태그 투표수 조회
     */
    public int getTagVoteCount(long keyboardId, long tagId) throws SQLException {
        return keyboardInfoDAO.getTagVoteCount(keyboardId, tagId);
    }
    
    /**
     * 태그 투표
     */
    public boolean voteKeyboardTag(long keyboardId, long tagId, long userId, boolean isUpvote) throws SQLException {
        return keyboardInfoDAO.voteKeyboardTag(keyboardId, tagId, userId, isUpvote);
    }
    
    /**
     * 태그 투표 수정
     */
    public boolean updateKeyboardTagVote(long keyboardId, long tagId, long userId, boolean isUpvote) throws SQLException {
        return keyboardInfoDAO.updateKeyboardTagVote(keyboardId, tagId, userId, isUpvote);
    }
    
    /**
     * 키보드에 태그가 이미 존재하는지 확인
     */
    public boolean isTagExistForKeyboard(long keyboardId, String tagName) throws SQLException {
        return keyboardInfoDAO.isTagExistForKeyboard(keyboardId, tagName);
    }
    
    /**
     * 태그 건의
     */
    public boolean suggestKeyboardTag(long keyboardId, String tagName, long userId, String reason) throws SQLException {
        return keyboardInfoDAO.suggestKeyboardTag(keyboardId, tagName, userId, reason);
    }
    
    /**
     * 키보드 별점 추가
     */
    public boolean addKeyboardScore(long keyboardId, long userId, int scoreValue, String review) throws SQLException {
        return keyboardInfoDAO.addKeyboardScore(keyboardId, userId, scoreValue, review);
    }
    
    /**
     * 키보드 별점 수정
     */
    public boolean updateKeyboardScore(long keyboardId, long userId, int scoreValue, String review) throws SQLException {
        return keyboardInfoDAO.updateKeyboardScore(keyboardId, userId, scoreValue, review);
    }
    
    /**
     * 키보드 평균 별점 조회
     */
    public double getAverageKeyboardScore(long keyboardId) throws SQLException {
        return keyboardInfoDAO.getAverageKeyboardScore(keyboardId);
    }
    
    /**
     * 키보드 별점 개수 조회
     */
    public int getKeyboardScoreCount(long keyboardId) throws SQLException {
        return keyboardInfoDAO.getKeyboardScoreCount(keyboardId);
    }
}