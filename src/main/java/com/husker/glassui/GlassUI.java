package com.husker.glassui;

import com.husker.glassui.components.BlurComponent;
import com.husker.glassui.screens.confirm.C_1_Email;
import com.husker.glassui.screens.confirm.C_2_SendingCode;
import com.husker.glassui.screens.confirm.C_3_Code;
import com.husker.glassui.screens.confirm.C_4_ConfirmingCode;
import com.husker.glassui.screens.main.control.ClientLoading;
import com.husker.glassui.screens.main.play.ClientSettings;
import com.husker.glassui.screens.main.play.RemovingProcess;
import com.husker.glassui.screens.main.profile.skin.*;
import com.husker.glassui.screens.main.settings.*;
import com.husker.glassui.screens.*;
import com.husker.glassui.screens.login.*;
import com.husker.glassui.screens.main.*;
import com.husker.glassui.screens.main.profile.edit.*;
import com.husker.glassui.screens.registration.*;
import com.husker.glassui.screens.reset.R_1_SendingCode;
import com.husker.glassui.screens.reset.R_2_EmailCode;
import com.husker.glassui.screens.reset.Reset;
import com.husker.glassui.screens.reset.R_3_NewPassword;
import com.husker.launcher.Launcher;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.api.API;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.screens.main.profile.edit.InfoApplying;
import com.husker.glassui.screens.main.profile.skin.SkinCategoriesLoading;
import com.husker.launcher.ui.utils.ShapeUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;

public class GlassUI extends LauncherUI {

    public static class Colors{
        public static final Color first = new Color(255, 255, 255, 200);
        public static final Color second = new Color(255, 255, 255, 140);
        public static final Color third = new Color(255, 255, 255, 225);

        public static final Color separator = new Color(180, 180, 180);

        public static final Color textField = new Color(255, 255, 255, 110);
        public static final Color textFieldHovered = new Color(255, 255, 255, 100);
        public static final Color textFieldFocused = new Color(255, 255, 255, 80);
        public static final Color textFieldText = new Color(30, 30, 30);

        public static final Color buttonDefault = new Color(255, 255, 255);
        public static final int buttonDefaultAlpha = 240;
        public static final int buttonHoveredAlpha = 255;
        public static final int buttonPressedAlpha = 180;

        public static final Color labelText = new Color(50, 50, 50);
        public static final Color labelLightText = new Color(80, 80, 80);

        public static final Color selectedColor = new Color(154, 178, 206);
    }


    public GlassUI(Launcher launcher) {
        super(launcher);
    }

    public void onInit() {
        setAnimated(true);

        // Message
        addScreen(Message.class);

        // Login
        addScreen(Login.class, LoginProcess.class);

        // Password reset
        addScreen(Reset.class, R_1_SendingCode.class, R_2_EmailCode.class, R_3_NewPassword.class);

        // Registration
        addScreen(Registration.class, RegistrationProgress.class);

        // Confirmation
        addScreen(C_1_Email.class, C_2_SendingCode.class, C_3_Code.class, C_4_ConfirmingCode.class);

        // Main
        addScreen(MainScreen.class);
        addScreen(BackgroundSelection.class);
        addScreen(ClientSettings.class, RemovingProcess.class);
        addScreen(ClientLoading.class);
        addScreen(InfoEdit.class, InfoApplying.class, EmailConfirm.class, SendingCode.class);

        // Skins
        addScreen(SkinCategoriesLoading.class, SkinCategories.class, SkinListLoading.class, SkinList.class, SkinApply.class);
        addScreen(CapeListLoading.class, CapeList.class, CapeApply.class);

        getLauncher().User.addLogoutListener(() -> {
            setScreen(Login.class, new Screen.Parameters("login", getLauncher().User.getNickname()));
        });

        if(LauncherSettings.isAutoAuth() && getLauncher().User.containSaved()){
            setScreen(LoginProcess.class, new Screen.Parameters(){{
                put(API.LOGIN, getLauncher().User.File.getLogin());
                put(API.PASSWORD, getLauncher().User.File.getPassword());
            }});
        }else
            setScreen(Login.class);
    }

    public Dimension getDefaultSize() {
        return new Dimension(1150, 750);
    }

