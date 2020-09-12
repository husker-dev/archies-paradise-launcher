package com.husker.glassui.screens.registration;

import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.GlassUI;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.utils.FormatUtils;
import com.husker.glassui.screens.TitledLogoScreen;

public class R_3_Email extends TitledLogoScreen {

    private BlurTextField email;
    private BlurButton nextButton;

    public void createMenu(TransparentPanel panel) {
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

    public void createComponents(TransparentPanel panel) {
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

    public void createSubComponents(TransparentPanel panel) {

    }
}
