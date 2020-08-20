package com.husker.launcher.ui.impl.glass.components.social;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;

public class BlankSocialPanel extends ImageSocialPanel{

    private boolean hidden = false;

    private WebLabel emptyTitle;
    private WebLabel emptyTitleIcon;

    public BlankSocialPanel(Screen screen) {
        super(screen);
        setImage(screen.getLauncher().Resources.Icon_Image);
        setSelectable(false);

        add(new WebPanel(StyleId.panelTransparent){{
            setLayout(new BorderLayout());
            add(new WebPanel(StyleId.panelTransparent){{
                setLayout(new BorderLayout());
                add(emptyTitleIcon = new WebLabel(){{
                    setPreferredHeight(35);
                    setPreferredWidth(25);
                }}, BorderLayout.WEST);
                add(emptyTitle = new WebLabel(){{
                    setPreferredHeight(35);
                }});
            }}, BorderLayout.SOUTH);

        }});

        screen.addBlurSegment(parameter -> onBlurApply(parameter, emptyTitle));
        screen.addBlurSegment(parameter -> onBlurApply(parameter, emptyTitleIcon));
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);

        if(component == getBlurScalableImage())
            parameter.setTextureAlpha(0.1f);

        if(component != null && component == emptyTitle){
            parameter.setAdditionColor(new Color(0, 0, 0, 30));
            parameter.setBlurFactor(0);

            if(component.isDisplayable()) {
                Point location = ComponentUtils.getComponentLocationOnScreen(getScreen().getLauncher(), component);
                int size = 20;
                parameter.setShape(ShapeUtils.createRoundRectangle(location.x + 10, location.y + (35d - size) / 2d, component.getWidth() - 20, size, 5, 5, ALL_CORNERS));
            }
        }

        if(component != null && component == emptyTitleIcon){
            parameter.setAdditionColor(new Color(0, 0, 0, 30));
            parameter.setBlurFactor(0);

            if(component.isDisplayable()) {
                Point location = ComponentUtils.getComponentLocationOnScreen(getScreen().getLauncher(), component);
                int size = 21;
                parameter.setShape(ShapeUtils.createRoundRectangle(location.x + 7, location.y + (35d - size) / 2d, size, size, 5, 5, ALL_CORNERS));
            }
        }

        if(hidden)
            parameter.setVisible(false);
    }

    public void hidePanel(){
        hidden = true;
        setIcon(null);
        setImage(null);
        setTitle("");
        dispose();
    }
}
