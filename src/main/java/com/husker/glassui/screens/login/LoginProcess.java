package com.husker.glassui.screens.login;

import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.confirm.C_1_Email;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.User;
import com.husker.launcher.api.API;


public class LoginProcess extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Вход");
    }

    public void process() {
        setText("Авторизация...");

        String login = getParameterValue("login");
        String password = getParameterValue("password");

        try {
            User user = getLauncher().User;
            user.auth(login, password);

            if(user.isEmailConfirmed())
                getLauncherUI().setScreen(MainScreen.class);
            else
                getLauncherUI().setScreen(C_1_Email.class, new Parameters(){{
                    put("login", login);
                    put("password", password);
                }});
        } catch (API.WrongAuthDataException e) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Неправильный логин или пароль", Login.class, new Parameters("login", login));
        } catch (API.InternalAPIException e) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка входа! (" + e.getMessage() + ")", Login.class, new Parameters("login", login, "password", password));
        }
    }

}
