package repository.dao.board;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import dto.board.AttachmentDTO;
import dto.board.FreeboardDTO;
import dto.board.FreeboardCommentDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import util.db.DBConnectionUtil;
import util.logging.LoggerConfig;

public class FreeboardDAO {
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	private static final Logger logger = LoggerConfig.getLogger(FreeboardDAO.class);

	// 공지사항 목록을 위한 캐싱 메서드
	private static final Map<String, Object> cache = new ConcurrentHashMap<>();
	private static final long CACHE_EXPIRY = 5 * 60 * 1000; // 5분

	// 누락된 CacheItem 클래스 추가
	private static class CacheItem {
		private final Object data;
		private final long expiry;
		private final long createdAt;

		public CacheItem(Object data, long expiry) {
			this.data = data;
			this.expiry = expiry;
			this.createdAt = System.currentTimeMillis();
		}

		public Object getData() {
			return data;
		}

		public boolean isExpired() {
			return System.currentTimeMillis() - createdAt > expiry;
		}
	}

	public List<FreeboardDTO> getNoticeList() throws SQLException {
		String cacheKey = "notice_list";
		CacheItem cacheItem = (CacheItem) cache.get(cacheKey);

		if (cacheItem != null && !cacheItem.isExpired()) {
			return (List<FreeboardDTO>) cacheItem.getData();
		}

		// 캐시에 없으면 DB에서 조회
		List<FreeboardDTO> noticeList = new ArrayList<>();
		String sql = "SELECT f.*, u.user_name FROM freeboard f " + "JOIN user u ON f.user_uid = u.user_uid "
				+ "WHERE f.freeboard_deleted = 'maintained' AND f.freeboard_notify = 'notification' "
				+ "ORDER BY f.freeboard_writetime DESC";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				FreeboardDTO post = createFreeboardFromResultSet(rs);
				post.setUserName(rs.getString("user_name"));
				noticeList.add(post);
			}
		} finally {
			closeResources();
		}

		// 결과를 캐시에 저장
		cache.put(cacheKey, new CacheItem(noticeList, CACHE_EXPIRY));
		return noticeList;
	}

	// DB 연결 가져오기
	private Connection getConnection() throws SQLException {
		return DBConnectionUtil.getConnection();
	}

	// 자원 해제 메서드
	private void closeResources() {
		try {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// ResultSet에서 DTO 객체 생성 유틸리티 메서드
	private FreeboardDTO createFreeboardFromResultSet(ResultSet rs) throws SQLException {
		FreeboardDTO freeboard = new FreeboardDTO();

		freeboard.setFreeboardUid(rs.getLong("freeboard_uid"));
		freeboard.setFreeboardTitle(rs.getString("freeboard_title"));
		freeboard.setFreeboardContents(rs.getString("freeboard_contents"));
		freeboard.setFreeboardRead(rs.getInt("freeboard_read"));
		freeboard.setFreeboardRecommend(rs.getInt("freeboard_recommend"));

		Timestamp writetime = rs.getTimestamp("freeboard_writetime");
		if (writetime != null) {
			freeboard.setFreeboardWritetime(writetime.toLocalDateTime());
		}

		Timestamp modifyTime = rs.getTimestamp("freeboard_modify_time");
		if (modifyTime != null) {
			freeboard.setFreeboardModifyTime(modifyTime.toLocalDateTime());
		}

		freeboard.setFreeboardAuthorIp(rs.getString("freeboard_author_ip"));
		freeboard.setFreeboardNotify(rs.getString("freeboard_notify"));
		freeboard.setFreeboardDeleted(rs.getString("freeboard_deleted"));
		freeboard.setUserUid(rs.getLong("user_uid"));

		return freeboard;
	}

	// 게시글 등록
	public boolean postFreeboard(FreeboardDTO post) throws SQLException {
		String sql = "INSERT INTO freeboard (freeboard_title, freeboard_contents, freeboard_read, "
				+ "freeboard_recommend, freeboard_writetime, freeboard_author_ip, "
				+ "freeboard_notify, freeboard_deleted, user_uid) "
				+ "VALUES (?, ?, 0, 0, NOW(), ?, 'common', 'maintained', ?)";

		try {
			conn = getConnection();
			conn.setAutoCommit(false); // 트랜잭션 시작

			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, post.getFreeboardTitle());
			pstmt.setString(2, post.getFreeboardContents());
			pstmt.setString(3, post.getFreeboardAuthorIp());
			pstmt.setLong(4, post.getUserUid());

			int result = pstmt.executeUpdate();
			boolean success = result > 0;

			if (success) {
				// 생성된 게시글 ID 가져오기
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						post.setFreeboardUid(generatedKeys.getLong(1));
					}
				}
				conn.commit();
				logger.info("게시글 등록 성공: ID=" + post.getFreeboardUid() + ", 작성자=" + post.getUserUid());
			} else {
				conn.rollback();
				logger.warning("게시글 등록 실패: 영향받은 행 없음");
			}

			return success;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					logger.severe("롤백 중 오류 발생: " + ex.getMessage());
				}
			}

			String errorCode = "DB_ERROR_" + System.currentTimeMillis();
			logger.severe("게시글 등록 중 오류 발생 [" + errorCode + "]: " + e.getMessage());
			logger.severe("SQL 상태: " + e.getSQLState() + ", 에러코드: " + e.getErrorCode());
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					logger.severe("AutoCommit 설정 복구 중 오류: " + e.getMessage());
				}
			}
			closeResources();
		}
	}

	// 모든 게시글 조회 (페이징 포함)
	public List<FreeboardDTO> getAllFreeboards(int page, int pageSize) throws SQLException {
		List<FreeboardDTO> freeboard = new ArrayList<>();
		String sql = "SELECT f.*, u.user_name, "
				+ "(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count "
				+ "FROM freeboard f " + "JOIN user u ON f.user_uid = u.user_uid "
				+ "WHERE f.freeboard_deleted = 'maintained' "
				+ "ORDER BY f.freeboard_notify DESC, f.freeboard_writetime DESC " + "LIMIT ? OFFSET ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pageSize);
			pstmt.setInt(2, (page - 1) * pageSize);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				FreeboardDTO post = createFreeboardFromResultSet(rs);
				// 추가 정보 설정
				post.setUserName(rs.getString("user_name"));
				post.setCommentCount(rs.getInt("comment_count"));
				freeboard.add(post);
			}

			return freeboard;
		} finally {
			closeResources();
		}
	}

	// 기본 모든 게시글 조회 (페이징 없음 - 오버로딩)
	public List<FreeboardDTO> getAllFreeboards() throws SQLException {
		return getAllFreeboards(1, 100); // 기본값으로 첫 페이지, 100개 항목
	}

	// ID로 게시글 조회
	public FreeboardDTO getFreeboardById(long postId) throws SQLException {
		FreeboardDTO post = null;
		String sql = "SELECT f.*, u.user_name, "
				+ "(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count "
				+ "FROM freeboard f " + "JOIN user u ON f.user_uid = u.user_uid "
				+ "WHERE f.freeboard_uid = ? AND f.freeboard_deleted = 'maintained'";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				post = createFreeboardFromResultSet(rs);
				post.setUserName(rs.getString("user_name"));
				post.setCommentCount(rs.getInt("comment_count"));

				// 조회수 증가
				updateReadCount(postId);
			}

			return post;
		} finally {
			closeResources();
		}
	}

	// 조회수 증가
	private void updateReadCount(long postId) throws SQLException {
		String sql = "UPDATE freeboard SET freeboard_read = freeboard_read + 1 WHERE freeboard_uid = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, postId);
			pstmt.executeUpdate();
		}
	}

	/**
	 * 조회수 증가 (세션 기반 중복 방지)
	 * 
	 * @param postId  게시글 ID
	 * @param request HTTP 요청 객체
	 * @throws SQLException SQL 예외 발생 시
	 */
	public void updateReadCount(long postId, jakarta.servlet.http.HttpServletRequest request) throws SQLException {
		// 세션이 없으면 그냥 조회수 증가
		if (request == null) {
			System.out.println("Request 객체가 null이어서 단순 조회수 증가 처리: " + postId);
			updateReadCount(postId);
			return;
		}

		// increaseReadCount 파라미터 체크 (false인 경우 증가 안 함)
		String increaseParam = request.getParameter("increaseReadCount");
		if (increaseParam != null && "false".equalsIgnoreCase(increaseParam)) {
			System.out.println("increaseReadCount=false 파라미터로 조회수 증가 생략: " + postId);
			return;
		}

		jakarta.servlet.http.HttpSession session = request.getSession();
		// 세션 타임아웃 설정 (30분)
		session.setMaxInactiveInterval(1800);

		// 세션에서 방문한 게시글 목록 가져오기
		@SuppressWarnings("unchecked")
		java.util.Set<Long> viewedPosts = (java.util.Set<Long>) session.getAttribute("VIEWED_FREEBOARD_POSTS");

		// 세션에 방문 기록이 없으면 새로 생성
		if (viewedPosts == null) {
			viewedPosts = new java.util.HashSet<>();
			session.setAttribute("VIEWED_FREEBOARD_POSTS", viewedPosts);
			System.out.println("새 세션 생성 및 조회 기록 초기화: " + session.getId());
		}

		// 이미 방문한 게시글이면 조회수 증가 생략
		if (viewedPosts.contains(postId)) {
			System.out.println("이미 조회한 게시글이므로 조회수 증가 생략: " + postId + " (세션 ID: " + session.getId() + ")");
			return;
		}

		// 방문 기록에 추가
		viewedPosts.add(postId);
		System.out.println("조회 기록에 게시글 추가 및 조회수 증가: " + postId + " (세션 ID: " + session.getId() + ")");

		// 조회수 증가 쿼리 실행
		updateReadCount(postId);
	}

	// 게시글 수정
	public boolean updateFreeboardById(FreeboardDTO post) throws SQLException {
		String sql = "UPDATE freeboard SET freeboard_title = ?, freeboard_contents = ?, "
				+ "freeboard_modify_time = NOW() WHERE freeboard_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, post.getFreeboardTitle());
			pstmt.setString(2, post.getFreeboardContents());
			pstmt.setLong(3, post.getFreeboardUid());

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	// 게시글 삭제 (소프트 삭제)
	public boolean deleteFreeboardById(long postId) throws SQLException {
		String sql = "UPDATE freeboard SET freeboard_deleted = 'deleted' WHERE freeboard_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);

			int result = pstmt.executeUpdate();
			boolean success = result > 0;

			if (success) {
				logger.info("게시글 삭제 성공: ID=" + postId);
			} else {
				logger.warning("게시글 삭제 실패: ID=" + postId + ", 영향받은 행 없음");
			}

			return success;
		} catch (SQLException e) {
			logger.severe("게시글 삭제 중 오류 발생: ID=" + postId + ", 오류=" + e.getMessage());
			throw e;
		} finally {
			closeResources();
		}
	}

	// 게시글 숨김 처리
	public boolean hideFreeboardById(long postId, String hideReason) throws SQLException {
		// 실제로는 freeboard_deleted를 'hidden'으로 설정하고 이유를 로그 테이블에 기록
		String sql = "UPDATE freeboard SET freeboard_deleted = 'deleted' WHERE freeboard_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);

			int result = pstmt.executeUpdate();

			if (result > 0) {
				// 로그 테이블에 숨김 이유 기록 (log_delete_post 테이블 활용)
				logPostDeletion(postId, hideReason);
				return true;
			}
			return false;
		} finally {
			closeResources();
		}
	}

	// 게시글 삭제 로그 기록
	private void logPostDeletion(long postId, String reason) throws SQLException {
		String logSql = "INSERT INTO log_delete_post (log_delete_boardtype, log_deleted_post_uid, "
				+ "log_delete_date, user_uid) " + "VALUES ('freeboard', ?, NOW(), ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(logSql);
			pstmt.setLong(1, postId);
			pstmt.setLong(2, 0); // 관리자 ID

			pstmt.executeUpdate();
		} finally {
			closeResources();
		}
	}

	// 공지사항 지정/해제
	public boolean setNoticeById(long postId, boolean isNotice) throws SQLException {
		String notifyValue = isNotice ? "notification" : "common";
		String sql = "UPDATE freeboard SET freeboard_notify = ? WHERE freeboard_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, notifyValue);
			pstmt.setLong(2, postId);

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	// 총 게시물 수 조회 (페이징용)
	public int getTotalCount() throws SQLException {
		String sql = "SELECT COUNT(*) FROM freeboard WHERE freeboard_deleted = 'maintained'";

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
	 * 게시글 신고
	 */
	public boolean reportFreeboard(long postId, long reporterId, String reason, String category) throws SQLException {
		// 게시글 존재 여부 확인
		FreeboardDTO post = getFreeboardById(postId);
		if (post == null) {
			return false;
		}

		// 중복 신고 확인 (이 부분은 수정 필요 없음)

		// 신고 로직 수정
		String sql = "INSERT INTO report (report_target_type, report_reason, report_status, "
				+ "report_createtime, report_user_uid, target_user_uid) " + "VALUES (?, ?, 'active', NOW(), ?, ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);

			// category를 report_target_type ENUM 값으로 변환
			// 클라이언트에서 받은 category가 ENUM에 맞지 않으면 기본값 사용
			String targetType = convertCategoryToEnum(category);

			pstmt.setString(1, targetType); // report_target_type (ENUM 값)
			pstmt.setString(2, reason); // report_reason
			pstmt.setLong(3, reporterId); // report_user_uid
			pstmt.setLong(4, post.getUserUid()); // target_user_uid

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 카테고리 문자열을 DB ENUM 값으로 변환
	 */
	private String convertCategoryToEnum(String category) {
		// 클라이언트에서 받은 카테고리 값에 따라 DB ENUM에 맞는 값으로 변환
		switch (category.toLowerCase()) {
		case "spam":
		case "ad":
		case "spam_ad":
			return "spam_ad";
		case "profanity":
		case "hate":
		case "hate_speech":
		case "profanity_hate_speech":
			return "profanity_hate_speech";
		case "adult":
		case "adult_content":
			return "adult_content";
		case "impersonation":
		case "fraud":
		case "impersonation_fraud":
			return "impersonation_fraud";
		case "copyright":
		case "copyright_infringement":
			return "copyright_infringement";
		default:
			return "spam_ad"; // 기본값
		}
	}

	/**
	 * 이용자 신고
	 */
	public boolean reportUser(long targetUserId, long reporterId, String reason, String category) throws SQLException {
		String sql = "INSERT INTO report (report_target_type, report_reason, report_status, "
				+ "report_createtime, report_user_uid, target_user_uid) " + "VALUES (?, ?, 'active', NOW(), ?, ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);

			// category를 report_target_type ENUM 값으로 변환
			String targetType = convertCategoryToEnum(category);

			pstmt.setString(1, targetType); // report_target_type (ENUM 값)
			pstmt.setString(2, reason); // report_reason
			pstmt.setLong(3, reporterId); // report_user_uid
			pstmt.setLong(4, targetUserId); // target_user_uid

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 이용자 제재
	 */
	public boolean penalizeUser(long targetUserId, long adminId, String reason, String category, String penaltyType,
			int duration) throws SQLException {
		try {
			conn = getConnection();
			conn.setAutoCommit(false); // 트랜잭션 시작

			// 1. 신고 기록 추가 - 실제 DB 스키마와 일치하도록 수정
			String reportSql = "INSERT INTO report (report_target_type, report_reason, report_status, "
					+ "report_createtime, report_user_uid, target_user_uid) " + "VALUES (?, ?, 'active', NOW(), ?, ?)";

			pstmt = conn.prepareStatement(reportSql);
			// category를 report_target_type ENUM 값으로 변환
			String targetType = convertCategoryToEnum(category);
			pstmt.setString(1, targetType); // report_target_type (ENUM 값)
			pstmt.setString(2, reason); // report_reason
			pstmt.setLong(3, adminId); // report_user_uid (관리자 ID)
			pstmt.setLong(4, targetUserId); // target_user_uid (대상 사용자)

			boolean reportResult = pstmt.executeUpdate() > 0;

			if (!reportResult) {
				conn.rollback();
				return false;
			}

			// 2. 제재 정보 추가 - 실제 DB 스키마와 일치하도록 수정
			String penaltySql = "INSERT INTO user_penalty (user_uid, penalty_reason, "
					+ "penalty_start_date, penalty_end_date, penalty_status, penalty_duration) "
					+ "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? DAY), 'active', ?)";

			pstmt = conn.prepareStatement(penaltySql);
			pstmt.setLong(1, targetUserId); // user_uid
			pstmt.setString(2, reason); // penalty_reason
			pstmt.setInt(3, duration); // penalty_end_date (계산)
			pstmt.setString(4, duration > 0 ? "temporary" : "permanent"); // penalty_duration

			boolean penaltyResult = pstmt.executeUpdate() > 0;

			if (!penaltyResult) {
				conn.rollback();
				return false;
			}

			// 3. 사용자 상태 업데이트 (이 부분은 이미 올바름)
			String statusSql = "UPDATE user SET user_status = ? WHERE user_uid = ?";

			pstmt = conn.prepareStatement(statusSql);
			pstmt.setString(1, "restricted");
			pstmt.setLong(2, targetUserId);

			boolean statusResult = pstmt.executeUpdate() > 0;

			if (!statusResult) {
				conn.rollback();
				return false;
			}

			conn.commit();
			return true;
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
	 * 사용자 상태 업데이트 (제재 적용)
	 */
	private boolean updateUserStatus(long userId, String status) throws SQLException {
		String sql = "UPDATE user SET user_status = ? WHERE user_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setLong(2, userId);

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 첨부파일 삭제
	 */
	public boolean deleteAttachByFilename(long postId, String filename, String reason, long adminId)
			throws SQLException {
		// 1. 로그 테이블에 삭제 이유 기록 - SQL 쿼리와 파라미터 수 일치시키기
		String logSql = "INSERT INTO log_delete_post (log_delete_boardtype, log_deleted_post_uid, "
				+ "log_delete_date, user_uid) " + "VALUES ('freeboard', ?, NOW(), ?)";

		// 2. 첨부파일 테이블에서 파일 정보 삭제
		String deleteSql = "DELETE FROM freeboard_attach WHERE freeboard_uid = ? AND file_name = ?";

		try {
			conn = getConnection();
			conn.setAutoCommit(false); // 트랜잭션 시작

			// 로그 기록 - 파라미터 개수 수정 (2개만 필요)
			pstmt = conn.prepareStatement(logSql);
			pstmt.setLong(1, postId); // log_deleted_post_uid
			pstmt.setLong(2, adminId); // user_uid

			int logResult = pstmt.executeUpdate();

			// 파일 정보 삭제
			pstmt = conn.prepareStatement(deleteSql);
			pstmt.setLong(1, postId);
			pstmt.setString(2, filename);

			int deleteResult = pstmt.executeUpdate();

			if (logResult > 0 && deleteResult > 0) {
				conn.commit();
				return true;
			} else {
				conn.rollback();
				return false;
			}
		} catch (SQLException e) {
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException ex) {
				}
			throw e;
		} finally {
			if (conn != null)
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
				}
			closeResources();
		}
	}

	/**
	 * 게시글 검색
	 */
	public List<FreeboardDTO> searchFreeboards(String keyword, String searchType, int page, int pageSize)
			throws SQLException {
		List<FreeboardDTO> searchResults = new ArrayList<>();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT f.*, u.user_name, ");
		sql.append(
				"(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count ");
		sql.append("FROM freeboard f ");
		sql.append("JOIN user u ON f.user_uid = u.user_uid ");
		sql.append("WHERE f.freeboard_deleted = 'maintained' ");

		// 검색 조건 추가
		if (searchType.equals("title")) {
			sql.append("AND f.freeboard_title LIKE ? ");
		} else if (searchType.equals("content")) {
			sql.append("AND f.freeboard_contents LIKE ? ");
		} else if (searchType.equals("author")) {
			sql.append("AND u.user_name LIKE ? ");
		} else {
			sql.append("AND (f.freeboard_title LIKE ? OR f.freeboard_contents LIKE ?) ");
		}

		sql.append("ORDER BY f.freeboard_notify DESC, f.freeboard_writetime DESC ");
		sql.append("LIMIT ? OFFSET ?");

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql.toString());

			if (searchType.equals("title") || searchType.equals("content") || searchType.equals("author")) {
				pstmt.setString(1, "%" + keyword + "%");
				pstmt.setInt(2, pageSize);
				pstmt.setInt(3, (page - 1) * pageSize);
			} else {
				pstmt.setString(1, "%" + keyword + "%");
				pstmt.setString(2, "%" + keyword + "%");
				pstmt.setInt(3, pageSize);
				pstmt.setInt(4, (page - 1) * pageSize);
			}

			rs = pstmt.executeQuery();

			while (rs.next()) {
				FreeboardDTO post = createFreeboardFromResultSet(rs);
				post.setUserName(rs.getString("user_name"));
				post.setCommentCount(rs.getInt("comment_count"));
				searchResults.add(post);
			}

			return searchResults;
		} finally {
			closeResources();
		}
	}

	// 새로운 커서 기반 페이징 메서드 추가
	public List<FreeboardDTO> getNextFreeboards(long lastPostId, int pageSize) throws SQLException {
		List<FreeboardDTO> freeboard = new ArrayList<>();
		String sql = "SELECT f.*, u.user_name, "
				+ "(SELECT COUNT(*) FROM freeboard_comment fc WHERE fc.freeboard_uid = f.freeboard_uid) AS comment_count "
				+ "FROM freeboard f " + "JOIN user u ON f.user_uid = u.user_uid "
				+ "WHERE f.freeboard_deleted = 'maintained' " + "AND f.freeboard_uid < ? " + // 커서 조건
				"ORDER BY f.freeboard_uid DESC " + "LIMIT ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, lastPostId); // 마지막으로 본 게시글 ID
			pstmt.setInt(2, pageSize);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				FreeboardDTO post = createFreeboardFromResultSet(rs);
				post.setUserName(rs.getString("user_name"));
				post.setCommentCount(rs.getInt("comment_count"));
				freeboard.add(post);
			}

			return freeboard;
		} finally {
			closeResources();
		}
	}

	// 간단한 목록 조회용 메서드 추가 (작성자 이름 없이)
	public List<FreeboardDTO> getSimpleFreeboardList(int page, int pageSize) throws SQLException {
		List<FreeboardDTO> freeboard = new ArrayList<>();
		String sql = "SELECT f.* FROM freeboard f " + "WHERE f.freeboard_deleted = 'maintained' "
				+ "ORDER BY f.freeboard_notify DESC, f.freeboard_writetime DESC " + "LIMIT ? OFFSET ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pageSize);
			pstmt.setInt(2, (page - 1) * pageSize);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				FreeboardDTO post = createFreeboardFromResultSet(rs);
				// 작성자 정보 조회 없이 기본 데이터만 설정
				freeboard.add(post);
			}

			return freeboard;
		} finally {
			closeResources();
		}
	}

	/**
	 * 첨부파일 추가
	 */
	public boolean addAttachment(long postId, String fileName, String filePath, long fileSize) throws SQLException {
		String sql = "INSERT INTO freeboard_attach (freeboard_uid, file_name, file_path, file_size, upload_date) "
				+ "VALUES (?, ?, ?, ?, NOW())";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setString(2, fileName);
			pstmt.setString(3, filePath);
			pstmt.setLong(4, fileSize);

			return pstmt.executeUpdate() > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 첨부파일 조회
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
	 * 파일명으로 첨부파일 조회
	 */
	public AttachmentDTO getAttachmentByFilename(String filename) throws SQLException {
		String sql = "SELECT * FROM freeboard_attach WHERE file_path = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, filename);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				AttachmentDTO attachment = new AttachmentDTO();
				attachment.setAttachId(rs.getLong("attach_uid"));
				attachment.setPostId(rs.getLong("freeboard_uid"));
				attachment.setFileName(rs.getString("file_name"));
				attachment.setFilePath(rs.getString("file_path"));
				attachment.setFileSize(rs.getLong("file_size"));

				if (rs.getTimestamp("upload_date") != null) {
					attachment.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
				}

				return attachment;
			}

			return null;
		} finally {
			closeResources();
		}
	}

	// 자유게시판 댓글 관련 메서드

	/**
	 * 게시글의 모든 댓글 조회
	 */
	public List<FreeboardCommentDTO> getCommentsByPostId(long postId) throws SQLException {
		List<FreeboardCommentDTO> comments = new ArrayList<>();
		String sql = "SELECT c.*, u.user_name FROM freeboard_comment c " + "JOIN user u ON c.user_uid = u.user_uid "
				+ "WHERE c.freeboard_uid = ? " + "ORDER BY c.freeboard_comment_writetime ASC";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				FreeboardCommentDTO comment = new FreeboardCommentDTO();
				comment.setFreeboardCommentUid(rs.getLong("freeboard_comment_uid"));
				comment.setFreeboardCommentContents(rs.getString("freeboard_comment_contents"));
				comment.setFreeboardCommentWritetime(rs.getTimestamp("freeboard_comment_writetime").toLocalDateTime());

				if (rs.getTimestamp("freeboard_comment_modifytime") != null) {
					comment.setFreeboardCommentModifytime(
							rs.getTimestamp("freeboard_comment_modifytime").toLocalDateTime());
				}

				comment.setFreeboardCommentAuthorIp(rs.getString("freeboard_comment_author_ip"));
				comment.setFreeboardUid(rs.getLong("freeboard_uid"));
				comment.setUserUid(rs.getLong("user_uid"));
				comment.setUserName(rs.getString("user_name"));

				comments.add(comment);
			}
		} finally {
			closeResources();
		}

		return comments;
	}

	/**
	 * 새 댓글 등록
	 */
	public boolean addComment(FreeboardCommentDTO comment) throws SQLException {
		String sql = "INSERT INTO freeboard_comment "
				+ "(freeboard_comment_contents, freeboard_comment_writetime, freeboard_comment_author_ip, freeboard_uid, user_uid) "
				+ "VALUES (?, NOW(), ?, ?, ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, comment.getFreeboardCommentContents());
			pstmt.setString(2, comment.getFreeboardCommentAuthorIp());
			pstmt.setLong(3, comment.getFreeboardUid());
			pstmt.setLong(4, comment.getUserUid());

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 댓글 수정
	 */
	public boolean updateComment(FreeboardCommentDTO comment) throws SQLException {
		String sql = "UPDATE freeboard_comment SET " + "freeboard_comment_contents = ?, "
				+ "freeboard_comment_modifytime = NOW() " + "WHERE freeboard_comment_uid = ? AND user_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, comment.getFreeboardCommentContents());
			pstmt.setLong(2, comment.getFreeboardCommentUid());
			pstmt.setLong(3, comment.getUserUid());

			int result = pstmt.executeUpdate();

			// 수정 로그 저장
			if (result > 0) {
				logModifyComment(comment.getFreeboardCommentUid(), comment.getUserUid(), "freeboard");
			}

			return result > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 댓글 삭제
	 */
	public boolean deleteComment(long commentId, long userId) throws SQLException {
		// 관리자 여부 확인을 위해 댓글 정보 조회
		FreeboardCommentDTO comment = getCommentById(commentId);
		if (comment == null) {
			return false;
		}

		String sql = "DELETE FROM freeboard_comment WHERE freeboard_comment_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, commentId);

			int result = pstmt.executeUpdate();

			// 삭제 로그 저장
			if (result > 0) {
				logDeleteComment(commentId, userId, "freeboard");
			}

			return result > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 댓글 상세 조회
	 */
	public FreeboardCommentDTO getCommentById(long commentId) throws SQLException {
		String sql = "SELECT c.*, u.user_name FROM freeboard_comment c " + "JOIN user u ON c.user_uid = u.user_uid "
				+ "WHERE c.freeboard_comment_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, commentId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				FreeboardCommentDTO comment = new FreeboardCommentDTO();
				comment.setFreeboardCommentUid(rs.getLong("freeboard_comment_uid"));
				comment.setFreeboardCommentContents(rs.getString("freeboard_comment_contents"));
				comment.setFreeboardCommentWritetime(rs.getTimestamp("freeboard_comment_writetime").toLocalDateTime());

				if (rs.getTimestamp("freeboard_comment_modifytime") != null) {
					comment.setFreeboardCommentModifytime(
							rs.getTimestamp("freeboard_comment_modifytime").toLocalDateTime());
				}

				comment.setFreeboardCommentAuthorIp(rs.getString("freeboard_comment_author_ip"));
				comment.setFreeboardUid(rs.getLong("freeboard_uid"));
				comment.setUserUid(rs.getLong("user_uid"));
				comment.setUserName(rs.getString("user_name"));

				return comment;
			}
			return null;
		} finally {
			closeResources();
		}
	}

	/**
	 * 댓글 수정 로그 저장
	 */
	private void logModifyComment(long commentId, long userId, String boardType) throws SQLException {
		String sql = "INSERT INTO log_modify_comment "
				+ "(log_modify_boardtype, log_modify_date, log_modify_comment_uid, user_uid) "
				+ "VALUES (?, NOW(), ?, ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, boardType);
			pstmt.setLong(2, commentId);
			pstmt.setLong(3, userId);

			pstmt.executeUpdate();
		} finally {
			closeResources();
		}
	}

	/**
	 * 댓글 삭제 로그 저장
	 */
	private void logDeleteComment(long commentId, long userId, String boardType) throws SQLException {
		String sql = "INSERT INTO log_delete_comment "
				+ "(log_delete_boardtype, log_delete_date, log_deleted_comment_uid, user_uid) "
				+ "VALUES (?, NOW(), ?, ?)";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, boardType);
			pstmt.setLong(2, commentId);
			pstmt.setLong(3, userId);

			pstmt.executeUpdate();
		} finally {
			closeResources();
		}
	}

	/**
	 * 첨부파일 다운로드 수 증가
	 */
	public boolean increaseDownloadCount(long attachId) throws SQLException {
		String sql = "UPDATE freeboard_attach SET download_count = download_count + 1 WHERE attach_uid = ?";

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, attachId);

			int result = pstmt.executeUpdate();
			return result > 0;
		} finally {
			closeResources();
		}
	}

	// 추천 관련 DAO 메서드 시작

	/**
	 * 사용자가 특정 게시글을 추천했는지 확인
	 * 
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 추천했으면 true, 아니면 false
	 * @throws SQLException SQL 예외
	 */
	public boolean hasUserRecommended(long postId, int userId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM log_recommend "
				+ "WHERE log_recommend_boardtype = 'freeboard' AND log_recommend_post_id = ? AND user_uid = ?";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setInt(2, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
			return false;
		} finally {
			closeResources();
		}
	}

	/**
	 * 추천 로그 삭제
	 * 
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 성공하면 true, 실패하면 false
	 * @throws SQLException SQL 예외
	 */
	public boolean removeRecommendationLog(long postId, int userId) throws SQLException {
		String sql = "DELETE FROM log_recommend "
				+ "WHERE log_recommend_boardtype = 'freeboard' AND log_recommend_post_id = ? AND user_uid = ?";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 추천 로그 추가
	 * 
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 성공하면 true, 실패하면 false
	 * @throws SQLException SQL 예외
	 */
	public boolean addRecommendationLog(long postId, int userId) throws SQLException {
		String sql = "INSERT INTO log_recommend (log_recommend_boardtype, log_recommend_post_id, log_recommend_date, user_uid) "
				+ "VALUES ('freeboard', ?, NOW(), ?)";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 게시글 추천 수 감소
	 * 
	 * @param postId 게시글 ID
	 * @return 성공하면 true, 실패하면 false
	 * @throws SQLException SQL 예외
	 */
	public boolean decrementFreeboardRecommendCount(long postId) throws SQLException {
		String sql = "UPDATE freeboard SET freeboard_recommend = freeboard_recommend - 1 WHERE freeboard_uid = ?";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			return pstmt.executeUpdate() > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 게시글 추천 수 증가
	 * 
	 * @param postId 게시글 ID
	 * @return 성공하면 true, 실패하면 false
	 * @throws SQLException SQL 예외
	 */
	public boolean incrementFreeboardRecommendCount(long postId) throws SQLException {
		String sql = "UPDATE freeboard SET freeboard_recommend = freeboard_recommend + 1 WHERE freeboard_uid = ?";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			return pstmt.executeUpdate() > 0;
		} finally {
			closeResources();
		}
	}

	/**
	 * 게시글의 현재 추천 수 조회
	 * 
	 * @param postId 게시글 ID
	 * @return 추천 수
	 * @throws SQLException SQL 예외
	 */
	public int getFreeboardRecommendCount(long postId) throws SQLException {
		String sql = "SELECT freeboard_recommend FROM freeboard WHERE freeboard_uid = ?";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("freeboard_recommend");
			}
			return 0; // 게시글이 없거나 추천 수가 없는 경우
		} finally {
			closeResources();
		}
	}

	/**
	 * 사용자의 게시글 추천 상태 조회
	 * 
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 추천 유형 ("like" 또는 null)
	 * @throws SQLException SQL 예외 발생 시
	 */
	public String getUserRecommendationType(long postId, long userId) throws SQLException {
		String sql = "SELECT recommendation_type FROM post_recommendation "
				+ "WHERE post_id = ? AND user_uid = ? AND post_type = 'freeboard'";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setLong(2, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("recommendation_type");
			}
			return null;
		} finally {
			closeResources();
		}
	}

	/**
	 * 게시글 추천 추가
	 * 
	 * @param postId             게시글 ID
	 * @param userId             사용자 ID
	 * @param recommendationType 추천 유형
	 * @return 성공 여부
	 * @throws SQLException SQL 예외 발생 시
	 */
	public boolean addRecommendation(long postId, long userId, String recommendationType) throws SQLException {
		String sql = "INSERT INTO post_recommendation (post_id, user_uid, post_type, recommendation_type, recommended_at) "
				+ "VALUES (?, ?, 'freeboard', ?, NOW())";
		String updateSql = "UPDATE freeboard SET freeboard_recommend = freeboard_recommend + 1 WHERE freeboard_uid = ?";
		try {
			conn = getConnection();
			conn.setAutoCommit(false);

			// 추천 기록 추가
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setLong(2, userId);
			pstmt.setString(3, recommendationType);
			int result = pstmt.executeUpdate();

			if (result > 0) {
				// 게시글 추천 수 업데이트
				pstmt = conn.prepareStatement(updateSql);
				pstmt.setLong(1, postId);
				int updateResult = pstmt.executeUpdate();
				if (updateResult > 0) {
					conn.commit();
					return true;
				}
			}
			conn.rollback();
			return false;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					logger.severe("추천 추가 중 롤백 오류: " + ex.getMessage());
				}
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					logger.severe("추천 추가 후 AutoCommit 설정 복구 오류: " + e.getMessage());
				}
			}
			closeResources();
		}
	}

	/**
	 * 게시글 추천 삭제
	 * 
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 성공 여부
	 * @throws SQLException SQL 예외 발생 시
	 */
	public boolean removeRecommendation(long postId, long userId) throws SQLException {
		String sql = "DELETE FROM post_recommendation WHERE post_id = ? AND user_uid = ? AND post_type = 'freeboard'";
		String updateSql = "UPDATE freeboard SET freeboard_recommend = GREATEST(0, freeboard_recommend - 1) WHERE freeboard_uid = ?";
		try {
			conn = getConnection();
			conn.setAutoCommit(false);

			// 추천 기록 삭제
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			pstmt.setLong(2, userId);
			int result = pstmt.executeUpdate();

			if (result > 0) {
				// 게시글 추천 수 업데이트
				pstmt = conn.prepareStatement(updateSql);
				pstmt.setLong(1, postId);
				int updateResult = pstmt.executeUpdate();
				if (updateResult > 0) {
					conn.commit();
					return true;
				}
			}
			conn.rollback();
			return false;
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					logger.severe("추천 삭제 중 롤백 오류: " + ex.getMessage());
				}
			}
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					logger.severe("추천 삭제 후 AutoCommit 설정 복구 오류: " + e.getMessage());
				}
			}
			closeResources();
		}
	}

	/**
	 * 게시글의 추천 수 조회
	 * 
	 * @param postId 게시글 ID
	 * @return 추천 수
	 * @throws SQLException SQL 예외 발생 시
	 */
	public int getLikeCountByPostId(long postId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM post_recommendation WHERE post_id = ? AND post_type = 'freeboard' AND recommendation_type = 'like'";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, postId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} finally {
			closeResources();
		}
	}
}