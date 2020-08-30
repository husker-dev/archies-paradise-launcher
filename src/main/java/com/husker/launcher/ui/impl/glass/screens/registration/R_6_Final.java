package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.ui.impl.glass.screens.Message;
import com.husker.launcher.ui.impl.glass.screens.SimpleLoadingScreen;

public class R_6_Final extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка кода...");
    }

    public void onShow() {
        super.onShow();

        new Thread(() -> {
            String login = getParameterValue("login");
            String password = getParameterValue("password");
            String email = getParameterValue("email");
            String code = getParameterValue("code");
            boolean encrypted = getParameterValue("encrypted", "false").equals("true");

            int result = getLauncher().NetManager.Email.confirmMail(login, password, email, code, encrypted);
            if(result == getLauncher().NetManager.Email.OK)
                Message.showMessage(getLauncherUI(), "Уведомление", "Аккаунт был создан!", "authProcess", new Parameters(){{
                    put("login", login);
                    put("password", password);
                    put("encrypted", encrypted);
                }});
            if(result == getLauncher().NetManager.Email.ERROR) {
                Message.showMessage(getLauncherUI(), "Ошибка", "Код не подходит", "emailConfirm", new Parameters(){{
                    put("login", login);
                    put("password", password);
                    put("email", email);
                    put("encrypted", encrypted);
                }});
            }
        }).start();
    }
}
