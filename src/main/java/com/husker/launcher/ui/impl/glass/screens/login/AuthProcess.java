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

            boolean encrypted = getParameters().length > 2 && getParameters()[2].equals("true");

            int result = getLauncher().NetManager.Auth.auth(getParameters()[0], getParameters()[1], encrypted);
            if(result == getLauncher().NetManager.Auth.OK) {
                if(getLauncher().getSettings().get("auto_auth", "false").equals("true")) {
                    getLauncher().getUserConfig().set("login", getParameters()[0]);
                    getLauncher().getUserConfig().set("password", encrypted ? getParameters()[1] : getLauncher().NetManager.PlayerInfo.getEncryptedPassword());
                }else{
                    getLauncher().getUserConfig().set("login", "null");
                    getLauncher().getUserConfig().set("password", "null");
                }

                if(getLauncher().NetManager.PlayerInfo.isEmailConfirmed())
                    getLauncherUI().setScreen("main");

                else
                    getLauncherUI().setScreen("registration_1", "[encrypted]", getLauncher().NetManager.PlayerInfo.getNickname(), getLauncher().NetManager.PlayerInfo.getEncryptedPassword());

                return;
            }
            if(result == getLauncher().NetManager.Auth.CONNECTION_ERROR) {
                Message.showMessage(getLauncherUI(), "Ошибка", "Сервер авторизации недоступен!", "login");
                return;
            }
            if(result == getLauncher().NetManager.Auth.WRONG_DATA) {
                Message.showMessage(getLauncherUI(), "Ошибка", "Неправильный логин или пароль", "login");
                return;
            }
            Message.showMessage(getLauncherUI(), "Ошибка", "Сервер вернул неизвестный код...", "login");

        }).start();
    }

}
