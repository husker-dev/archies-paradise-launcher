package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class R_4_SendEmail extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Отправка проверочного кода...");
    }

    public void onShow() {
        new Thread(() -> {

            int result = getLauncher().NetManager.Email.sendConfirmCode(getParameterLogin(), getParameterPassword(), getParameterMail(), getParameters()[0].equals("[encrypted]"));
            if(result == getLauncher().NetManager.Email.OK)
                getLauncherUI().setScreen("emailConfirm", getParameters());
            if(result == getLauncher().NetManager.Email.ERROR)
                getLauncherUI().setScreen("message", "registration_1," + getParameters()[0] + "," + getParameters()[1], "Ошибка", "Ошибка отправки кода");
        }).start();
    }

    private String getParameterLogin(){
        if(getParameters()[0].equals("[encrypted]"))
            return getParameters()[1];
        else
            return getParameters()[0];
    }

    private String getParameterPassword(){
        if(getParameters()[0].equals("[encrypted]"))
            return getParameters()[2];
        else
            return getParameters()[1];
    }

    private String getParameterMail(){
        if(getParameters()[0].equals("[encrypted]"))
            return getParameters()[3];
        else
            return getParameters()[2];
    }
}
