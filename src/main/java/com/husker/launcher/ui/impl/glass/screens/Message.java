package com.husker.launcher.ui.impl.glass.screens;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.UIUtils;
import com.husker.launcher.ui.impl.glass.components.BlurButton;

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
        addIndent(30);
        addToMenu(message = GlassUI.createSimpleLabel("Сообщение", true));
        addIndent(30);
        addToMenu(new WebPanel(StyleId.panelTransparent){{
            setLayout(new VerticalFlowLayout());
            setMargin(0, 50, 0, 50);
            add(new BlurButton(Message.this, "Окей"){{
                addActionListener(e -> {
                    getLauncherUI().setScreen(prevScreen, prevParameters);
                });
            }});
        }});
    }

    public static void showMessage(LauncherUI ui, String title, String text, String prevScreen){
        showMessage(ui, title, text, prevScreen, new Parameters());
    }

    public static void showMessage(LauncherUI ui, String title, String text, String prevScreen, Parameters parameters){
        ui.setScreen("message", new Parameters(){{
            put("backScreen", prevScreen);
            put("title", title);
            put("text", text);
            put("backParameters", parameters);
        }});
    }

    public void onShow() {
        super.onShow();

        prevScreen = getParameterValue("backScreen");
        prevParameters = (Parameters)getParameter("backParameters");
        title.setText(getParameterValue("title"));
        message.setText(getParameterValue("text"));
    }

}
