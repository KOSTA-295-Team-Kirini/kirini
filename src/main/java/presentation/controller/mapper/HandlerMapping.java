package presentation.controller.mapper;

import java.util.HashMap;
import java.util.Map;

import presentation.controller.page.Controller;
import presentation.controller.page.user.UserLoginController;
import presentation.controller.page.user.UserRegisterController;
import presentation.controller.page.user.UserProfileController;
import presentation.controller.page.guide.GuideController;
import presentation.controller.page.admin.AdminPageController;
import presentation.controller.page.board.NewsController;
import presentation.controller.page.board.FreeboardController;
import presentation.controller.page.board.ChatboardController;
import presentation.controller.page.question.QuestionController;

public class HandlerMapping {
    private static HandlerMapping instance;
    private Map<String, Controller> controllerMap;
    
    private HandlerMapping() {
        controllerMap = new HashMap<>();
        initializeControllers();
    }
    
    public static synchronized HandlerMapping getInstance() {
        if (instance == null) {
            instance = new HandlerMapping();
        }
        return instance;
    }
    
    private void initializeControllers() {
        // 기존 URL과 동일한 컨트롤러 매핑
        controllerMap.put("login", new UserLoginController());
        controllerMap.put("signup", new UserRegisterController());
          // 관리자 컨트롤러 추가
        controllerMap.put("admin", new AdminPageController());
        controllerMap.put("admin/users", new AdminPageController());
        controllerMap.put("admin/reports", new AdminPageController());
        controllerMap.put("admin/posts", new AdminPageController());
        controllerMap.put("admin/guides", new AdminPageController());
        controllerMap.put("admin/keyboards", new AdminPageController());
        
        // 뉴스 컨트롤러 추가
        controllerMap.put("news", new NewsController());
        controllerMap.put("news/view", new NewsController());
        controllerMap.put("news/write", new NewsController());
        controllerMap.put("news/edit", new NewsController());
        
        // 새로운 기능은 여기에 추가
    }
    
    public Controller getController(String command) {
        return controllerMap.get(command);
    }
}