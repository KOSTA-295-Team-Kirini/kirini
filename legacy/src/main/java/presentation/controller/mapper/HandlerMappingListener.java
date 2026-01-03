package presentation.controller.mapper;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class HandlerMappingListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 핸들러 매핑 초기화
        HandlerMapping.getInstance();
        System.out.println("핸들러 매핑이 초기화되었습니다.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 종료 시 필요한 정리 작업
    }
}
