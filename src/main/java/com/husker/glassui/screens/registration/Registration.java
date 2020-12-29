package com.husker.glassui.screens.registration;

import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPasswordField;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.screens.login.Login;
import com.husker.launcher.discord.Discord;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.utils.FormatUtils;
import com.husker.glassui.screens.TitledLogoScreen;

import javax.swing.*;
import java.awt.*;


public class Registration extends TitledLogoScreen {

    private BlurTextField loginField;
    private BlurPasswordField passwordField;
    private BlurPasswordField password1Field;

    private BlurButton nextButton;

    public void createMenu(TransparentPanel panel) {
        setTitle("Регистрация");

        panel.add(createLabel("Имя"));
        panel.add(loginField = new BlurTextField(this){{
            addTextListener(text -> updateButton());
            addFastAction(() -> event());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Пароль"));
        panel.add(passwordField = new BlurPasswordField(this){{
            addTextListener(text -> updateButton());
            addFastAction(() -> event());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        panel.add(createLabel("Подтверждение пароля"));
        panel.add(password1Field = new BlurPasswordField(this){{
            addTextListener(text -> updateButton());
            addFastAction(() -> event());
        }});
    }

    public void updateButton(){
        new Thread(() -> {
            nextButton.setEnabled(passwordField.getText().equals(password1Field.getText()) && FormatUtils.isCorrectPassword(passwordField.getText()) && FormatUtils.isCorrectName(loginField.getText()));
        }).start();
    }

    public void createComponents(TransparentPanel panel) {
        nextButton = createButton("Далее", this::event);
        nextButton.setEnabled(false);
        panel.add(nextButton);
    }

    public void event(){
        getLauncherUI().setScreen(RegistrationProgress.class, new Parameters(){{
            put("login", loginField.getText());
            put("password", passwordField.getText());
        }});
    }

    public void onShow(){
        super.onShow();
        Discord.setState(Discord.Texts.InRegistration);

        loginField.setText(getParameterValue("login", ""));
        passwordField.clear();
        password1Field.clear();
    }

    public void createSubComponents(TransparentPanel panel) {
        panel.add(createSubLabel("Войти в аккаунт", () -> getLauncherUI().setScreen(Login.class)));
    }
}
