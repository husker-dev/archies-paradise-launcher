package com.husker.glassui.screens.confirm;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.login.Login;
import com.husker.glassui.screens.login.LoginProcess;
import com.husker.launcher.api.API;

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

        try {
            if(getLauncher().User.confirmMail(email, code)){
                Message.showMessage(getLauncherUI(), "Уведомление", "Приятной игры!", LoginProcess.class, new Parameters(){{
                    put("login", login);
                    put("password", password);
                }});
            }else{
                Message.showMessage(getLauncherUI(), "Ошибка", "Код не подходит", C_3_Code.class, new Parameters(){{
                    put("login", login);
                    put("password", password);
                    put("email", email);
                }});
            }
        } catch (API.EmailIsNotSpecifiedException e) {
            Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка подтверждения кода!", C_3_Code.class, new Parameters(){{
                put("login", login);
                put("password", password);
                put("email", email);
            }});
        } catch (API.IncorrectEmailCodeException e) {
            e.printStackTrace();
        }
    }
}
