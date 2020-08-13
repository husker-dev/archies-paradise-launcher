package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.NetManager;
import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class R_2_NameCheck extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка доступности имени...");
    }

    public void onShow() {
        new Thread(() -> {
            if(NetManager.checkNickname(getParameters()[0])){
                getLauncherUI().setScreen("registration_1", getParameters());
            }else{
                getLauncherUI().setScreen("message", "registration", "Проблемка", "Такое имя уже занято...");
            }
        }).start();
    }
}
