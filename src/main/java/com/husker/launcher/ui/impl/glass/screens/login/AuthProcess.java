package com.husker.launcher.ui.impl.glass.screens.login;

import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;
import com.husker.launcher.ui.impl.glass.Message;


public class AuthProcess extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Вход");
        setText("Проверка соединения...");
    }

    public void onShow() {
        new Thread(() -> {
            setText("Авторизация...");

            boolean encrypted = getParameterValue("encrypted", "false").equals("true");
            String login = getParameterValue("login");
            String password = getParameterValue("password");

            int result = getLauncher().NetManager.Auth.auth(login, password, encrypted);
            if(result == getLauncher().NetManager.Auth.OK) {
                if(getLauncher().getSettings().get("auto_auth", "false").equals("true")) {
                    getLauncher().getUserConfig().set("login", login);
                    getLauncher().getUserConfig().set("password", encrypted ? password : getLauncher().NetManager.PlayerInfo.getEncryptedPassword());
                }else{
                    getLauncher().getUserConfig().set("login", "null");
                    getLauncher().getUserConfig().set("password", "null");
                }

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

        }).start();
    }

}
