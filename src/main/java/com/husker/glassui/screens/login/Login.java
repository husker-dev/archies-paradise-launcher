package com.husker.glassui.screens.login;

import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPasswordField;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.screens.TitledLogoScreen;
import com.husker.glassui.screens.registration.Registration;
import com.husker.glassui.screens.reset.Reset;
import com.husker.launcher.discord.Discord;
import com.husker.launcher.ui.components.TransparentPanel;
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
            addFastAction(() -> event());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Пароль"));
        panel.add(passwordField = new BlurPasswordField(Login.this){{
            addTextListener(text -> updateLoginButton());
            addFastAction(() -> event());
        }});
    }

    public void updateLoginButton(){
        new Thread(() -> {
            loginButton.setEnabled(FormatUtils.isCorrectName(loginField.getText()) && FormatUtils.isCorrectPassword(passwordField.getText()));
        }).start();
    }

    public void createComponents(TransparentPanel panel) {
        BlurButton createButton = createButton("Создать", () -> getLauncherUI().setScreen(Registration.class));
        loginButton = createButton("Войти", this::event);
        loginButton.setEnabled(false);
        panel.add(createButton);
        panel.add(loginButton);
    }

    public void event(){
        getLauncherUI().setScreen(LoginProcess.class, new Parameters(){{
            put("login", loginField.getText());
            put("password", new String(passwordField.getPassword()));
        }});
    }

    public void onShow(){
        super.onShow();
        Discord.setState(Discord.Texts.InLogin);
        loginField.setText(getParameterValue("login", ""));
        passwordField.setText(getParameterValue("password", ""));
    }

    public void createSubComponents(TransparentPanel panel) {
        panel.add(createSubLabel("Забыли пароль?", () -> {
        getLauncherUI().setScreen(Reset.class);
        }));
    }
}
