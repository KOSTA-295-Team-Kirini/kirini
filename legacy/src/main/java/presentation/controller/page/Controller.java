package presentation.controller.page;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import presentation.controller.dispatcher.ModelAndView;

/**
 * 모든 컨트롤러가 구현해야 하는 인터페이스
 * HandlerMapping을 통해 관리되는 모든 컨트롤러는 이 인터페이스를 구현해야 함
 */
public interface Controller {
    /**
     * HTTP GET 요청 처리
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @throws ServletException 서블릿 처리 중 발생한 예외
     * @throws IOException      입출력 처리 중 발생한 예외
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    /**
     * HTTP POST 요청 처리
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체     * @throws ServletException 서블릿 처리 중 발생한 예외
     * @throws IOException 입출력 처리 중 발생한 예외
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    // 향후 확장을 위한 메서드 (지금은 구현하지 않아도 됨)
    // ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) 
    //        throws Exception;
}