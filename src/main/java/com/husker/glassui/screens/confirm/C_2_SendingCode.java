package com.husker.glassui.screens.confirm;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.launcher.api.API;

public class C_2_SendingCode extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Отправка проверочного кода...");
    }

    public void process() {
        try {
            getLauncher().User.sendConfirmCode(getParameterValue("email"));
            getLauncherUI().setScreen(C_3_Code.class, getParameters());
        } catch (API.EmailCodeSendingException e) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", C_1_Email.class, getParameters());
        } catch (API.EmailAlreadyExistException e) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Данный email уже привязан а аккаунту", C_1_Email.class, getParameters());
        }
    }
}
