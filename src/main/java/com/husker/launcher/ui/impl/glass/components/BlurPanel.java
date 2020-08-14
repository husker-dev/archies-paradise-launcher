package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BlurPanel extends WebPanel implements BlurComponent{

    private final boolean isMainColor;
    private final Screen screen;

    public BlurPanel(Screen screen){
        this(screen, false);
    }

    public BlurPanel(Screen screen, boolean isMain){
        super(StyleId.panelTransparent);
        this.isMainColor = isMain;
        this.screen = screen;

        screen.addBlurSegment(parameter -> onBlurApply(parameter, this));
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        if(isMainColor)
            GlassUI.applyTopLayer(parameter);
        else
            GlassUI.applyBottomLayer(parameter);

        Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

        parameter.setVisible(isVisible() && isDisplayable());
        parameter.setShape(new RoundRectangle2D.Double(location.x, location.y, getWidth(), getHeight(), 25, 25));
    }

    public Screen getScreen() {
        return screen;
    }
}
