package com.husker.glassui.screens.main.profile.edit;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.launcher.api.API;

public class SendingCode extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Подтверждение");
        setText("Отправка кода подтверждения...");
    }

    public void process() {
        try {
            getLauncher().User.sendConfirmCode(getParameterValue(API.EMAIL));
            getLauncherUI().setScreen(EmailConfirm.class, getParameters());
        } catch (API.EmailCodeSendingException | API.EmailAlreadyExistException e) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", InfoEdit.class, getParameters());
        }
    }
}
