package com.husker.launcher.ui.impl.glass.screens.login;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.ui.impl.glass.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPasswordField;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.FormatUtils;

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
            addTextListener(text -> updateLoginButton());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Пароль"));
        panel.add(passwordField = new BlurPasswordField(Login.this){{
            addTextListener(text -> updateLoginButton());
        }});
    }

    public void updateLoginButton(){
        new Thread(() -> {
            loginButton.setEnabled(FormatUtils.isCorrectName(loginField.getText()) && FormatUtils.isCorrectPassword(passwordField.getText()));
        }).start();
    }

    public void createComponents(WebPanel panel) {
        loginButton = createButton(1, "Войти", () -> {
            getLauncherUI().setScreen("authProcess", new Parameters(){{
                put("login", loginField.getText());
                put("password", new String(passwordField.getPassword()));
                put("encrypted", "false");
            }});
        });
        loginButton.setEnabled(false);
        panel.add(loginButton);
    }

    public void onShow(){
        super.onShow();

        loginField.setText(getParameterValue("login", ""));
        passwordField.clear();
    }

    public void createSubComponents(WebPanel panel) {
        panel.add(createSubLabel("Зарегистрироваться", () -> getLauncherUI().setScreen("registration")));
    }
}
