package com.husker.glassui.screens.main.profile.edit;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.BlurTextField;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.API;
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
                setFont(Resources.Fonts.getChronicaProExtraBold());
            }});
        }});
        panel.add(createSeparator());
        panel.add(new TransparentPanel(){{
            setMargin(0, 50, 0, 50);

            setLayout(new VerticalFlowLayout());
            add(createTitleLabel("Код"));
            add(code = new BlurTextField(EmailConfirm.this){{
                addTextListener(text -> new Thread(() -> next.setEnabled(code.getText().length() == 6)).start());
                addFastAction(() -> event());
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
            addActionListener(e -> getLauncherUI().setScreen(InfoEdit.class, new Parameters(API.LOGIN, getParameterValue(API.LOGIN), API.EMAIL, getParameterValue(API.EMAIL))));
        }});
        panel.add(next = new BlurButton(this, "Далее"){{
            setEnabled(false);
            setPreferredWidth(120);
            addActionListener(e -> event());
        }});
    }

    public void event(){
        getLauncherUI().setScreen(InfoApplying.class, new Parameters(){{
            put(API.PASSWORD, getParameter(API.PASSWORD));
            if(getParameters().containsKey(API.LOGIN))
                put(API.LOGIN, getParameter(API.LOGIN));
            put(API.EMAIL, getParameter(API.EMAIL));
            put(API.EMAIL_CODE, code.getText());
        }});
    }

    public void send(){
        seconds = Integer.MAX_VALUE;

        resend.setText("Отправка...");
        resend.setEnabled(false);

        new Thread(() -> {
            seconds = 60;

            try {
                getLauncher().User.sendConfirmCode(getParameterValue(API.EMAIL));
            } catch (API.EmailCodeSendingException e) {
                Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", EmailConfirm.class, getParameters());
            } catch (API.EmailAlreadyExistException e) {
                Message.showMessage(getLauncherUI(), "Ошибка", "Данный email уже привязан а аккаунту", EmailConfirm.class, getParameters());
            }
        }).start();
    }

    public void onShow(){
        super.onShow();

        code.setText("");
        seconds = 60;
        resend.setEnabled(false);
        emailLabel.setText("Код подтверждения был отправлен на {" + getParameterValue(API.EMAIL) + " :c(" + GlassUI.Colors.labelText.getRed() + "," + GlassUI.Colors.labelText.getGreen() + "," + GlassUI.Colors.labelText.getBlue() + ")}");
    }
}