    public static MLabel createTitleLabel(String text){
        return new MLabel(text){{
            setFont(Resources.Fonts.getChronicaProExtraBold(26));
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(CENTER);
        }};
    }

    public static MLabel createSimpleLabel(String text){
        return createSimpleLabel(text, false);
    }

    public static MLabel createSimpleLabel(String text, boolean centered){
        return new MLabel(text){{
            setForeground(Colors.labelText);
            setPreferredHeight(16);
            setFont(Resources.Fonts.getChronicaProExtraBold());
            // todo Remove if troubles \/
            setVerticalAlignment(CENTER);
            if(centered)
                setHorizontalAlignment(CENTER);
        }};
    }

    public static void applyBottomLayer(BlurParameter parameter){
        parameter.setAdditionColor(GlassUI.Colors.second);
        parameter.setUseTexture(false);
        parameter.setBlurFactor(25);
        parameter.setShadowSize(10);
        parameter.setShadowColor(new Color(0, 0, 0, 40));
    }

    public static void applyTopLayer(BlurParameter parameter){
        parameter.setAdditionColor(GlassUI.Colors.first);
        parameter.setBlurFactor(25);
        parameter.setShadowSize(10);
        parameter.setShadowColor(new Color(0, 0, 0, 50));
    }

    public static void applyTag(BlurParameter parameter){
        parameter.setBlurFactor(25);
        parameter.setShadowColor(new Color(0, 0, 0, 50));
        parameter.setAdditionColor(GlassUI.Colors.third);

        if(parameter.getShape() != null) {
            Rectangle2D bounds = parameter.getShape().getBounds();
            parameter.setShape(ShapeUtils.createRoundRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 10, 10, ALL_CORNERS));
        }
        parameter.setShadowSize(4);
    }

    public static void applyTransparentButton(BlurParameter parameter, boolean isHovered){
        parameter.setAdditionColor(new Color(0, 0, 0, 0));
        parameter.setBlurFactor(0);
        parameter.setShadowSize(2);

        if(isHovered)
            parameter.setShadowColor(new Color(0, 0, 0, 90));
    }

    public static MLabel createTagLabel(Screen screen, String text){
        return new MLabel(text){{
            setForeground(Colors.labelText);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setPreferredHeight(30);
            setFont(Resources.Fonts.getChronicaProExtraBold(23));
            setMargin(1, 10, 0, 10);

            screen.addBlurSegment("ProfilePanel.Label", parameter -> {
                if(BlurComponent.isReturnOnInvisible(parameter, this))
                    return;

                parameter.setShape(ShapeUtils.createRoundRectangle(this, 10, 10, ALL_CORNERS));
                GlassUI.applyTag(parameter);
            });
        }};
    }

    public static TransparentPanel createParameterLine(String name, Component component){
        return new TransparentPanel(){
            {
                setMargin(10, 20, 0, 10);
                setLayout(new BorderLayout());
                add(new MLabel(name + ":"){{
                    setForeground(GlassUI.Colors.labelText);
                    setFont(Resources.Fonts.getChronicaProExtraBold(18));
                    setMargin(0, 0, 0, 10);
                }}, BorderLayout.WEST);
                add(component, BorderLayout.EAST);
            }

            public void paint(Graphics gr) {
                super.paint(gr);

                gr.setColor(GlassUI.Colors.separator);
                gr.drawLine(10, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
    }

    public static MLabel createParameterLineValueLabel(){
        return createParameterLineValueLabel(false);
    }

    public static MLabel createParameterLineValueLabel(boolean main){
        float minFontSize = 14;
        float maxFontSize = main ? 18 : 16;

        return new MLabel(){
            {
                setForeground(GlassUI.Colors.labelLightText);
                setFont(Resources.Fonts.getChronicaProExtraBold(maxFontSize));
                setHorizontalAlignment(RIGHT);
            }
            public void setText(String text) {
                super.setText(text);
                if(text == null)
                    return;

                for(int i = (int)maxFontSize; i >= minFontSize; i--){
                    setFont(Resources.Fonts.getChronicaProExtraBold(i));
                    if(getFontMetrics(Resources.Fonts.getChronicaProExtraBold(i)).stringWidth(text) < 160)
                        break;
                }
            }
        };
    }
}
