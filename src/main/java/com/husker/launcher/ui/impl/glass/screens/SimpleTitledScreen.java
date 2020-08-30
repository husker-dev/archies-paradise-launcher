package com.husker.launcher.ui.impl.glass.screens;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.ui.impl.glass.components.BlurButton;
import com.husker.launcher.ui.impl.glass.components.BlurPanel;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;
import static java.awt.FlowLayout.CENTER;

public abstract class SimpleTitledScreen extends Screen {

    private final String title;

    public SimpleTitledScreen(String title){
        this.title = title;
    }

    public void onInit() {
        setLayout(new GridBagLayout());

        add(new BlurPanel(this, true){
            {
                setLayout(new BorderLayout(0, 0));
                add(GlassUI.createTitleLabel(title), BorderLayout.NORTH);

                add(new WebPanel(StyleId.panelTransparent){{
                    onMenuInit(this);
                }});

                add(new WebPanel(StyleId.panelTransparent){{
                    setLayout(new FlowLayout(CENTER, 10, 0));
                    setMargin(20, 0, 0, 0);
                    onButtonsInit(this);

                }}, BorderLayout.SOUTH);
            }

            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                if(returnOnInvisible(parameter, component))
                    return;
                if(component == this){
                    Rectangle bounds = parameter.getShape().getBounds();
                    bounds.height -= 15;

                    parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 25, 25, ALL_CORNERS));
                }
            }
        });
    }

    public abstract void onMenuInit(WebPanel panel);
    public abstract void onButtonsInit(WebPanel panel);
}
