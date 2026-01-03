package presentation.controller.mapper;

import java.util.HashMap;
import java.util.Map;

import presentation.controller.admin.AdminPageController;
import presentation.controller.page.Controller;
import presentation.controller.page.board.ChatboardController;
import presentation.controller.page.board.FreeboardController;
import presentation.controller.page.board.NewsController;
import presentation.controller.page.database.KeyboardInfoController;
import presentation.controller.page.guide.GuideController;
import presentation.controller.page.question.QuestionController;
import presentation.controller.page.user.UserLoginController;
import presentation.controller.page.user.UserProfileController;
import presentation.controller.page.user.UserRegisterController;

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
        // 사용자 관련 컨트롤러
        controllerMap.put("login", new UserLoginController());
        controllerMap.put("signup", new UserRegisterController());
        controllerMap.put("profile", new UserProfileController());

        // 게시판 관련 컨트롤러
        controllerMap.put("freeboard", new FreeboardController());
        controllerMap.put("chatboard", new ChatboardController());
        controllerMap.put("news", new NewsController());
        controllerMap.put("question", new QuestionController());

        // 키보드 정보 컨트롤러
        controllerMap.put("keyboard", new KeyboardInfoController());

        // 가이드 컨트롤러
        controllerMap.put("guide", new GuideController());

        // 관리자 컨트롤러 추가
        controllerMap.put("admin", new AdminPageController());
        controllerMap.put("admin/users", new AdminPageController());
        controllerMap.put("admin/reports", new AdminPageController());
        controllerMap.put("admin/posts", new AdminPageController());
        controllerMap.put("admin/guides", new AdminPageController());
        controllerMap.put("admin/keyboards", new AdminPageController());
    }

    public Controller getController(String command) {
        return controllerMap.get(command);
    }
}