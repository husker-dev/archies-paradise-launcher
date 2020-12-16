package com.husker.glassui.screens.registration;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.login.LoginProcess;
import com.husker.launcher.api.API;

public class RegistrationProgress extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка доступности имени...");
    }

    public void process() {
        String login = getParameterValue("login");
        String password = getParameterValue("password");

        try {
            API.Auth.register(login, password);
            getLauncherUI().setScreen(LoginProcess.class, getParameters());
        } catch (API.InternalAPIException e) {
            Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка (" + e.getMessage() + ")", Registration.class, getParameters());
        } catch (API.IncorrectLoginFormatException e) {
            Message.showMessage(getLauncherUI(), "Проблемка", "Логин имеет неверный формат!", Registration.class, getParameters());
        } catch (API.LoginAlreadyExistException e) {
            Message.showMessage(getLauncherUI(), "Проблемка", "Данное имя уже занято...", Registration.class, getParameters());
        } catch (API.IncorrectPasswordFormatException e) {
            Message.showMessage(getLauncherUI(), "Проблемка", "Пароль имеет неверный формат!", Registration.class, getParameters());
        }
    }
}
