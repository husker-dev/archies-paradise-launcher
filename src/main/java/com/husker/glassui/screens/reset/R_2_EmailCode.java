package com.husker.glassui.screens.reset;

import com.alee.extended.label.WebStyledLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurTextField;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.TitledLogoScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.components.TransparentPanel;
import org.json.JSONObject;

public class R_2_EmailCode extends TitledLogoScreen {

    private BlurTextField code;
    private BlurButton nextBtn, backBtn;

    public void createMenu(TransparentPanel panel) {
        setTitle("Подтверждение");
        panel.add(new WebStyledLabel(WebStyledLabel.CENTER){{
            setMaximumRows(7);
            setForeground(GlassUI.Colors.labelLightText);
            setPreferredHeight(80);
            setFont(Resources.Fonts.getChronicaProExtraBold());
            setText("Что бы сменить пароль, введите код, отправленный на электронную почту");
        }});
        panel.add(createLabel("Код подтверждения (6 цифр)"));
        panel.add(code = new BlurTextField(R_2_EmailCode.this){{
            addTextListener(text -> onCodeUpdate());
            addFastAction(() -> event());
        }});
    }

    public void createComponents(TransparentPanel panel) {
        panel.add(backBtn = createButton("Отменить", () -> getLauncherUI().setScreen(getParameterValue("prevScreen"), new Parameters())));
        panel.add(nextBtn = createButton("Далее", this::event));
    }

    public void createSubComponents(TransparentPanel panel) {

    }

    public void event(){
        code.setEnabled(false);
        nextBtn.setEnabled(false);
        backBtn.setEnabled(false);

        new Thread(() -> {
            try{
                JSONObject json = API.getJSON(ApiMethod.create("profiles.generateChangeHash").set("id", getParameterValue("id")).set("code", code.getText()));
                String hash = json.getString("hash");
                getLauncherUI().setScreen(R_3_NewPassword.class, new Parameters(){{
                    put("id", getParameterValue("id"));
                    put("name", getParameterValue("name"));
                    put("hash", hash);
                    put("finalScreen", getParameterValue("finalScreen"));
                    put("prevScreen", getParameterValue("prevScreen"));
                }});
            }catch (Exception ex){
                Message.showMessage(getLauncherUI(), "Проблемка", "Неправильный код подтверждения", R_2_EmailCode.class, getParameters());
            }

            code.setEnabled(true);
            nextBtn.setEnabled(true);
            backBtn.setEnabled(true);
        }).start();
    }

    public void onCodeUpdate(){
        nextBtn.setEnabled(code.getText().length() == 6);
    }

    public void onShow() {
        new Thread(() -> code.setText("")).start();
        onCodeUpdate();
    }
}
