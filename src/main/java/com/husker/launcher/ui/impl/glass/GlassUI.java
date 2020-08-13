package com.husker.launcher.ui.impl.glass;

import com.alee.laf.label.WebLabel;
import com.husker.launcher.LauncherWindow;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.screens.MainScreen;
import com.husker.launcher.ui.impl.glass.screens.login.AuthProcess;
import com.husker.launcher.ui.impl.glass.screens.login.Login;
import com.husker.launcher.ui.impl.glass.screens.registration.*;

import java.awt.*;

public class GlassUI extends LauncherUI {

    public static class Colors{
        public static final Color first = new Color(255, 255, 255, 200);
        public static final Color second = new Color(255, 255, 255, 140);
        public static final Color third = new Color(255, 255, 255, 100);

        public static final Color textField = new Color(255, 255, 255, 110);
        public static final Color textFieldHovered = new Color(255, 255, 255, 90);
        public static final Color textFieldFocused = new Color(255, 255, 255, 40);

        public static final Color buttonDefault = new Color(255, 255, 255);
        public static final int buttonDefaultAlpha = 230;
        public static final int buttonHoveredAlpha = 255;
        public static final int buttonPressedAlpha = 180;

        public static final Color labelText = new Color(50, 50, 50);
        public static final Color labelLightText = new Color(80, 80, 80);
    }


    public GlassUI(LauncherWindow launcher) {
        super(launcher);
    }

    public void onInit() {
        setAnimated(true);

        // Message
        addScreen("message", new Message());

        // Login
        addScreen("login", new Login());
        addScreen("authProcess", new AuthProcess());

        // Registration
        addScreen("registration", new R_1_LoginNPassword());
        addScreen("nicknameCheck", new R_2_NameCheck());
        addScreen("registration_1", new R_3_Email());
        addScreen("sendEmail", new R_4_SendEmail());
        addScreen("emailConfirm", new R_5_CodeConfirm());
        addScreen("checkingEmailCode", new R_6_Final());

        // Main
        addScreen("main", new MainScreen());

        setScreen("main");
    }

    public Dimension getDefaultSize() {
        return new Dimension(1100, 650);
    }

    public static WebLabel createTitleLabel(String text){
        return new WebLabel(text){{
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(35f));
            setForeground(new Color(50, 50, 50));
            setHorizontalAlignment(CENTER);
        }};
    }

    public static WebLabel createSimpleLabel(String text){
        return createSimpleLabel(text, false);
    }

    public static WebLabel createSimpleLabel(String text, boolean centered){
        return new WebLabel(text){{
            setForeground(Colors.labelText);
            setPreferredHeight(16);
            setFont(Resources.Fonts.ChronicaPro_ExtraBold);
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

    public static void applyThirdLayer(BlurParameter parameter){
        parameter.setAdditionColor(Colors.third);
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
}
