package com.husker.glassui.screens.confirm;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;

public class C_2_SendingCode extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Отправка проверочного кода...");
    }

    public void process() {
        String email = getParameterValue("email");

        int result = getLauncher().NetManager.PlayerInfo.sendConfirmCode(email);
        if(result == 0)
            getLauncherUI().setScreen(C_3_Code.class, getParameters());
        if(result == -1)
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", C_1_Email.class, getParameters());
    }
}
