package com.husker.glassui.screens.confirm;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurTextField;
import com.husker.launcher.Resources;
import com.husker.glassui.GlassUI;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.TitledLogoScreen;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.components.TransparentPanel;

import java.util.Timer;
import java.util.TimerTask;

public class C_3_Code extends TitledLogoScreen {

    private BlurTextField code;
    private BlurButton nextButton;
    private WebStyledLabel emailLabel;

    private BlurButton resendButton;

    private int seconds;

    public void createMenu(TransparentPanel panel) {
        setTitle("Подтверждение");

        new Timer().schedule(new TimerTask() {
            public void run() {
                if(resendButton == null)
                    return;

                if(seconds > 0)
                    seconds--;
                if(seconds > 0) {
                    resendButton.setText("Повторить (" + seconds + ")");
                }else
                    resendButton.setText("Повторить");
                resendButton.setEnabled(seconds <= 0);
            }
        }, 0, 1000);

        panel.setMargin(0, 20, 0, 20);
        emailLabel = new WebStyledLabel(WebStyledLabel.CENTER){{
            setMaximumRows(3);
            setForeground(GlassUI.Colors.labelLightText);
            setPreferredHeight(80);
            setFont(Resources.Fonts.getChronicaProExtraBold());
        }};
        panel.add(emailLabel);
        panel.add(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout(0, 6));
            setMargin(0, 40, 0, 40);

            add(createLabel("Код (6 цифр)"));
            add(code = new BlurTextField(C_3_Code.this){{
                addTextListener(text -> nextButton.setEnabled(code.getText().length() == 6));
                addFastAction(() -> event());
            }});
            add(resendButton = createButton("Повторить", () -> {
                seconds = 60;

                new Thread(() -> {
                    try {
                        getLauncher().User.sendConfirmCode(getParameterValue("email"));
                    } catch (API.EmailCodeSendingException | API.EmailAlreadyExistException e) {
                        Message.showMessage(getLauncherUI(), "Ошибка", "Ошибка отправки кода", "emailConfirm", getParameters());
                    }
                }).start();
            }));
        }});
    }

    public void createComponents(TransparentPanel panel) {
        nextButton = createButton("Далее", this::event);
        nextButton.setEnabled(false);
        panel.add(createButton("Назад", () -> getLauncherUI().setScreen(C_1_Email.class, getParameters())));
        panel.add(nextButton);

    }

    public void event(){
        getLauncherUI().setScreen(C_4_ConfirmingCode.class, new Parameters(){{
            put("login", getParameter("login"));
            put("password", getParameter("password"));
            put("email", getParameter("email"));
            put("code", code.getText());
        }});
    }

    public void createSubComponents(TransparentPanel panel) {
    }

    public void onShow() {
        super.onShow();
        code.setText("");
        seconds = 60;
        resendButton.setEnabled(false);
        emailLabel.setText("Код подтверждения был отправлен на {" + getParameterValue("email") + " :c(" + GlassUI.Colors.labelText.getRed() + "," + GlassUI.Colors.labelText.getGreen() + "," + GlassUI.Colors.labelText.getBlue() + ")}");
    }
}
