package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.ui.impl.glass.screens.Message;
import com.husker.launcher.ui.impl.glass.screens.SimpleLoadingScreen;

public class R_2_NameCheck extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка доступности имени...");
    }

    public void onShow() {
        new Thread(() -> {
            setText("Проверка доступности имени...");

            String login = getParameterValue("login");
            String password = getParameterValue("password");

            int register_result = getLauncher().NetManager.Players.register(login, password);

            if(register_result == getLauncher().NetManager.Players.ERROR)
                Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка", "registration", getParameters());
            if(register_result == getLauncher().NetManager.Players.NAME_TAKEN)
                Message.showMessage(getLauncherUI(), "Проблемка", "Такое имя уже занято...", "registration", getParameters());
            if(register_result == getLauncher().NetManager.Players.BAD_PASSWORD)
                Message.showMessage(getLauncherUI(), "Проблемка", "Пароль имеет неверный формат!", "registration", getParameters());
            if(register_result == getLauncher().NetManager.Players.SUCCESSFUL_REGISTRATION)
                getLauncherUI().setScreen("registration_1", getParameters());

        }).start();
    }
}
