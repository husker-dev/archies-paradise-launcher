package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.NetManager;
import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class R_4_SendEmail extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Отправка проверочного кода...");
    }

    public void onShow() {
        new Thread(() -> {
            if(NetManager.sendEmailCode(getParameters()[2])){
                getLauncherUI().setScreen("emailConfirm", getParameters());
            }else{
                getLauncherUI().setScreen("message", "registration_1," + getParameters()[0] + "," + getParameters()[1], "Ошибка", "Ошибка отправки кода");
            }
        }).start();
    }
}
