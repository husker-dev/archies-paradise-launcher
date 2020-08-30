package com.husker.launcher.ui.impl.glass;

import com.alee.laf.label.WebLabel;
import com.husker.launcher.LauncherWindow;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.screens.BackgroundSelection;
import com.husker.launcher.ui.impl.glass.screens.main.MainScreen;
import com.husker.launcher.ui.impl.glass.screens.login.AuthProcess;
import com.husker.launcher.ui.impl.glass.screens.login.Login;
import com.husker.launcher.ui.impl.glass.screens.main.profile.InfoApplying;
import com.husker.launcher.ui.impl.glass.screens.main.profile.InfoEdit;
import com.husker.launcher.ui.impl.glass.screens.registration.*;

import java.awt.*;

public class GlassUI extends LauncherUI {

    public static class Colors{
        public static final Color first = new Color(255, 255, 255, 200);
        public static final Color second = new Color(255, 255, 255, 140);
        public static final Color third = new Color(255, 255, 255, 225);

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
        addScreen("backgroundSelection", new BackgroundSelection());
        addScreen("info_edit", new InfoEdit());
        addScreen("info_edit_apply", new InfoApplying());

        boolean hasAccount = false;
        if(getLauncher().getSettings().get("auto_auth", "false").equals("true")){
            String login = getLauncher().getUserConfig().get("login");
            String password = getLauncher().getUserConfig().get("password");

            if (login != null && !login.equals("null") && password != null && !password.equals("null")) {
                hasAccount = true;
                setScreen("authProcess", new Screen.Parameters(){{
                    put("login", login);
                    put("password", password);
                    put("encrypted", "true");
                }});
            }
        }
        if(!hasAccount)
            setScreen("login");
    }

    public Dimension getDefaultSize() {
        return new Dimension(1150, 750);
    }

    public static WebLabel createTitleLabel(String text){
        return new WebLabel(text){{
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(30f));
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

    public static void applyTopLayer(BlurParameter parameter){
        parameter.setAdditionColor(GlassUI.Colors.first);
        parameter.setBlurFactor(25);
        parameter.setShadowSize(10);
        parameter.setShadowColor(new Color(0, 0, 0, 50));
    }

    public void logout(){
        String login = getLauncher().NetManager.PlayerInfo.getNickname();
        getLauncher().NetManager.PlayerInfo.logout();
        getLauncher().getUserConfig().set("login", "null");
        getLauncher().getUserConfig().set("password", "null");
        setScreen("login", new Screen.Parameters("login", login));
    }
}
