package com.husker.glassui.screens.reset;

import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.TitledLogoScreen;
import com.husker.glassui.screens.login.Login;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.components.TransparentPanel;
import org.json.JSONObject;

public class Reset extends TitledLogoScreen {

    private BlurTextField name;
    private BlurButton createButton, back;

    public void createMenu(TransparentPanel panel) {
        setTitle("Восстановление");

        panel.add(createLabel("Имя или email"));
        panel.add(name = new BlurTextField(Reset.this){{
            addFastAction(() -> event());
        }});
    }

    public void createComponents(TransparentPanel panel) {
        createButton = createButton("Далее", this::event);
        back = createButton("Назад", () -> getLauncherUI().setScreen(Login.class));
        panel.add(back);
        panel.add(createButton);
    }

    public void createSubComponents(TransparentPanel panel) {

    }

    public void event(){
        new Thread(() -> {
            createButton.setEnabled(false);
            back.setEnabled(false);

            try {
                String text = name.getText();
                JSONObject info;
                if (text.contains("@"))
                    info = API.getJSON(ApiMethod.create("profiles.isExist").set("email", text));
                else
                    info = API.getJSON(ApiMethod.create("profiles.isExist").set("login", text));

                if(info.getBoolean("exist") && info.getBoolean("email_bound"))
                    R_1_SendingCode.show(getLauncherUI(), info.getInt("id"), info.getString("name"), Login.class);
                else if(info.has("email_bound") && info.getBoolean("email_bound"))
                    Message.showMessage(getLauncherUI(), "Проблемка", "В профиле не указана почта", Reset.class);
                else
                    Message.showMessage(getLauncherUI(), "Проблемка", "Не удалось найти ваш профиль", Reset.class);
            }catch (Exception ex){
                ex.printStackTrace();
                Message.showMessage(getLauncherUI(), "Проблемка", "Возникла какая-то ошибка...", Reset.class);
            }
            createButton.setEnabled(true);
            back.setEnabled(true);
        }).start();
    }
}
