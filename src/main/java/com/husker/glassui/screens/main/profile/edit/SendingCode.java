package com.husker.glassui.screens.main.profile.edit;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.launcher.managers.API;
import com.husker.launcher.managers.NetManager;

public class SendingCode extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Подтверждение");
        setText("Отправка кода подтверждения...");
    }

    public void process() {
        String email = getParameterValue(API.EMAIL);

        int result = getLauncher().API.PlayerInfo.sendConfirmCode(email);

        if(result == -1)
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", InfoEdit.class, getParameters());
        else
            getLauncherUI().setScreen(EmailConfirm.class, getParameters());
    }
}
