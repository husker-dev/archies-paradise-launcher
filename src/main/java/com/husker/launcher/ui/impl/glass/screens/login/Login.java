package com.husker.launcher.ui.impl.glass.screens.login;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.ui.impl.glass.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPasswordField;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;
import com.husker.launcher.utils.ConsoleUtils;

import javax.swing.*;
import java.awt.*;

public class Login extends TitledScreen {


    private BlurTextField loginField;
    private BlurPasswordField passwordField;

    private BlurButton loginButton;

    public void createMenu(WebPanel panel) {
        setTitle("Авторизация");


        panel.add(createLabel("Имя"));
        panel.add(loginField = new BlurTextField(Login.this){{
            addTextListener(text -> {
                loginButton.setEnabled(!passwordField.isEmpty() && passwordField.getPassword().length >= 5 && !loginField.getText().isEmpty());
            });
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Пароль"));
        panel.add(passwordField = new BlurPasswordField(Login.this){{
            addTextListener(text -> {
                loginButton.setEnabled(!passwordField.isEmpty() && passwordField.getPassword().length >= 5 && !loginField.getText().isEmpty());
            });
        }});

    }

    public void createComponents(WebPanel panel) {
        loginButton = createButton(1, "Войти", () -> {
            getLauncherUI().setScreen("authProcess", loginField.getText(), new String(passwordField.getPassword()));
        });
        loginButton.setEnabled(false);
        panel.add(loginButton);
    }

    public void createSubComponents(WebPanel panel) {
        panel.add(createSubLabel("Зарегистрироваться", () -> getLauncherUI().setScreen("registration")));
    }
}
