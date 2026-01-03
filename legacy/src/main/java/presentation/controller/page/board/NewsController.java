package presentation.controller.page.board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import business.service.news.NewsService;
import dto.board.NewsCommentDTO;
import dto.board.NewsDTO;
import dto.user.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import presentation.controller.page.Controller;
import repository.dao.board.NewsDAO;
import util.web.IpUtil;
import repository.dao.board.NewsDAO;

/**
 * 키보드 소식 게시판 관련 요청을 처리하는 컨트롤러
 * URL 패턴: /news.do 형식 지원
 */
@WebServlet({"/news/*", "/news.do"})
public class NewsController extends HttpServlet implements Controller {
    private static final long serialVersionUID = 1L;
    private NewsService newsService;
    private util.web.RequestRouter router;

    // LocalDateTime 직렬화/역직렬화를 위한 Gson 설정
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    // LocalDateTime 어댑터
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        newsService = new NewsService();

        // 라우터 설정
        initRequestRouter();
    }

    /**
     * 요청 라우터 초기화
     */
    private void initRequestRouter() {
        router = new util.web.RequestRouter();

        // GET 요청 JSON 라우터 설정
        router.getJson("/", (req, res) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "뉴스 게시판 API");
            return result;
        });

        // 게시글 삭제 API 추가
        router.postJson("/delete", (req, res) -> {
            try {
                // 로그인 확인
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");

                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }

                // 파라미터 가져오기 (action, id 둘 다 지원)
                String idParam = req.getParameter("id");
                System.out.println("삭제 요청 ID: " + idParam);

                if (idParam == null || idParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "게시글 ID가 제공되지 않았습니다.");
                    return errorResult;
                }

                try {
                    long newsId = Long.parseLong(idParam.trim());

                    // 원본 게시글 가져오기
                    NewsDTO original = newsService.getNewsById(newsId);

                    if (original == null || "deleted".equals(original.getNewsDeleted())) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("status", "error");
                        errorResult.put("success", false);
                        errorResult.put("message", "존재하지 않는 게시글입니다.");
                        return errorResult;
                    }

                    // 작성자 또는 관리자만 삭제 가능
                    if (original.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("status", "error");
                        errorResult.put("success", false);
                        errorResult.put("message", "삭제 권한이 없습니다.");
                        return errorResult;
                    }

                    // 게시글 삭제
                    boolean success = newsService.deleteNewsById(newsId, user.getUserId(), user.getUserLevel() >= 3 ? "admin" : "user");

                    Map<String, Object> result = new HashMap<>();
                    if (success) {
                        result.put("status", "success");
                        result.put("success", true);
                        result.put("message", "게시글이 삭제되었습니다.");
                    } else {
                        result.put("status", "error");
                        result.put("success", false);
                        result.put("message", "게시글 삭제에 실패했습니다.");
                    }
                    return result;

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "잘못된 게시글 ID입니다.");
                    return errorResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });

        router.getJson("/list", (req, res) -> {
            int page = 1;
            int pageSize = 10;

            try {
                if (req.getParameter("page") != null) {
                    page = Integer.parseInt(req.getParameter("page"));
                }

                if (req.getParameter("pageSize") != null) {
                    pageSize = Integer.parseInt(req.getParameter("pageSize"));
                }
            } catch (NumberFormatException e) {
                // 기본값 사용
            }

            List<NewsDTO> newsList = newsService.getAllNews(page, pageSize);
            int totalCount = newsService.getTotalNewsCount();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("newsList", newsList);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalCount", totalCount);

            return result;
        });

        router.getJson("/view", (req, res) -> {
            try {
                // postId와 id 파라미터 모두 지원 (클라이언트 호환성을 위해)
                String idParam = req.getParameter("id");
                System.out.println("받은 뉴스 id 파라미터: " + idParam);

                if (idParam == null || idParam.isEmpty()) {
                    idParam = req.getParameter("postId");
                    System.out.println("받은 뉴스 postId 파라미터: " + idParam);
                }

                // 파라미터 디버그를 위한 모든 요청 파라미터 출력
                System.out.println("요청 URL: " + req.getRequestURL().toString() + "?" + req.getQueryString());
                System.out.println("모든 파라미터 목록:");
                Enumeration<String> paramNames = req.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    System.out.println(paramName + ": " + req.getParameter(paramName));
                }

                if (idParam == null || idParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "뉴스 ID가 제공되지 않았습니다.");
                    return errorResult;
                }

                // 문자열에서 공백 제거 후 변환 시도
                idParam = idParam.trim();
                System.out.println("변환 전 뉴스 ID 문자열: [" + idParam + "]");

                try {
                    long newsId = Long.parseLong(idParam);
                    System.out.println("변환된 뉴스 ID: " + newsId);

                    // 여기를 수정: 세션 기반 조회수 증가가 적용된 메서드 호출
                    NewsDTO news = newsService.getNewsById(newsId, req);

                    if (news == null) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("status", "error");
                        errorResult.put("message", "뉴스를 찾을 수 없습니다.");
                        return errorResult;
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("news", news);

                    // 댓글 목록도 함께 조회
                    List<NewsCommentDTO> comments = newsService.getNewsComments(newsId);
                    result.put("comments", comments);

                    return result;
                } catch (NumberFormatException e) {
                    // 예외 정보 로깅 추가
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "잘못된 뉴스 ID입니다. 오류: " + e.getMessage());
                    return errorResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "뉴스 조회 중 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });

        // 뉴스 댓글 목록 조회 API 추가
        router.getJson("/comments", (req, res) -> {
            try {
                // postId와 id 파라미터 모두 지원 (클라이언트 호환성을 위해)
                String idParam = req.getParameter("postId");
                System.out.println("받은 댓글 postId 파라미터: " + idParam);

                if (idParam == null || idParam.isEmpty()) {
                    idParam = req.getParameter("id");
                    System.out.println("받은 댓글 id 파라미터: " + idParam);
                }

                if (idParam == null || idParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "뉴스 ID가 제공되지 않았습니다.");
                    return errorResult;
                }

                // 문자열에서 공백 제거 후 변환 시도
                idParam = idParam.trim();
                long newsId = Long.parseLong(idParam);

                System.out.println("변환된 댓글 뉴스 ID: " + newsId);

                // 댓글 목록 조회
                List<NewsCommentDTO> comments = newsService.getNewsComments(newsId);
                System.out.println("조회된 댓글 수: " + (comments != null ? comments.size() : "없음"));

                if (comments == null) {
                    // 댓글이 없거나 오류 발생 시 빈 배열 반환
                    return new ArrayList<>();
                }

                return comments;
            } catch (NumberFormatException e) {
                // 예외 정보 로깅 추가
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "잘못된 뉴스 ID입니다. 오류: " + e.getMessage());
                return errorResult;
            }
        });

        router.getJson("/recommend", (req, res) -> {
            try {
                // 클라이언트에서 전송된 ID 파라미터 가져오기
                String idParam = req.getParameter("id");
                if (idParam == null) {
                    idParam = req.getParameter("postId");
                }

                // 현재 로그인한 사용자 정보 가져오기
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");

                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }

                // 로그 추가
                System.out.println("GET 방식 추천 요청: 뉴스ID=" + idParam + ", 사용자ID=" + user.getUserId());

                if (idParam == null || idParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "뉴스 ID가 제공되지 않았습니다.");
                    return errorResult;
                }

                try {
                    long newsId = Long.parseLong(idParam.trim());

                    // 현재 로그인한 사용자의 ID를 사용
                    boolean success = newsService.recommendNewsById(newsId, user.getUserId());

                    Map<String, Object> result = new HashMap<>();
                    if (success) {
                        // 업데이트된 게시글 정보를 가져와 추천수 포함
                        NewsDTO updatedNews = newsService.getNewsById(newsId);

                        result.put("status", "success");
                        result.put("success", true);  // board.js와 호환성 위해 추가
                        result.put("message", "추천이 완료되었습니다.");
                        result.put("likeCount", updatedNews.getNewsRecommend());
                    } else {
                        result.put("status", "error");
                        result.put("success", false);
                        result.put("message", "이미 추천한 게시글입니다.");
                    }
                    return result;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("success", false);
                    errorResult.put("message", "잘못된 뉴스 ID입니다.");
                    return errorResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("success", false);
                errorResult.put("message", "서버 오류가 발생했습니다.");
                return errorResult;
            }
        });

        // POST 요청 JSON 라우터 설정
        router.postJson("/create", (request, response) -> {
            // API로 게시글 작성 요청 처리
            HttpSession session = request.getSession();
            UserDTO user = (UserDTO) session.getAttribute("user");

            if (user == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "로그인이 필요합니다.");
                return errorResult;
            }

            // JSON 데이터 읽기
            String title = null;
            String content = null;
            String notifyValue = null;

            try {
                // JSON 요청 본문 파싱
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = request.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                // JSON이 비어있지 않은 경우 파싱
                if (sb.length() > 0) {
                    try {
                        Map<String, Object> jsonMap = gson.fromJson(sb.toString(), Map.class);
                        title = (String) jsonMap.get("newsTitle");
                        content = (String) jsonMap.get("newsContents");
                        notifyValue = (String) jsonMap.get("newsNotify");

                        // 로깅 - 디버깅용
                        System.out.println("JSON으로 뉴스 게시글 작성 요청 수신: " + title + ", " + content);
                    } catch (Exception e) {
                        System.out.println("JSON 파싱 오류: " + e.getMessage());
                    }
                } else {
                    // 폼 데이터에서 시도
                    title = request.getParameter("newsTitle");
                    content = request.getParameter("newsContents");
                    notifyValue = request.getParameter("newsNotify");

                    // 로깅 - 디버깅용
                    System.out.println("폼데이터로 뉴스 게시글 작성 요청 수신: " + title + ", " + content);
                }
            } catch (Exception e) {
                System.out.println("요청 본문 읽기 오류: " + e.getMessage());
                // 폼 데이터 가져오기
                title = request.getParameter("newsTitle");
                content = request.getParameter("newsContents");
                notifyValue = request.getParameter("newsNotify");
            }

            // 데이터 유효성 검사
            if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "제목과 내용을 모두 입력해야 합니다.");
                return errorResult;
            }

            // 새 게시글 DTO 생성
            NewsDTO news = new NewsDTO();
            news.setNewsTitle(title);
            news.setNewsContents(content);
            news.setUserId(user.getUserId());
            news.setNewsAuthorIp(IpUtil.getClientIpAddr(request));
            // 데이터베이스에 news_notify 컬럼이 없어서 설정할 수 없음
            // 기본값 설정만 유지
            news.setNewsNotify("common");

            // 저장
            boolean success = newsService.postNews(news, user.getUserLevel() >= 3 ? "admin" : "user");

            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("message", "게시글이 성공적으로 등록되었습니다.");
                result.put("newsId", news.getNewsId());
                return result;
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "게시글 등록에 실패했습니다.");
                return errorResult;
            }
        });

        router.postJson("/recommend", (req, res) -> {
            try {
                // 클라이언트에서 전송된 ID 파라미터 가져오기
                String idParam = req.getParameter("id");
                if (idParam == null) {
                    idParam = req.getParameter("postId");
                }

                // 현재 로그인한 사용자 정보 가져오기 (추천을 누른 사용자)
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");

                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }

                // 사용자 ID를 문자열로 추출
                String userId = String.valueOf(user.getUserId());

                String type = req.getParameter("type");
                System.out.println("추천 요청: 뉴스ID=" + idParam + ", 타입=" + type + ", 사용자ID=" + userId);

                if (idParam == null || idParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "뉴스 ID가 제공되지 않았습니다.");
                    return errorResult;
                }

                try {
                    long newsId = Long.parseLong(idParam.trim());

                    // 현재 로그인한 사용자의 ID를 두 번째 인자로 전달 ("like" 대신)
                    boolean success = newsService.recommendNewsById(newsId, user.getUserId());

                    Map<String, Object> result = new HashMap<>();
                    if (success) {
                        result.put("status", "success");
                        result.put("message", "추천이 완료되었습니다.");

                        // 업데이트된 추천 수 반환
                        NewsDTO updatedNews = newsService.getNewsById(newsId);
                        result.put("likeCount", updatedNews.getNewsRecommend());
                    } else {
                        result.put("status", "error");
                        result.put("message", "추천 처리 중 오류가 발생했습니다.");
                    }
                    return result;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "잘못된 뉴스 ID입니다.");
                    return errorResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "서버 오류가 발생했습니다.");
                return errorResult;
            }
        });

        router.postJson("/comment", (req, res) -> {
            try {
                // 로그인 확인
                HttpSession session = req.getSession();
                UserDTO user = (UserDTO) session.getAttribute("user");

                if (user == null) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "로그인이 필요합니다.");
                    return errorResult;
                }

                // 파라미터 가져오기
                String postIdParam = req.getParameter("postId");
                String content = req.getParameter("content");
                String parentIdParam = req.getParameter("parentId");

                System.out.println("댓글 작성 요청: postId=" + postIdParam + ", 내용=" + content);

                if (postIdParam == null || postIdParam.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "게시글 ID가 제공되지 않았습니다.");
                    return errorResult;
                }

                if (content == null || content.trim().isEmpty()) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "댓글 내용을 입력해주세요.");
                    return errorResult;
                }

                try {
                    long newsId = Long.parseLong(postIdParam.trim());

                    // 게시글 존재 여부 확인
                    NewsDTO news = newsService.getNewsById(newsId);
                    if (news == null) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("status", "error");
                        errorResult.put("message", "뉴스를 찾을 수 없습니다.");
                        return errorResult;
                    }

                    // 댓글 객체 생성
                    NewsCommentDTO comment = new NewsCommentDTO();
                    comment.setNewsId(newsId);
                    comment.setNewsCommentContents(content);
                    comment.setUserId(user.getUserId());
                    comment.setNewsCommentAuthorIp(IpUtil.getClientIpAddr(req));

                    // 부모 댓글 ID 처리 (답글인 경우)
                    if (parentIdParam != null && !parentIdParam.trim().isEmpty()) {
                        try {
                            long parentId = Long.parseLong(parentIdParam.trim());
                            comment.setParentId(parentId);
                        } catch (NumberFormatException e) {
                            // 부모 ID가 유효하지 않으면 무시
                        }
                    }

                    // 댓글 저장
                    boolean success = newsService.addNewsComment(comment);

                    Map<String, Object> result = new HashMap<>();
                    if (success) {
                        result.put("status", "success");
                        result.put("message", "댓글이 등록되었습니다.");

                        // 댓글 목록 다시 조회해서 반환
                        List<NewsCommentDTO> comments = newsService.getNewsComments(newsId);
                        result.put("comments", comments);
                    } else {
                        result.put("status", "error");
                        result.put("message", "댓글 등록에 실패했습니다.");
                    }
                    return result;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("status", "error");
                    errorResult.put("message", "잘못된 게시글 ID입니다.");
                    return errorResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
                return errorResult;
            }
        });
    }

    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 직접 JSON으로 변환하지 않고 직렬화된 문자열 반환
            String jsonString = convertToJsonWithoutLocalDateTime(data);
            out.print(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 시 기본 오류 응답
            out.print("{\"error\": \"JSON 직렬화 중 오류가 발생했습니다.\"}");
        }

        out.flush();
    }

    /**
     * LocalDateTime 객체를 문자열로 변환
     */
    private String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "null";
        }
        return "\"" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime) + "\"";
    }

    /**
     * 객체의 필드 값을 직접 JSON으로 변환하며 LocalDateTime 처리
     */
    private String convertToJsonWithoutLocalDateTime(Object data) {
        if (data == null) {
            return "null";
        }

        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            StringBuilder sb = new StringBuilder();
            sb.append("{");

            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;

                sb.append("\"").append(entry.getKey()).append("\":");
                Object value = entry.getValue();

                if (value instanceof LocalDateTime) {
                    // LocalDateTime 직접 처리
                    sb.append(formatLocalDateTime((LocalDateTime) value));
                } else if (value instanceof String) {
                    sb.append("\"").append(escapeJsonString((String) value)).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    sb.append(value);
                } else if (value instanceof List) {
                    sb.append(convertListToJson((List<?>) value));
                } else if (value instanceof Map) {
                    sb.append(convertToJsonWithoutLocalDateTime(value));
                } else if (value == null) {
                    sb.append("null");
                } else {
                    // 다른 객체의 경우 gson 사용
                    sb.append(gson.toJson(value));
                }
            }

            sb.append("}");
            return sb.toString();
        } else if (data instanceof List) {
            return convertListToJson((List<?>) data);
        } else if (data instanceof LocalDateTime) {
            // LocalDateTime 직접 처리
            return formatLocalDateTime((LocalDateTime) data);
        } else {
            // 기본 타입이나 다른 객체는 Gson 사용
            return gson.toJson(data);
        }
    }

    /**
     * List를 JSON 배열로 변환
     */
    private String convertListToJson(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            if (item instanceof LocalDateTime) {
                // LocalDateTime은 ISO 형식 문자열로 직접 변환
                LocalDateTime dateTime = (LocalDateTime) item;
                sb.append("\"").append(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\"");
            } else if (item instanceof String) {
                sb.append("\"").append(escapeJsonString((String) item)).append("\"");
            } else if (item instanceof Number || item instanceof Boolean) {
                sb.append(item);
            } else if (item instanceof List) {
                sb.append(convertListToJson((List<?>) item));
            } else if (item instanceof Map) {
                sb.append(convertToJsonWithoutLocalDateTime(item));
            } else if (item == null) {
                sb.append("null");
            } else {
                // Custom DTO 처리
                sb.append(convertToJsonWithoutLocalDateTime(convertDtoToMap(item)));
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * DTO 객체를 Map으로 변환
     */
    private Map<String, Object> convertDtoToMap(Object dto) {
        Map<String, Object> map = new HashMap<>();

        try {
            // NewsDTO 특별 처리
            if (dto instanceof NewsDTO) {
                NewsDTO news = (NewsDTO) dto;
                map.put("newsId", news.getNewsId());
                map.put("newsTitle", news.getNewsTitle());
                map.put("newsContents", news.getNewsContents());
                map.put("newsRead", news.getNewsRead());
                map.put("newsRecommend", news.getNewsRecommend());

                // LocalDateTime 직접 문자열로 변환
                if (news.getNewsWritetime() != null) {
                    map.put("newsWritetime", news.getNewsWritetime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (news.getNewsModifyTime() != null) {
                    map.put("newsModifyTime", news.getNewsModifyTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                map.put("newsAuthorIp", news.getNewsAuthorIp());
                map.put("newsNotify", news.getNewsNotify());
                map.put("newsDeleted", news.getNewsDeleted());
                map.put("userId", news.getUserId());
                return map;
            }

            // NewsCommentDTO 특별 처리
            else if (dto instanceof NewsCommentDTO) {
                NewsCommentDTO comment = (NewsCommentDTO) dto;
                map.put("newsCommentUid", comment.getNewsCommentId());
                map.put("newsCommentContents", comment.getNewsCommentContents());

                // LocalDateTime 직접 문자열로 변환
                if (comment.getNewsCommentWritetime() != null) {
                    map.put("newsCommentWritetime", comment.getNewsCommentWritetime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (comment.getNewsCommentModifytime() != null) {
                    map.put("newsCommentModifytime", comment.getNewsCommentModifytime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                map.put("newsCommentAuthorIp", comment.getNewsCommentAuthorIp());
                map.put("newsUid", comment.getNewsId());
                map.put("userId", comment.getUserId());

                // 사용자 이름 추가 (JOIN 결과에서 가져온 경우)
                if (comment.getUserName() != null) {
                    map.put("userName", comment.getUserName());
                }

                return map;
            }

            // 기타 DTO는 gson으로 변환 후 다시 파싱 (위험한 방법이지만 임시 해결책)
            String json = gson.toJson(dto);
            return gson.fromJson(json, Map.class);

        } catch (Exception e) {
            e.printStackTrace();
            map.put("error", "객체 변환 오류");
            return map;
        }
    }

    /**
     * JSON 문자열 이스케이프 처리
     */
    private String escapeJsonString(String str) {
        if (str == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < ' ') {
                        String hex = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            sb.append('0');
                        }
                        sb.append(hex);
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // API 요청인지 먼저 확인 (pathInfo 있는 요청은 API 요청으로 간주)
        String pathInfo = request.getPathInfo();

        // pathInfo가 있으면 API 요청으로 간주하고 Router를 통해 처리 시도
        if (pathInfo != null) {
            boolean handled = router.handleGetJson(request, response);
            if (handled) {
                // Router가 요청을 처리했으므로 메서드 종료
                return;
            }
        }

        // .do 요청일 경우 JSON 응답으로 처리
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith(".do")) {
            // JSON 응답 처리
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "뉴스 게시판 API");
            sendJsonResponse(response, result);
            return;
        }

        String action = request.getParameter("action");

        if (action == null) {
            // 기본값은 목록 보기
            action = "list";
        }

        try {
            switch (action) {
                case "list":
                    listNews(request, response);
                    break;
                case "view":
                    viewNews(request, response);
                    break;
                case "write":
                    writeForm(request, response);
                    break;
                case "edit":
                    editForm(request, response);
                    break;
                case "toggle-notice":
                    toggleNotice(request, response);
                    break;
                default:
                    listNews(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // API 요청인지 먼저 확인 (pathInfo 있는 요청은 API 요청으로 간주)
        String pathInfo = request.getPathInfo();

        // pathInfo가 있으면 API 요청으로 간주하고 Router를 통해 처리 시도
        if (pathInfo != null) {
            boolean handled = router.handlePostJson(request, response);
            if (handled) {
                // Router가 요청을 처리했으므로 메서드 종료
                return;
            }
        }

        // 일반 form submit 요청 처리
        String action = request.getParameter("action");

        if (action == null) {
            // 기본값은 목록 보기로 리다이렉트
            response.sendRedirect("news.do");
            return;
        }

        try {
            switch (action) {
                case "write":
                    createNews(request, response);
                    break;
                case "edit":
                    updateNews(request, response);
                    break;
                case "delete":
                    deleteNews(request, response);
                    break;
                case "recommend":
                    recommendNews(request, response);
                    break;
                case "toggleNotice":
                    toggleNotice(request, response);
                    break;
                default:
                    response.sendRedirect("news.do");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 뉴스 목록을 조회하여 목록 페이지로 포워딩
     */
    private void listNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 페이지네이션을 위한 파라미터 처리
        int page = 1;
        int pageSize = 10;

        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 숫자가 아닌 값이 들어온 경우 기본값 사용
            }
        }

        // 공지사항 및 일반 게시글 목록 가져오기
        List<NewsDTO> notificationList = newsService.getAllNews(1, 100); // 공지사항 목록 가져오기 - 최대 100개
        List<NewsDTO> newsList = newsService.getAllNews(page, pageSize);
        int totalCount = newsService.getTotalNewsCount();
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);

        // 요청 속성에 설정
        request.setAttribute("notificationList", notificationList);
        request.setAttribute("newsList", newsList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        // HTML 페이지로 포워딩 대신 JSON 응답 반환
        Map<String, Object> result = new HashMap<>();
        result.put("newsList", newsList);
        result.put("notificationList", notificationList);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalCount", totalCount);

        sendJsonResponse(response, result);
    }

    /**
     * 특정 뉴스 게시글 조회
     */
    private void viewNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String newsIdStr = request.getParameter("id");

        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }

        try {
            long newsId = Long.parseLong(newsIdStr);

            // 게시글 정보 가져오기
            NewsDTO news = newsService.getNewsById(newsId);

            if (news == null || "deleted".equals(news.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }

            // increaseReadCount 파라미터 확인 (기본값은 true)
            String increaseReadCountParam = request.getParameter("increaseReadCount");
            boolean increaseReadCount = !"false".equalsIgnoreCase(increaseReadCountParam);

            System.out.println("조회수 증가 요청 - newsId: " + newsId + ", increaseReadCount: " + increaseReadCount);

            // 세션 기반 조회수 증가 처리 (파라미터가 true일 때만 실행)
            if (increaseReadCount) {
                try {
                    // NewsDAO의 세션 기반 조회수 증가 메서드를 직접 호출
                    new NewsDAO().updateReadCount(newsId, request);
                    System.out.println("조회수 증가 처리 완료: newsId=" + newsId);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("조회수 증가 중 오류 발생: " + e.getMessage());
                }
            } else {
                System.out.println("조회수 증가 생략 (increaseReadCount=false): newsId=" + newsId);
            }

            // 요청 속성에 설정
            request.setAttribute("news", news);

            // HTML 페이지로 포워딩 대신 JSON 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("news", news);

            // 댓글 목록도 함께 조회
            List<NewsCommentDTO> comments = newsService.getNewsComments(newsId);
            result.put("comments", comments);

            sendJsonResponse(response, result);

        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }

    /**
     * 글 작성 폼을 표시하는 대신 사용자 권한 정보만 JSON으로 반환
     */
    private void writeForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            // 로그인되지 않은 경우 권한 없음 응답
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("redirect", request.getContextPath() + "/login.do");
        } else {
            // 로그인된 경우 사용자 정보 및 권한 포함
            result.put("success", true);
            result.put("userId", user.getUserId());
            result.put("username", user.getUsername());
            result.put("userLevel", user.getUserLevel());
            result.put("canWrite", true);
        }

        sendJsonResponse(response, result);
    }

    /**
     * 글 수정 폼을 표시
     */
    private void editForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            // 로그인되지 않은 경우
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }

        String newsIdStr = request.getParameter("id");

        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }

        try {
            long newsId = Long.parseLong(newsIdStr);
            NewsDTO news = newsService.getNewsById(newsId);

            if (news == null || "deleted".equals(news.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }

            // 작성자 또는 관리자만 수정 가능
            if (news.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                return;
            }

            // 요청 속성에 설정
            request.setAttribute("news", news);

            // 수정 폼으로 포워딩
            request.getRequestDispatcher("/view/pages/news/edit.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }

    /**
     * 새 뉴스 게시글 생성
     */
    private void createNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }

        // 폼 데이터 가져오기
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String notifyValue = request.getParameter("notify");

        // 데이터 유효성 검사
        if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
            request.setAttribute("error", "제목과 내용을 모두 입력해야 합니다.");
            request.getRequestDispatcher("/view/pages/news/write.jsp").forward(request, response);
            return;
        }

        // 새 게시글 DTO 생성
        NewsDTO news = new NewsDTO();
        news.setNewsTitle(title);
        news.setNewsContents(content);
        news.setUserId(user.getUserId());
        news.setNewsAuthorIp(IpUtil.getClientIpAddr(request));

        // 데이터베이스에 news_notify 컬럼이 없어서 설정할 수 없음
        // 기본값만 유지
        news.setNewsNotify("common");

        // 저장
        // NewsService의 postNews 메서드 사용
        long newsId = newsService.postNews(news, user.getUserLevel() >= 3 ? "admin" : "user") ? news.getNewsId() : -1;

        // 상세 페이지로 리다이렉트
        response.sendRedirect("news.do?action=view&id=" + newsId);
    }

    /**
     * 뉴스 게시글 수정
     */
    private void updateNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }

        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }

        try {
            long newsId = Long.parseLong(newsIdStr);

            // 원본 게시글 가져오기
            NewsDTO original = newsService.getNewsById(newsId);

            if (original == null || "deleted".equals(original.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }

            // 작성자 또는 관리자만 수정 가능
            if (original.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
                return;
            }

            // 폼 데이터 가져오기
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String notifyValue = request.getParameter("notify");

            // 데이터 유효성 검사
            if (title == null || content == null || title.trim().isEmpty() || content.trim().isEmpty()) {
                request.setAttribute("error", "제목과 내용을 모두 입력해야 합니다.");
                request.setAttribute("news", original);
                request.getRequestDispatcher("/view/pages/news/edit.jsp").forward(request, response);
                return;
            }

            // 게시글 수정
            original.setNewsTitle(title);
            original.setNewsContents(content);

            // 데이터베이스에 news_notify 컬럼이 없어서 설정할 수 없음
            // 기본값만 유지
            original.setNewsNotify("common");

            // 저장
            newsService.updateNewsById(original, user.getUserLevel() >= 3 ? "admin" : "user");

            // 상세 페이지로 리다이렉트
            response.sendRedirect("news.do?action=view&id=" + newsId);

        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }

    /**
     * 뉴스 게시글 삭제
     */
    private void deleteNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.do");
            return;
        }

        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.sendRedirect("news.do");
            return;
        }

        try {
            long newsId = Long.parseLong(newsIdStr);

            // 원본 게시글 가져오기
            NewsDTO original = newsService.getNewsById(newsId);

            if (original == null || "deleted".equals(original.getNewsDeleted())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
                return;
            }

            // 작성자 또는 관리자만 삭제 가능
            if (original.getUserId() != user.getUserId() && user.getUserLevel() < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "삭제 권한이 없습니다.");
                return;
            }

            // 게시글 삭제
            newsService.deleteNewsById(newsId, user.getUserId(), user.getUserLevel() >= 3 ? "admin" : "user");

            // 목록 페이지로 리다이렉트
            response.sendRedirect("news.do");

        } catch (NumberFormatException e) {
            response.sendRedirect("news.do");
        }
    }

    /**
     * 게시글 추천
     */
    private void recommendNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }

        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"게시글 ID가 필요합니다.\"}");
            return;
        }

        try {
            long newsId = Long.parseLong(newsIdStr);

            // 게시글 존재 여부 확인
            NewsDTO news = newsService.getNewsById(newsId);

            if (news == null || "deleted".equals(news.getNewsDeleted())) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"존재하지 않는 게시글입니다.\"}");
                return;
            }

            // 본인 게시글 추천 방지
            if (news.getUserId() == user.getUserId()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"자신의 게시글은 추천할 수 없습니다.\"}");
                return;
            }

            // 이미 추천했는지 확인
            boolean alreadyRecommended = isAlreadyRecommended(newsId, user.getUserId());

            if (alreadyRecommended) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"이미 추천한 게시글입니다.\"}");
                return;
            }

            // 추천 처리
            newsService.recommendNewsById(newsId, user.getUserId());

            // 업데이트된 추천 수 가져오기
            NewsDTO updatedNews = newsService.getNewsById(newsId);
            int recommendCount = updatedNews != null ? updatedNews.getNewsRecommend() : 0;

            response.setContentType("application/json");
            response.getWriter().write("{\"success\": true, \"message\": \"추천이 완료되었습니다.\", \"count\": " + recommendCount + "}");

        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 게시글 ID입니다.\"}");
        }
    }

    /**
     * 공지사항 설정/해제 토글
     */
    private void toggleNotice(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 로그인 확인
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
            return;
        }

        // 관리자 권한 확인
        if (user.getUserLevel() < 3) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"관리자만 공지사항 상태를 변경할 수 있습니다.\"}");
            return;
        }

        String newsIdStr = request.getParameter("id");
        if (newsIdStr == null || newsIdStr.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"게시글 ID가 필요합니다.\"}");
            return;
        }

        try {
            long newsId = Long.parseLong(newsIdStr);

            // 원본 게시글 가져오기
            NewsDTO original = newsService.getNewsById(newsId);

            if (original == null || "deleted".equals(original.getNewsDeleted())) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"존재하지 않는 게시글입니다.\"}");
                return;
            }

            // 공지사항 상태 토글
            // 데이터베이스에 news_notify 컬럼이 없어 구현이 제한됨
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"현재 공지사항 기능은 지원되지 않습니다. 관리자에게 문의하세요.\"}");
            return;

        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\": false, \"message\": \"잘못된 게시글 ID입니다.\"}");
        }
    }

    /**
     * 사용자가 이미 게시글을 추천했는지 확인
     */
    private boolean isAlreadyRecommended(long newsId, long userId) throws SQLException {
        try {
            // NewsService의 hasUserRecommended 메서드를 사용하여 추천 여부를 확인합니다.
            return newsService.hasUserRecommended(newsId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

