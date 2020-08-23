package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.*;

public class BlurPanel extends WebPanel implements BlurComponent{

    private final boolean isMainColor;
    private final Screen screen;
    private boolean disposed = false;

    public BlurPanel(Screen screen){
        this(screen, false);
    }

    public BlurPanel(Screen screen, boolean isMain){
        super(StyleId.panelTransparent);
        this.isMainColor = isMain;
        this.screen = screen;

        screen.addBlurSegment("Panel", parameter -> onBlurApply(parameter, this));
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);
        if(returnOnInvisible(parameter, component))
            return;

        if(component == this){
            if(isMainColor)
                GlassUI.applyTopLayer(parameter);
            else
                GlassUI.applyBottomLayer(parameter);

            parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), this, 25, 25, ALL_CORNERS));
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void dispose() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }
}
