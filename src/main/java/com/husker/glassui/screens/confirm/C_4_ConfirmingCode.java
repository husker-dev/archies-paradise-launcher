package com.husker.glassui.screens.confirm;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.login.Login;
import com.husker.glassui.screens.login.LoginProcess;

public class C_4_ConfirmingCode extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка кода...");
    }

    public void process() {
        String login = getParameterValue("login");
        String password = getParameterValue("password");
        String email = getParameterValue("email");
        String code = getParameterValue("code");

        int result = getLauncher().NetManager.PlayerInfo.confirmMail(email, code);
        if(result == 0)
            Message.showMessage(getLauncherUI(), "Уведомление", "Аккаунт был создан!", LoginProcess.class, new Parameters(){{
                put("login", login);
                put("password", password);
            }});
        if(result == -1) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Код не подходит", C_3_Code.class, new Parameters(){{
                put("login", login);
                put("password", password);
                put("email", email);
            }});
        }
    }
}
