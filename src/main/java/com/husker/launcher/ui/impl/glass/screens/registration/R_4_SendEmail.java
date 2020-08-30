package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.ui.impl.glass.screens.Message;
import com.husker.launcher.ui.impl.glass.screens.SimpleLoadingScreen;

public class R_4_SendEmail extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Отправка проверочного кода...");
    }

    public void onShow() {
        new Thread(() -> {

            String login = getParameterValue("login");
            String password = getParameterValue("password");
            String email = getParameterValue("email");
            boolean encrypted = getParameterValue("encrypted", "false").equals("true");

            int result = getLauncher().NetManager.Email.sendConfirmCode(login, password, email, encrypted);
            if(result == getLauncher().NetManager.Email.OK)
                getLauncherUI().setScreen("emailConfirm", getParameters());
            if(result == getLauncher().NetManager.Email.ERROR)
                Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", "registration_1", getParameters());
        }).start();
    }
}
