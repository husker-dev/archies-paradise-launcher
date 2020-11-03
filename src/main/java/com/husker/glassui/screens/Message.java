package com.husker.glassui.screens;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.components.BlurButton;
import com.husker.glassui.components.BlurPanel;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.launcher.ui.LauncherUI;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.UIUtils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static com.husker.launcher.utils.UIUtils.ShadowSide.BOTTOM;

public class Message extends CenteredMenuScreen {

    private final int title_size = 50;

    private WebLabel title;
    private WebLabel message;
    private String prevScreen = "";
    private Parameters prevParameters;

    public void onMenuInit() {

        addBlurSegment("Message.Menu", parameter -> {
            GlassUI.applyBottomLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getMenuX(), getMenuY(), getMenuWidth(), getMenuHeight() - 15, 25, 25));
        });

        addBlurSegment("Message.Title", parameter -> {
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getMenuX(), getMenuY(), getMenuWidth(), title_size, 25, 25));
            parameter.setShadowClip(UIUtils.keepShadow(parameter, BOTTOM));
        });


        setLayout(new GridBagLayout());

        addToMenu(title = GlassUI.createTitleLabel("Заголовок"));
        title.setMargin(6, 0, 0, 0);
        addIndent(36);
        addToMenu(message = GlassUI.createSimpleLabel("Сообщение", true));
        addIndent(30);
        addToMenu(new TransparentPanel(){{
            setLayout(new VerticalFlowLayout());
            setMargin(0, 50, 0, 50);
            add(new BlurButton(Message.this, "Окей"){{
                addActionListener(e -> {
                    getLauncherUI().setScreen(prevScreen, prevParameters);
                });
            }});
        }});
    }

    public static void showMessage(LauncherUI ui, String title, String text, Class<? extends Screen> prevScreen){
        showMessage(ui, title, text, ui.getScreen(prevScreen));
    }

    public static void showMessage(LauncherUI ui, String title, String text, Class<? extends Screen> prevScreen, Parameters parameters){
        showMessage(ui, title, text, ui.getScreen(prevScreen), parameters);
    }

    public static void showMessage(LauncherUI ui, String title, String text, String prevScreen){
        showMessage(ui, title, text, prevScreen, new Parameters());
    }

    public static void showMessage(LauncherUI ui, String title, String text, String prevScreen, Parameters parameters){
        ui.setScreen(Message.class, new Parameters(){{
            put("title", title);
            put("text", text);
            put("prevScreen", prevScreen);
            put("backParameters", parameters);
        }});
    }

    public void onShow() {
        super.onShow();

        prevScreen = getParameterValue("prevScreen");
        prevParameters = (Parameters)getParameter("backParameters");
        title.setText(getParameterValue("title"));
        message.setText(getParameterValue("text"));
    }

}
