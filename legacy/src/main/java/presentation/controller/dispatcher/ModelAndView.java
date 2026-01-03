package presentation.controller.dispatcher;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String viewName;
    private Map<String, Object> model;
    private boolean isRedirect;

    public ModelAndView() {
        this.model = new HashMap<>();
        this.isRedirect = false;
    }

    public ModelAndView(String viewName) {
        this();
        this.viewName = viewName;
    }

    public ModelAndView(String viewName, boolean isRedirect) {
        this(viewName);
        this.isRedirect = isRedirect;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void addAttribute(String name, Object value) {
        model.put(name, value);
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public void setRedirect(boolean isRedirect) {
        this.isRedirect = isRedirect;
    }
}
