package com.husker.glassui.screens.main.profile.edit;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.managers.style.StyleId;
import com.husker.glassui.components.BlurTextField;
import com.husker.launcher.Resources;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.managers.NetManager;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.screens.Message;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class EmailConfirm extends InfoEditPanel{

    private BlurButton next, resend;
    private BlurTextField code;
    private WebStyledLabel emailLabel;

    private int seconds;

    public void onContentInit(TransparentPanel panel) {
        setSubTitle("Подтверждение");

        new Timer().schedule(new TimerTask() {
            public void run() {
                if(resend == null)
                    return;

                if(seconds > 0)
                    seconds--;
                if(seconds > 0 && seconds <= 60)
                    resend.setText("Повторить (" + seconds + ")");
                if(seconds == 0)
                    resend.setText("Повторить");
                resend.setEnabled(seconds <= 0);
            }
        }, 0, 1000);

        panel.add(new TransparentPanel(){{
            setMargin(0, 20, 0, 20);

            add(emailLabel = new WebStyledLabel(){{
                setMaximumRows(3);
                setForeground(GlassUI.Colors.labelLightText);
                setPreferredHeight(80);
                setFont(Resources.Fonts.ChronicaPro_ExtraBold);
            }});
        }});
        panel.add(createSeparator());
        panel.add(new TransparentPanel(){{
            setMargin(0, 50, 0, 50);

            setLayout(new VerticalFlowLayout());
            add(createTitleLabel("Код"));
            add(code = new BlurTextField(EmailConfirm.this){{
                addTextListener(text -> new Thread(() -> next.setEnabled(code.getText().length() == 6)).start());
            }});
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(resend = new BlurButton(EmailConfirm.this, "Повторить"){{
                addActionListener(e -> send());
            }});
        }});
    }

    public void onButtonsInit(TransparentPanel panel) {
        panel.add(new BlurButton(this, "Назад"){{
            setPreferredWidth(120);
            addActionListener(e -> getLauncherUI().setScreen("info_edit", new Parameters(NetManager.LOGIN, getParameterValue(NetManager.LOGIN), NetManager.EMAIL, getParameterValue(NetManager.EMAIL))));
        }});
        panel.add(next = new BlurButton(this, "Далее"){{
            setEnabled(false);
            setPreferredWidth(120);
            addActionListener(e -> {
                getLauncherUI().setScreen("info_edit_apply", new Parameters(){{
                    put("currentPassword", getParameter("currentPassword"));
                    put(NetManager.LOGIN, getParameter(NetManager.LOGIN));
                    put(NetManager.EMAIL, getParameter(NetManager.EMAIL));
                    put(NetManager.EMAIL_CODE, code.getText());
                }});
            });
        }});
    }

    public void send(){
        seconds = Integer.MAX_VALUE;

        resend.setText("Отправка...");
        resend.setEnabled(false);

        new Thread(() -> {
            String login = getLauncher().NetManager.PlayerInfo.getNickname();
            String password = getLauncher().NetManager.PlayerInfo.getEncryptedPassword();
            String email = getParameterValue(NetManager.EMAIL);

            int result = getLauncher().NetManager.Email.sendConfirmCode(login, password, email, true);

            if(result == getLauncher().NetManager.Email.ERROR)
                Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", "info_email_confirm", getParameters());

            seconds = 60;
        }).start();
    }

    public void onShow(){
        super.onShow();

        code.setText("");
        seconds = 60;
        resend.setEnabled(false);
        emailLabel.setText("Код подтверждения был отправлен на {" + getParameterValue(NetManager.EMAIL) + " :c(" + GlassUI.Colors.labelText.getRed() + "," + GlassUI.Colors.labelText.getGreen() + "," + GlassUI.Colors.labelText.getBlue() + ")}");

        send();
    }
}
