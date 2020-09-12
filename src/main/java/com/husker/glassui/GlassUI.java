package com.husker.glassui;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.screens.main.profile.skin.*;
import com.husker.glassui.screens.main.settings.*;
import com.husker.glassui.screens.*;
import com.husker.glassui.screens.login.*;
import com.husker.glassui.screens.main.*;
import com.husker.glassui.screens.main.profile.edit.*;
import com.husker.glassui.screens.registration.*;
import com.husker.launcher.Launcher;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.screens.main.profile.edit.InfoApplying;
import com.husker.glassui.screens.main.profile.skin.SkinFoldersLoading;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

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
    }


    public GlassUI(Launcher launcher) {
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

        addScreen("skin_folders_loading", new SkinFoldersLoading());
        addScreen("skin_folders", new SkinFolders());
        addScreen("skin_list_loading", new SkinListLoading());
        addScreen("skin_list", new SkinList());
        addScreen("skin_apply", new SkinApply());

        addScreen("info_edit", new InfoEdit());
        addScreen("info_edit_apply", new InfoApplying());
        addScreen("info_email_confirm", new EmailConfirm());

        if(getLauncher().getSettings().isAutoAuth() && getLauncher().getUserConfig().hasAccount()){
            setScreen("authProcess", new Screen.Parameters(){{
                put(NetManager.LOGIN, getLauncher().getUserConfig().getLogin());
                put(NetManager.PASSWORD, getLauncher().getUserConfig().getPassword());
                put(NetManager.ENCRYPTED, "true");
            }});
        }else
            setScreen("login");
    }

    public Dimension getDefaultSize() {
        return new Dimension(1150, 750);
    }

    public static WebLabel createTitleLabel(String text){
        return new WebLabel(text){{
            setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(26f));
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

    public static void applyTag(BlurParameter parameter){
        parameter.setBlurFactor(25);
        parameter.setShadowColor(new Color(0, 0, 0, 50));
        parameter.setAdditionColor(GlassUI.Colors.third);

        Rectangle2D bounds = parameter.getShape().getBounds();
        parameter.setShape(ShapeUtils.createRoundRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 10, 10, ALL_CORNERS));
        parameter.setShadowSize(4);
    }

    public void logout(){
        String login = getLauncher().NetManager.PlayerInfo.getNickname();
        getLauncher().NetManager.PlayerInfo.logout();
        getLauncher().getUserConfig().reset();
        setScreen("login", new Screen.Parameters("login", login));
    }
}
