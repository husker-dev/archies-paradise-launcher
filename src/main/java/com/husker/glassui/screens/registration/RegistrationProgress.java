package com.husker.glassui.screens.registration;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.login.LoginProcess;

public class RegistrationProgress extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка доступности имени...");
    }

    public void process() {
        String login = getParameterValue("login");
        String password = getParameterValue("password");

        int register_result = getLauncher().API.Players.register(login, password);

        if(register_result == getLauncher().API.Players.ERROR)
            Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка", Registration.class, getParameters());
        if(register_result == getLauncher().API.Players.NAME_TAKEN)
            Message.showMessage(getLauncherUI(), "Проблемка", "Такое имя уже занято...", Registration.class, getParameters());
        if(register_result == getLauncher().API.Players.BAD_PASSWORD)
            Message.showMessage(getLauncherUI(), "Проблемка", "Пароль имеет неверный формат!", Registration.class, getParameters());
        if(register_result == getLauncher().API.Players.SUCCESSFUL_REGISTRATION)
            getLauncherUI().setScreen(LoginProcess.class, getParameters());

    }
}
