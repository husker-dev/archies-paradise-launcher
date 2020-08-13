package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.NetManager;
import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class R_6_Final extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка кода...");
    }

    public void onShow() {
        super.onShow();

        new Thread(() -> {
            if(NetManager.confirmEmail(getParameters()[0], getParameters()[1], getParameters()[2], getParameters()[3])){
                getLauncherUI().setScreen("message", "login", "Регистрация", "Аккаунт создан");
            }else{
                getLauncherUI().setScreen("message", "emailConfirm", "Ошибка", "Код не подходит");
            }
        }).start();
    }
}
