package business.service.guide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dto.keyboard.GuideDTO;
import util.db.DBConnectionUtil;

/**
 * 키보드 용어집 기능을 위한 서비스 클래스
 * DAO와 서비스 로직을 통합하여 하나의 클래스로 구현
 */
public class GuideService {

    /**
     * 모든 키보드 용어집 데이터를 가져옵니다.
     *
     * @return 키보드 용어집 리스트
     */
    public List<GuideDTO> getAllGuides() {
        List<GuideDTO> guides = new ArrayList<>();
        String sql = "SELECT * FROM keyboard_glossary ORDER BY keyboard_glossary_title";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                guides.add(convertToDTO(rs));
            }

        } catch (SQLException e) {
            System.err.println("키보드 용어집 데이터 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 로그 기록 추가 (필요시 적절한 로깅 프레임워크 사용)
            // Logger.error("키보드 용어집 데이터 조회 실패", e);
        }

        return guides;
    }

    /**
     * 키워드로 키보드 용어집을 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @return 검색된 키보드 용어집 리스트
     */
    public List<GuideDTO> searchGuidesByKeyword(String keyword) {
        List<GuideDTO> guides = new ArrayList<>();
        String sql = "SELECT * FROM keyboard_glossary WHERE keyboard_glossary_title LIKE ? OR keyboard_glossary_summary LIKE ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + keyword + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    guides.add(convertToDTO(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("키보드 용어집 검색 중 오류 발생 (키워드: " + keyword + "): " + e.getMessage());
            e.printStackTrace();
            // 로그 기록 추가 (필요시 적절한 로깅 프레임워크 사용)
            // Logger.error("키보드 용어집 검색 실패", e);
        }

        return guides;
    }

    /**
     * ID로 특정 키보드 용어를 가져옵니다.
     *
     * @param guideId 용어 ID
     * @return 해당 ID의 용어 정보
     */
    public GuideDTO getGuideById(long guideId) {
        String sql = "SELECT * FROM keyboard_glossary WHERE keyboard_glossary_uid = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, guideId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertToDTO(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("키보드 용어 상세 조회 중 오류 발생 (ID: " + guideId + "): " + e.getMessage());
            e.printStackTrace();
            // 로그 기록 추가 (필요시 적절한 로깅 프레임워크 사용)
            // Logger.error("키보드 용어 상세 조회 실패", e);
        }

        return null;
    }

    /**
     * ResultSet을 GuideDTO 객체로 변환합니다.
     *
     * @param rs 데이터베이스 조회 결과
     * @return GuideDTO 객체
     * @throws SQLException SQL 예외 발생 시
     */
    private GuideDTO convertToDTO(ResultSet rs) throws SQLException {
        GuideDTO guide = new GuideDTO();
        guide.setGuideId(rs.getLong("keyboard_glossary_uid"));
        guide.setTerm(rs.getString("keyboard_glossary_title"));
        guide.setDescription(rs.getString("keyboard_glossary_summary"));
        // URL 정보 매핑 추가
        String url = rs.getString("keyboard_glossary_url");
        if (url != null && !url.isEmpty()) {
            // URL은 별도 필드에 설정 (GuideDTO에 url 필드 있음) 
            guide.setUrl(url);

            // 설명에 URL 정보 추가 (선택적)
            // guide.setDescription(guide.getDescription() + "\n\n참고 링크: " + url);
        }

        guide.setCategory("keyboard"); // 기본 카테고리 설정

        // 날짜 정보가 DB에 없는 경우 현재 시간으로 설정
        guide.setCreateDate(LocalDateTime.now());
        guide.setUpdateDate(LocalDateTime.now());

        return guide;
    }
}