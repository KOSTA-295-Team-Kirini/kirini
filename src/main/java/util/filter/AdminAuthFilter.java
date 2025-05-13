package util.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/admin/*")
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        boolean isAdmin = false;
        
        if (isLoggedIn) {
            // 사용자 객체에서 관리자 권한 확인
            // dto.user.UserDTO user = (dto.user.UserDTO) session.getAttribute("user");
            // isAdmin = "ADMIN".equals(user.getRole());
            
            // 임시 구현: 세션에서 직접 role 확인
            String userRole = (String) session.getAttribute("userRole");
            isAdmin = "ADMIN".equals(userRole);
        }
        
        if (isAdmin) {
            // 관리자 인증 성공 - 요청 계속 진행
            chain.doFilter(request, response);
        } else {
            // 관리자 인증 실패 - index 페이지로 리다이렉트
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/view/pages/index.html");
        }
    }
}