package com.husker.launcher.ui.impl.glass.screens.registration;

import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.NetManager;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.TitledScreen;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurTextField;

import java.util.Timer;
import java.util.TimerTask;

public class R_5_CodeConfirm extends TitledScreen {

    private BlurTextField code;
    private BlurButton nextButton;
    private WebStyledLabel emailLabel;

    private BlurButton resendButton;

    private int seconds;

    public void createMenu(WebPanel panel) {
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
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);
        }};
        panel.add(emailLabel);
        panel.add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new VerticalFlowLayout(0, 6));
            setMargin(0, 40, 0, 40);

            add(createLabel("Код (6 цифр)"));
            add(code = new BlurTextField(R_5_CodeConfirm.this){{
                addTextListener(text -> nextButton.setEnabled(code.getText().length() == 6));
            }});
            add(resendButton = createButton(1, "Повторить", () -> {
                seconds = Integer.parseInt(getLauncher().getConfig().get("emailCodeTimeout", "60"));
                NetManager.sendEmailCode(getParameters()[2]);
            }));
        }});
    }

    public void createComponents(WebPanel panel) {
        nextButton = createButton(2, "Далее", () -> {
            getLauncherUI().setScreen("checkingEmailCode", getParameters(code.getText()));
        });
        nextButton.setEnabled(false);
        panel.add(createButton(2, "Назад", () -> {
            getLauncherUI().setScreen("registration_1", getParameters()[0], getParameters()[1]);
        }));
        panel.add(nextButton);

    }

    public void createSubComponents(WebPanel panel) {
    }


    public void onShow() {
        super.onShow();
        seconds = Integer.parseInt(getLauncher().getConfig().get("emailCodeTimeout", "60"));
        resendButton.setEnabled(false);
        emailLabel.setText("Код подтверждения был отправлен на {" + getParameters()[2] + " :c(" + GlassUI.Colors.labelText.getRed() + "," + GlassUI.Colors.labelText.getGreen() + "," + GlassUI.Colors.labelText.getBlue() + ")}");
    }
}