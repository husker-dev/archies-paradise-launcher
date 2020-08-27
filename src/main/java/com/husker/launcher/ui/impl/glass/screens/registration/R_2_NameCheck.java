package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class R_2_NameCheck extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка доступности имени...");
    }

    public void onShow() {
        new Thread(() -> {
            setText("Проверка доступности имени...");

            int result = getLauncher().NetManager.Players.checkNickname(getParameters()[0]);

            if(result == getLauncher().NetManager.Players.ERROR)
                getLauncherUI().setScreen("message", "registration", "Проблемка", "Произошла ошибка");
            if(result == getLauncher().NetManager.Players.NAME_NOT_TAKEN) {
                setText("Создание аккаунта...");

                int register_result = getLauncher().NetManager.Players.register(getParameters()[0], getParameters()[1]);

                if(register_result == getLauncher().NetManager.Players.ERROR)
                    getLauncherUI().setScreen("message", "registration", "Проблемка", "Произошла ошибка");
                if(register_result == getLauncher().NetManager.Players.NAME_TAKEN)
                    getLauncherUI().setScreen("message", "registration", "Проблемка", "Такое имя уже занято...");
                if(register_result == getLauncher().NetManager.Players.BAD_PASSWORD)
                    getLauncherUI().setScreen("message", "registration", "Проблемка", "Пароль имеет неверный формат!");
                if(register_result == getLauncher().NetManager.Players.SUCCESSFUL_REGISTRATION)
                    getLauncherUI().setScreen("registration_1", getParameters());
            }
            if(result == getLauncher().NetManager.Players.NAME_TAKEN)
                getLauncherUI().setScreen("message", "registration", "Проблемка", "Такое имя уже занято...");
        }).start();
    }
}
