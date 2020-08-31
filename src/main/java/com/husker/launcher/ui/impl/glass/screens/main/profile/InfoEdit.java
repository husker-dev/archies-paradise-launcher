package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.utils.FormatUtils;
import com.husker.launcher.ui.impl.glass.components.*;

import javax.swing.*;
import java.awt.*;


public class InfoEdit extends InfoEditPanel {

    private BlurTextField nickname, email;
    private BlurPasswordField password;
    private BlurButton apply;

    public void onContentInit(WebPanel panel) {
        panel.add(new WebPanel(StyleId.panelTransparent){{
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

        panel.add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new VerticalFlowLayout(0, 0));
            setMargin(0, 30, 0, 30);

            add(createTitleLabel("Текущий пароль"));
            add(password = createPasswordField());
            password.addTextListener(text -> updateApplyButton());
        }});
    }

    public void onButtonsInit(WebPanel panel) {
        panel.add(new BlurButton(InfoEdit.this, "Назад"){{
            setPreferredWidth(120);
            addActionListener(e -> getLauncherUI().setScreen("main"));
        }});
        panel.add(apply = new BlurButton(InfoEdit.this, "Применить"){{
            setEnabled(false);
            setPreferredWidth(120);
            addActionListener(e -> {

                if(!email.getText().equals(getLauncher().NetManager.PlayerInfo.getEmail())){
                    getLauncherUI().setScreen("info_email_confirm", new Parameters(){{
                        put("currentPassword", password.getText());
                        put(NetManager.LOGIN, nickname.getText());
                        put(NetManager.EMAIL, email.getText());
                    }});
                }else{
                    getLauncherUI().setScreen("info_edit_apply", new Parameters(){{
                        put("currentPassword", password.getText());
                        put(NetManager.LOGIN, nickname.getText());
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

        nickname.setText(getParameterValue("login", getLauncher().NetManager.PlayerInfo.getNickname()));
        email.setText(getParameterValue("email", getLauncher().NetManager.PlayerInfo.getEmail()));

        password.clear();
    }

}
