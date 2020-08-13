package com.husker.launcher.ui.impl.glass.screens.registration;

import com.alee.laf.panel.WebPanel;
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
            addTextListener(text -> nextButton.setEnabled(!email.getText().isEmpty() && email.getText().contains("@") && email.getText().contains(".") && !email.getText().endsWith(".") && !email.getText().startsWith("@")));
        }});
    }

    public void createComponents(WebPanel panel) {
        nextButton = createButton(2, "Далее", () -> {
            getLauncherUI().setScreen("sendEmail", getParameters(email.getText()));
        });
        nextButton.setEnabled(false);
        panel.add(createButton(2, "Назад", () -> getLauncherUI().setScreen("registration")));
        panel.add(nextButton);
    }

    public void createSubComponents(WebPanel panel) {

    }
}
