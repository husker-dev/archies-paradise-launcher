package com.husker.launcher.server.utils.settings;


public class SettingsSubElement implements SettingsContainer{

    private final SettingsContainer parent;
    private final String title;

    public SettingsSubElement(SettingsContainer parent, String title){
        this.parent = parent;
        this.title = title;
    }

    public String get(String parameter){
        return get(parameter, null);
    }

    public String get(String parameter, String defaultValue){
        return getRoot().get(getPath() + "." + parameter, defaultValue);
    }

    private String getPath(){
        StringBuilder path = new StringBuilder(title);
        SettingsContainer current = this;
        while(true) {
            current = current.getParentSettingsContainer();
            if(current.getParentSettingsContainer() == null)
                break;
            path.insert(0, ".");
            path.insert(0, current.getTitle());
        }
        return path.toString();
    }

    public SettingsContainer getRoot(){
        SettingsContainer current = this;
        while(current.getParentSettingsContainer() != null)
            current = current.getParentSettingsContainer();
        return current;
    }

    public SettingsContainer getParentSettingsContainer() {
        return parent;
    }

    public String getTitle() {
        return title;
    }
}
