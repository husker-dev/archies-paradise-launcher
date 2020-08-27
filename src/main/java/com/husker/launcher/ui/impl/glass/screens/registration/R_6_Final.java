package com.husker.launcher.ui.impl.glass.screens.registration;

import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class R_6_Final extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Регистрация");
        setText("Проверка кода...");
    }

    public void onShow() {
        super.onShow();

        new Thread(() -> {
            int result = getLauncher().NetManager.Email.confirmMail(getParameterLogin(), getParameterPassword(), getParameterMail(), getParameterMailCode(), getParameters()[0].equals("[encrypted]"));
            if(result == getLauncher().NetManager.Email.OK)
                getLauncherUI().setScreen("message", "authProcess," + getParameterLogin() + "," + getParameterPassword() + "," + getParameters()[0].equals("[encrypted]"), "Уведомление", "Аккаунт создан!");
            if(result == getLauncher().NetManager.Email.ERROR) {
                String encryptedCase = "";
                if(getParameters()[0].equals("[encrypted]"))
                    encryptedCase += "," + getParameters()[3];

                getLauncherUI().setScreen("message", "emailConfirm," + getParameters()[0] + "," + getParameters()[1] + "," + getParameters()[2] + encryptedCase, "Ошибка", "Код не подходит");
            }
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

    private String getParameterMailCode(){
        if(getParameters()[0].equals("[encrypted]"))
            return getParameters()[4];
        else
            return getParameters()[3];
    }
}
