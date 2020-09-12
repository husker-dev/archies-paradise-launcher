package com.husker.glassui.screens.login;

import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.Message;


public class AuthProcess extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Вход");
        setText("Проверка соединения...");
    }

    public void process() {
        setText("Авторизация...");

        boolean encrypted = getParameterValue("encrypted", "false").equals("true");
        String login = getParameterValue("login");
        String password = getParameterValue("password");

        int result = getLauncher().NetManager.Auth.auth(login, password, encrypted);
        if(result == getLauncher().NetManager.Auth.OK) {
            if(getLauncher().getSettings().isAutoAuth()) {
                getLauncher().getUserConfig().setLogin(login);
                getLauncher().getUserConfig().setPassword(encrypted ? password : getLauncher().NetManager.PlayerInfo.getEncryptedPassword());
            }else
                getLauncher().getUserConfig().reset();

            if(getLauncher().NetManager.PlayerInfo.isEmailConfirmed())
                getLauncherUI().setScreen("main");
            else
                getLauncherUI().setScreen("registration_1", new Parameters(){{
                    put("login", login);
                    put("password", password);
                    put("encrypted", encrypted);
                    put("fromLogin", "true");
                }});

            return;
        }
        if(result == getLauncher().NetManager.Auth.CONNECTION_ERROR) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Сервер авторизации недоступен!", "login", new Parameters("login", login));
            return;
        }
        if(result == getLauncher().NetManager.Auth.WRONG_DATA) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Неправильный логин или пароль", "login", new Parameters("login", login));
            return;
        }
        Message.showMessage(getLauncherUI(), "Ошибка", "Сервер вернул неизвестный код...", "login", new Parameters("login", login));

    }

}