package presentation.controller.mapper;

import java.util.HashMap;
import java.util.Map;

import presentation.controller.page.Controller;
import presentation.controller.page.user.UserLoginController;
import presentation.controller.page.user.UserRegisterController;
import presentation.controller.page.user.UserProfileController;
import presentation.controller.page.guide.GuideController;
import presentation.controller.page.admin.AdminPageController;

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
        
        // 새로운 기능은 여기에 추가
    }
    
    public Controller getController(String command) {
        return controllerMap.get(command);
    }
}