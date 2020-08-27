package com.husker.launcher.ui.impl.glass.screens.registration;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.utils.FormatUtils;
import com.husker.launcher.ui.impl.glass.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPasswordField;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;

import javax.swing.*;
import java.awt.*;


public class R_1_LoginNPassword extends TitledScreen {

    private BlurTextField loginField;
    private BlurPasswordField passwordField;
    private BlurPasswordField password1Field;

    private BlurButton nextButton;

    public void createMenu(WebPanel panel) {
        setTitle("Регистрация");

        panel.add(createLabel("Имя"));
        panel.add(loginField = new BlurTextField(this){{
            addTextListener(text -> updateButton());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Пароль"));
        panel.add(passwordField = new BlurPasswordField(this){{
            addTextListener(text -> updateButton());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Подверждение пароля"));
        panel.add(password1Field = new BlurPasswordField(this){{
            addTextListener(text -> updateButton());
        }});
    }

    public void updateButton(){
        nextButton.setEnabled(passwordField.getText().equals(password1Field.getText()) && FormatUtils.isCorrectPassword(passwordField.getText()) && FormatUtils.isCorrectName(loginField.getText()));
    }

    public void createComponents(WebPanel panel) {
        nextButton = createButton(1, "Далее", () -> {
            getLauncherUI().setScreen("nicknameCheck", loginField.getText(), passwordField.getText());
        });
        nextButton.setEnabled(false);
        panel.add(nextButton);
    }

    public void createSubComponents(WebPanel panel) {
        panel.add(createSubLabel("Войти в аккаунт", () -> getLauncherUI().setScreen("login")));
    }
}
