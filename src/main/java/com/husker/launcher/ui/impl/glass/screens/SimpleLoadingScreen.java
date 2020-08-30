package com.husker.launcher.ui.impl.glass.screens;

import com.alee.laf.label.WebLabel;
import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.UIUtils;

import javax.swing.*;
import java.awt.geom.RoundRectangle2D;

import static com.husker.launcher.utils.UIUtils.ShadowSide.BOTTOM;

public abstract class SimpleLoadingScreen extends CenteredMenuScreen {
    private final int title_size = 50;
    private WebLabel title;
    private WebLabel text;

    public void onMenuInit() {
        addBlurSegment("LoadingScreen.Menu", parameter -> {
            GlassUI.applyBottomLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getMenuX(), getMenuY(), getMenuWidth(), getMenuHeight(), 25, 25));
        });

        addBlurSegment("LoadingScreen.Title", parameter -> {
            GlassUI.applyTopLayer(parameter);
            parameter.setShape(new RoundRectangle2D.Double(getMenuX(), getMenuY(), getMenuWidth(), title_size, 25, 25));
            parameter.setShadowClip(UIUtils.keepShadow(parameter, BOTTOM));
        });

        addToMenu(title = GlassUI.createTitleLabel("Вход"));
        addIndent(30);
        addToMenu(text = GlassUI.createSimpleLabel("Проверка соединения...", true));
        addIndent(30);

        onContentInit();
    }

    public abstract void onContentInit();

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void setText(String text){
        SwingUtilities.invokeLater(() -> this.text.setText(text));
    }
}
