package com.husker.launcher.ui.impl.glass.screens.registration;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.utils.FormatUtils;
import com.husker.launcher.ui.impl.glass.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;

public class R_3_Email extends TitledScreen {

    private BlurTextField email;
    private BlurButton nextButton;

    public void createMenu(WebPanel panel) {
        setTitle("Регистрация");

        panel.add(createLabel("Электронная почта"));
        panel.add(email = new BlurTextField(this){{
            addTextListener(text -> nextButton.setEnabled(FormatUtils.isCorrectEmail(email.getText())));
        }});
    }

    public void createComponents(WebPanel panel) {
        nextButton = createButton(2, "Далее", () -> {
            getLauncherUI().setScreen("sendEmail", getParameters(email.getText()));
        });
        nextButton.setEnabled(false);
        panel.add(createButton(2, "Назад", () -> {
            if(getParameters()[0].equals("[encrypted]")){
                getLauncher().NetManager.PlayerInfo.logout();
                getLauncher().getUserConfig().set("login", "null");
                getLauncher().getUserConfig().set("password", "null");
                getLauncherUI().setScreen("login");
            }else
                getLauncherUI().setScreen("registration");
        }));
        panel.add(nextButton);
    }

    public void createSubComponents(WebPanel panel) {

    }
}
