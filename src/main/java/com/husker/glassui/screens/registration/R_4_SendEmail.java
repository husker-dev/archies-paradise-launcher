package com.husker.glassui.screens.registration;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;

public class R_4_SendEmail extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Отправка проверочного кода...");
    }

    public void process() {

        String login = getParameterValue("login");
        String password = getParameterValue("password");
        String email = getParameterValue("email");
        boolean encrypted = getParameterValue("encrypted", "false").equals("true");

        int result = getLauncher().NetManager.Email.sendConfirmCode(login, password, email, encrypted);
        if(result == getLauncher().NetManager.Email.OK)
            getLauncherUI().setScreen("emailConfirm", getParameters());
        if(result == getLauncher().NetManager.Email.ERROR)
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", "registration_1", getParameters());
    }
}
