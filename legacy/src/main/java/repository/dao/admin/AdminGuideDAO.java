package repository.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dto.keyboard.GuideDTO;
import util.db.DBConnectionUtil;

/**
 * 관리자용 키보드 용어집 관리 DAO 클래스
 */
public class AdminGuideDAO {
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    /**
     * 키보드 용어를 등록합니다.
     *
     * @param guide 등록할 용어 정보
     * @return 등록 성공 여부
     */
    public boolean addGuide(GuideDTO guide) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "INSERT INTO keyboard_glossary (keyboard_glossary_title, keyboard_glossary_summary, keyboard_glossary_url) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, guide.getTerm());

            // URL 정보 처리
            String description = guide.getDescription();
            String url = "";
            if (description.contains("참고 링크:")) {
                int index = description.indexOf("참고 링크:");
                url = description.substring(index + "참고 링크:".length()).trim();
                // 설명에서 URL 부분 제거
                description = description.substring(0, index).trim();
                pstmt.setString(2, description);
            } else {
                pstmt.setString(2, description);
            }
            pstmt.setString(3, url);

            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 키보드 용어를 수정합니다.
     *
     * @param guide 수정할 용어 정보
     * @return 수정 성공 여부
     */
    public boolean updateGuide(GuideDTO guide) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE keyboard_glossary SET keyboard_glossary_title = ?, keyboard_glossary_summary = ?, keyboard_glossary_url = ? WHERE keyboard_glossary_uid = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, guide.getTerm());

            // URL 정보 처리
            String description = guide.getDescription();
            String url = "";
            if (description.contains("참고 링크:")) {
                int index = description.indexOf("참고 링크:");
                url = description.substring(index + "참고 링크:".length()).trim();
                // 설명에서 URL 부분 제거
                description = description.substring(0, index).trim();
                pstmt.setString(2, description);
            } else {
                pstmt.setString(2, description);
            }
            pstmt.setString(3, url);
            pstmt.setLong(4, guide.getId());

            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 키보드 용어를 삭제합니다.
     *
     * @param guideId 삭제할 용어 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteGuide(long guideId) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "DELETE FROM keyboard_glossary WHERE keyboard_glossary_uid = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, guideId);

            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 모든 용어를 조회합니다.
     *
     * @return 용어 목록
     */
    public List<GuideDTO> getAllGuides() throws SQLException {
        List<GuideDTO> guideList = new ArrayList<>();

        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT * FROM keyboard_glossary ORDER BY keyboard_glossary_title";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                GuideDTO guide = new GuideDTO();
                guide.setId(rs.getLong("keyboard_glossary_uid"));
                guide.setTerm(rs.getString("keyboard_glossary_title"));
                guide.setDescription(rs.getString("keyboard_glossary_summary"));

                // URL이 있으면 설명에 추가
                String url = rs.getString("keyboard_glossary_url");
                if (url != null && !url.isEmpty()) {
                    guide.setUrl(url);
                    String description = guide.getDescription();
                    if (!description.contains("참고 링크:")) {
                        guide.setDescription(description + "\n\n참고 링크: " + url);
                    }
                }

                // 카테고리 ID가 있으면 설정
                Object categoryIdObj = rs.getObject("category_id");
                if (categoryIdObj != null) {
                    guide.setCategory(getCategoryNameById(rs.getLong("category_id")));
                }

                guideList.add(guide);
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }

        return guideList;
    }

    /**
     * 용어 카테고리 목록을 조회합니다.
     *
     * @return 카테고리 이름 목록
     */
    public List<String> getAllGuideCategories() throws SQLException {
        List<String> categoryList = new ArrayList<>();

        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT category_name FROM keyboard_glossary_category ORDER BY category_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                categoryList.add(rs.getString("category_name"));
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }

        return categoryList;
    }

    /**
     * 용어 카테고리를 추가합니다.
     *
     * @param categoryName 카테고리 이름
     * @return 추가 성공 여부
     */
    public boolean addGuideCategory(String categoryName) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "INSERT INTO keyboard_glossary_category (category_name) VALUES (?)";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, categoryName);

            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 용어 카테고리 이름을 수정합니다.
     *
     * @param oldCategoryName 기존 카테고리 이름
     * @param newCategoryName 새 카테고리 이름
     * @return 수정 성공 여부
     */
    public boolean updateGuideCategory(String oldCategoryName, String newCategoryName) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "UPDATE keyboard_glossary_category SET category_name = ? WHERE category_name = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newCategoryName);
            pstmt.setString(2, oldCategoryName);

            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 용어 카테고리를 삭제합니다.
     *
     * @param categoryName 카테고리 이름
     * @return 삭제 성공 여부
     */
    public boolean deleteGuideCategory(String categoryName) throws SQLException {
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "DELETE FROM keyboard_glossary_category WHERE category_name = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, categoryName);

            return pstmt.executeUpdate() > 0;
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 카테고리 ID로 카테고리 이름을 조회합니다.
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리 이름
     */
    private String getCategoryNameById(long categoryId) throws SQLException {
        String categoryName = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "SELECT category_name FROM keyboard_glossary_category WHERE category_id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                categoryName = rs.getString("category_name");
            }
        } finally {
            DBConnectionUtil.close(rs, pstmt, conn);
        }

        return categoryName;
    }
}
