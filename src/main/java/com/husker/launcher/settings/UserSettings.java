package com.husker.launcher.settings;

import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.settings.SettingsFile;

import java.io.File;

public class UserSettings extends SettingsFile {

    public UserSettings() {
        super(new File("user.cfg"));
    }

    public String getLogin(){
        return get("login");
    }

    public void setLogin(String login){
        set("login", login);
    }

    public String getPassword(){
        return get("password");
    }

    public void setPassword(String password){
        set("password", password);
    }

    public void reset(){
        setLogin("null");
        setPassword("null");
    }

    public boolean hasAccount(){
        String login = getLogin();
        String password = getPassword();

        return login != null && password != null && !login.equals("null") && !password.equals("null");
    }
}
