package com.husker.glassui.screens.reset;

import com.alee.extended.label.WebStyledLabel;
import com.husker.glassui.GlassUI;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPasswordField;
import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.TitledLogoScreen;
import com.husker.launcher.Resources;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.components.skin.SkinViewer;
import com.husker.launcher.utils.FormatUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class R_3_NewPassword extends TitledLogoScreen {

    private SkinViewer skinViewer;
    private BlurPasswordField password;
    private BlurButton nextBtn, backBtn;

    public void createMenu(TransparentPanel panel) {
        panel.add(new TransparentPanel(){{
            setLayout(new BorderLayout());
            add(skinViewer = new SkinViewer(){{
                setPreferredSize(new Dimension(90, 150));
            }}, BorderLayout.WEST);
            add(new WebStyledLabel(WebStyledLabel.CENTER){{
                setMaximumRows(7);
                setForeground(GlassUI.Colors.labelLightText);
                setPreferredHeight(80);
                setFont(Resources.Fonts.getChronicaProExtraBold());
                setText("Введите новый пароль, что бы продожить...");
            }});
        }});

        panel.add(createLabel("Новый пароль"));
        panel.add(password = new BlurPasswordField(this){{
            addTextListener(text -> onPasswordChanged());
            addFastAction(() -> event());
        }});
    }

    public void createComponents(TransparentPanel panel) {
        panel.add(backBtn = createButton("Отменить", () -> getLauncherUI().setScreen(getParameterValue("prevScreen"))));
        panel.add(nextBtn = createButton("Изменить", this::event));
    }

    public void event(){
        new Thread(() -> {
            backBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            password.setEnabled(false);

            try{
                API.getJSON(ApiMethod.create("profiles.changePassword").set("id", getParameterValue("id")).set("hash", getParameterValue("hash")).set("password", password.getText()));
                Message.showMessage(getLauncherUI(), "Успех", "Пароль был изменён!", getParameterValue("finalScreen"));
            }catch (Exception ex){
                Message.showMessage(getLauncherUI(), "Проблемка", "Не удалось изменить пароль (" + ex.getLocalizedMessage() + ")", R_3_NewPassword.class, getParameters());
            }

            backBtn.setEnabled(true);
            nextBtn.setEnabled(true);
            password.setEnabled(true);
        }).start();
    }

    public void onPasswordChanged(){
        nextBtn.setEnabled(FormatUtils.isCorrectPassword(password.getText()));
    }

    public void createSubComponents(TransparentPanel panel) {

    }

    public void onShow() {
        int id = Integer.parseInt(getParameterValue("id"));
        String name = getParameterValue("name");

        setTitle(name);
        try {
            BufferedImage skin = API.getImage(ApiMethod.create("skins.getSkin").set("id", id));
            skinViewer.setPlayerTexture(skin);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
