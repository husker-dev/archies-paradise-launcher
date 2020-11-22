package com.husker.glassui.screens.login;

import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.confirm.C_1_Email;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.utils.ConsoleUtils;


public class LoginProcess extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Вход");
        setText("Проверка соединения...");
    }

    public void process() {
        setText("Авторизация...");

        String login = getParameterValue("login");
        String password = getParameterValue("password");

        getLauncher().getUserConfig().setLogin(login);
        getLauncher().getUserConfig().setPassword(password);

        int result = getLauncher().API.Auth.auth(login, password);

        if(result == getLauncher().API.Auth.OK) {
            if(getLauncher().API.PlayerInfo.isEmailConfirmed())
                getLauncherUI().setScreen(MainScreen.class);
            else
                getLauncherUI().setScreen(C_1_Email.class, new Parameters(){{
                    put("login", login);
                    put("password", password);
                }});
            return;
        }
        if(result == getLauncher().API.Auth.CONNECTION_ERROR) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Сервер авторизации недоступен!", Login.class, new Parameters("login", login, "password", password));
            return;
        }
        if(result == getLauncher().API.Auth.WRONG_DATA) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Неправильный логин или пароль", Login.class, new Parameters("login", login));
            return;
        }
        Message.showMessage(getLauncherUI(), "Ошибка", "Сервер вернул неизвестный код...", Login.class, new Parameters("login", login, "password", password));

    }

}
