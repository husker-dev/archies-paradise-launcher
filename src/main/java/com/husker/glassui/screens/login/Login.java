package com.husker.glassui.screens.login;

import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPasswordField;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.screens.TitledLogoScreen;
import com.husker.glassui.screens.registration.Registration;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.utils.FormatUtils;

import javax.swing.*;
import java.awt.*;

public class Login extends TitledLogoScreen {


    private BlurTextField loginField;
    private BlurPasswordField passwordField;

    private BlurButton loginButton;

    public void createMenu(TransparentPanel panel) {
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

    public void createComponents(TransparentPanel panel) {
        loginButton = createButton(1, "Войти", () -> {
            getLauncherUI().setScreen(LoginProcess.class, new Parameters(){{
                put("login", loginField.getText());
                put("password", new String(passwordField.getPassword()));
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

    public void createSubComponents(TransparentPanel panel) {
        panel.add(createSubLabel("Зарегистрироваться", () -> getLauncherUI().setScreen(Registration.class)));
        //panel.add(createSubLabel("Восстановить пароль", () -> getLauncherUI().setScreen(Registration.class)));
    }
}
