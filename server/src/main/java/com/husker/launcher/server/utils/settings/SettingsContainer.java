package com.husker.launcher.server.utils.settings;

public interface SettingsContainer {
    SettingsContainer getParentSettingsContainer();
    String getTitle();
    String get(String parameter, String defaultValue);
    String get(String parameter);
}
