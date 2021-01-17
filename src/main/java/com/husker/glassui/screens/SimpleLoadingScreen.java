package com.husker.glassui.screens;

import com.husker.launcher.ui.CenteredMenuScreen;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.geom.RoundRectangle2D;

import static com.husker.launcher.ui.utils.UIUtils.ShadowSide.BOTTOM;

public abstract class SimpleLoadingScreen extends CenteredMenuScreen {
    private final int title_size = 50;
    private MLabel title;
    private MLabel text;

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

        addToMenu(title = GlassUI.createTitleLabel("Заголовок"));
        title.setMargin(6, 0, 0, 0);
        addIndent(30);
        addToMenu(text = GlassUI.createSimpleLabel("Загрузка...", true));
        text.setMargin(3, 0, 0, 0);
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

    public void onShow(){
        super.onShow();
        new Thread(this::process).start();
    }

    public abstract void process();
}
