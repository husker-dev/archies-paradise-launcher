package com.husker.launcher.ui.impl.glass.screens.registration;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.FormatUtils;
import com.husker.launcher.ui.impl.glass.screens.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;

public class R_3_Email extends TitledScreen {

    private BlurTextField email;
    private BlurButton nextButton;

    public void createMenu(WebPanel panel) {
        setTitle("Регистрация");

        panel.add(createLabel("Электронная почта"));
        panel.add(email = new BlurTextField(this){{
            addTextListener(text -> updateNextButton());
        }});
    }

    public void updateNextButton(){
        new Thread(() -> {
            nextButton.setEnabled(FormatUtils.isCorrectEmail(email.getText()));
        }).start();
    }

    public void createComponents(WebPanel panel) {
        nextButton = createButton(2, "Далее", () -> {
            getLauncherUI().setScreen("sendEmail", new Parameters(){{
                put("login", getParameter("login"));
                put("password", getParameter("password"));
                put("email", email.getText());
                put("encrypted", getParameterValue("encrypted", "false"));
                put("fromLogin", getParameterValue("fromLogin", "false"));
            }});
        });
        nextButton.setEnabled(false);
        panel.add(createButton(2, "Назад", () -> {
            if(getParameterValue("fromLogin", "false").equals("true"))
                ((GlassUI)getLauncherUI()).logout();
            else
                getLauncherUI().setScreen("registration", new Parameters(){{
                    put("login", getParameter("login"));
                }});
        }));
        panel.add(nextButton);
    }

    public void onShow(){
        super.onShow();

        email.setText(getParameterValue("email", ""));
    }

    public void createSubComponents(WebPanel panel) {

    }
}
