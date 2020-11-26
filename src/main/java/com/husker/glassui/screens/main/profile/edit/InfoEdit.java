package com.husker.glassui.screens.main.profile.edit;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPasswordField;
import com.husker.glassui.components.BlurTextField;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.API;
import com.husker.launcher.utils.FormatUtils;

import javax.swing.*;
import java.awt.*;


public class InfoEdit extends InfoEditPanel {

    private BlurTextField nickname, email;
    private BlurPasswordField password;
    private BlurButton apply;

    public void onContentInit(TransparentPanel panel) {
        panel.add(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(0, 0));
            setMargin(0, 30, 0, 30);

            add(createTitleLabel("Имя"));
            add(nickname = createTextField());
            nickname.addTextListener(text -> updateApplyButton());
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(createTitleLabel("Почта"));
            add(email = createTextField());
            email.addTextListener(text -> updateApplyButton());
        }});

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createSeparator());

        panel.add(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(0, 0));
            setMargin(0, 30, 0, 30);

            add(createTitleLabel("Текущий пароль"));
            add(password = createPasswordField());
            password.addTextListener(text -> updateApplyButton());
        }});
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.add(new BlurButton(InfoEdit.this, "Назад"){{
            setPreferredWidth(120);
            addActionListener(e -> getLauncherUI().setScreen("main"));
        }});
        panel.add(apply = new BlurButton(InfoEdit.this, "Применить"){{
            setEnabled(false);
            setPreferredWidth(120);
            addActionListener(e -> {
                if(!email.getText().equals(getLauncher().User.getEmail())){
                    getLauncherUI().setScreen(SendingCode.class, new Parameters(){{
                        put(API.PASSWORD, password.getText());
                        put(API.EMAIL, email.getText());
                        if(!getLauncher().User.getNickname().equals(nickname.getText()))
                            put(API.LOGIN, nickname.getText());
                    }});
                }else{
                    getLauncherUI().setScreen(InfoApplying.class, new Parameters(){{
                        put(API.PASSWORD, password.getText());
                        put(API.LOGIN, nickname.getText());
                    }});
                }
            });
        }});
    }

    public void updateApplyButton(){
        new Thread(() -> {
            apply.setEnabled(FormatUtils.isCorrectEmail(email.getText()) && FormatUtils.isCorrectName(nickname.getText()) && FormatUtils.isCorrectPassword(password.getText()));
        }).start();
    }

    public void onShow() {
        super.onShow();

        nickname.setText(getParameterValue("login", getLauncher().User.getNickname()));
        email.setText(getParameterValue("email", getLauncher().User.getEmail()));

        password.clear();
    }

}
