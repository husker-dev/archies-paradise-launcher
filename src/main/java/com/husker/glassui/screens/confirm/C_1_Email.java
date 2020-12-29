package com.husker.glassui.screens.confirm;

import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.utils.FormatUtils;
import com.husker.glassui.screens.TitledLogoScreen;

public class C_1_Email extends TitledLogoScreen {

    private BlurTextField email;
    private BlurButton nextButton;

    public void createMenu(TransparentPanel panel) {
        setTitle("Регистрация");

        panel.add(createLabel("Электронная почта"));
        panel.add(email = new BlurTextField(this){{
            addTextListener(text -> updateNextButton());
            addFastAction(() -> event());
        }});
    }

    public void updateNextButton(){
        new Thread(() -> {
            nextButton.setEnabled(FormatUtils.isCorrectEmail(email.getText()));
        }).start();
    }

    public void createComponents(TransparentPanel panel) {
        nextButton = createButton("Далее", () -> event());
        nextButton.setEnabled(false);
        panel.add(createButton("Назад", () -> getLauncher().User.logout()));
        panel.add(nextButton);
    }

    public void event(){
        getLauncherUI().setScreen(C_2_SendingCode.class, new Parameters(){{
            put("login", getParameter("login"));
            put("password", getParameter("password"));
            put("email", email.getText());
        }});
    }

    public void onShow(){
        super.onShow();
        email.setText(getParameterValue("email", ""));
    }

    public void createSubComponents(TransparentPanel panel) {

    }
}
