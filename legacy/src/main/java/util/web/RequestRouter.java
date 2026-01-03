package util.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import util.json.LocalDateTimeAdapter;

/**
 * URL 경로에 따라 적절한 핸들러를 매핑하고 실행하는 유틸리티 클래스
 * JSON 응답을 위한 기능 제공
 */
public class RequestRouter {
    private Map<String, BiConsumer<HttpServletRequest, HttpServletResponse>> getHandlers = new HashMap<>();
    private Map<String, BiConsumer<HttpServletRequest, HttpServletResponse>> postHandlers = new HashMap<>();
    private Map<String, BiFunction<HttpServletRequest, HttpServletResponse, Object>> getJsonHandlers = new HashMap<>();
    private Map<String, BiFunction<HttpServletRequest, HttpServletResponse, Object>> postJsonHandlers = new HashMap<>();

    // GsonBuilder를 사용하여 LocalDateTime을 처리할 수 있는 Gson 인스턴스 생성
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new util.json.LocalDateTimeAdapter())
            .create();

    /**
     * GET 요청 핸들러 등록
     *
     * @param path    요청 경로
     * @param handler 요청 처리 핸들러
     * @return 현재 라우터 인스턴스 (체이닝 지원)
     */
    public RequestRouter get(String path, BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
        getHandlers.put(path, handler);
        return this;
    }

    /**
     * POST 요청 핸들러 등록
     *
     * @param path    요청 경로
     * @param handler 요청 처리 핸들러
     * @return 현재 라우터 인스턴스 (체이닝 지원)
     */
    public RequestRouter post(String path, BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
        postHandlers.put(path, handler);
        return this;
    }

    /**
     * 요청 경로에 해당하는 핸들러 찾기
     *
     * @param method HTTP 메소드
     * @param path   요청 경로
     * @return 해당 경로의 핸들러, 없으면 null
     */
    public BiConsumer<HttpServletRequest, HttpServletResponse> getHandler(String method, String path) {
        if ("GET".equalsIgnoreCase(method)) {
            return getHandlers.get(path);
        } else if ("POST".equalsIgnoreCase(method)) {
            return postHandlers.get(path);
        }
        return null;
    }

    /**
     * 라우터에 등록된 핸들러 수 반환
     *
     * @return 등록된 총 핸들러 수
     */
    public int getHandlerCount() {
        return getHandlers.size() + postHandlers.size();
    }

    /**
     * GET 요청 JSON 핸들러 등록
     *
     * @param path    요청 경로
     * @param handler 요청 처리 핸들러 (Object 반환, JSON으로 변환됨)
     * @return 현재 라우터 인스턴스 (체이닝 지원)
     */
    public RequestRouter getJson(String path, BiFunction<HttpServletRequest, HttpServletResponse, Object> handler) {
        getJsonHandlers.put(path, handler);
        return this;
    }

    /**
     * POST 요청 JSON 핸들러 등록
     *
     * @param path    요청 경로
     * @param handler 요청 처리 핸들러 (Object 반환, JSON으로 변환됨)
     * @return 현재 라우터 인스턴스 (체이닝 지원)
     */
    public RequestRouter postJson(String path, BiFunction<HttpServletRequest, HttpServletResponse, Object> handler) {
        postJsonHandlers.put(path, handler);
        return this;
    }

    /**
     * 요청 경로에 해당하는 JSON 핸들러 찾기
     *
     * @param method HTTP 메소드
     * @param path   요청 경로
     * @return 해당 경로의 JSON 핸들러, 없으면 null
     */
    public BiFunction<HttpServletRequest, HttpServletResponse, Object> getJsonHandler(String method, String path) {
        if ("GET".equalsIgnoreCase(method)) {
            return getJsonHandlers.get(path);
        } else if ("POST".equalsIgnoreCase(method)) {
            return postJsonHandlers.get(path);
        }
        return null;
    }

    /**
     * 요청을 처리합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @return 요청 처리 여부 (적절한 핸들러가 있어서 처리되었으면 true)
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    public boolean handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getPathInfo();

        if (path == null) {
            path = "/";
        }

        // JSON 핸들러 먼저 시도
        if ("GET".equalsIgnoreCase(method)) {
            if (handleGetJson(request, response)) {
                return true;
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            if (handlePostJson(request, response)) {
                return true;
            }
        }

        // 일반 핸들러 시도
        BiConsumer<HttpServletRequest, HttpServletResponse> handler = getHandler(method, path);
        if (handler != null) {
            handler.accept(request, response);
            return true;
        }

        return false;
    }

    /**
     * GET 요청의 JSON 핸들러를 실행하고 JSON 응답을 전송합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @return 요청 처리 여부 (적절한 핸들러가 있어서 처리되었으면 true)
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    public boolean handleGetJson(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            return false;
        }

        // 정확히 일치하는 핸들러 먼저 확인
        BiFunction<HttpServletRequest, HttpServletResponse, Object> handler = getJsonHandlers.get(pathInfo);

        // 정확히 일치하는 것이 없으면 정규식 패턴 확인
        if (handler == null) {
            for (Map.Entry<String, BiFunction<HttpServletRequest, HttpServletResponse, Object>> entry : getJsonHandlers.entrySet()) {
                String pattern = entry.getKey();
                if (pattern.contains("(") && pattern.contains(")")) {
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(pathInfo);
                    if (m.matches()) {
                        handler = entry.getValue();
                        break;
                    }
                }
            }
        }

        if (handler != null) {
            Object result = handler.apply(request, response);
            if (result != null) {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(result));
                out.flush();
                return true;
            }
        }

        return false;
    }

    /**
     * POST 요청의 JSON 핸들러를 실행하고 JSON 응답을 전송합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @return 요청 처리 여부 (적절한 핸들러가 있어서 처리되었으면 true)
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    public boolean handlePostJson(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            return false;
        }

        // 정확히 일치하는 핸들러 먼저 확인
        BiFunction<HttpServletRequest, HttpServletResponse, Object> handler = postJsonHandlers.get(pathInfo);

        // 정확히 일치하는 것이 없으면 정규식 패턴 확인
        if (handler == null) {
            for (Map.Entry<String, BiFunction<HttpServletRequest, HttpServletResponse, Object>> entry : postJsonHandlers.entrySet()) {
                String pattern = entry.getKey();
                if (pattern.contains("(") && pattern.contains(")")) {
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(pathInfo);
                    if (m.matches()) {
                        handler = entry.getValue();
                        break;
                    }
                }
            }
        }

        if (handler != null) {
            Object result = handler.apply(request, response);
            if (result != null) {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(result));
                out.flush();
                return true;
            }
        }

        return false;
    }
}
