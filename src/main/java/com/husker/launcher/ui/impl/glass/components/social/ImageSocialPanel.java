package com.husker.launcher.ui.impl.glass.components.social;

import com.alee.laf.panel.WebPanel;
import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.components.BlurScalableImage;
import com.husker.launcher.utils.ComponentUtils;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.husker.launcher.utils.ShapeUtils.Corner.TOP_LEFT;
import static com.husker.launcher.utils.ShapeUtils.Corner.TOP_RIGHT;

public abstract class ImageSocialPanel extends SocialPanel{

    private BlurScalableImage image;

    public ImageSocialPanel(Screen screen) {
        super(screen);
    }

    public void onContentInit(WebPanel panel) {
        image = new BlurScalableImage(getScreen()){
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                ImageSocialPanel.this.onBlurApply(parameter, image);
            }
        };
        image.setFitType(ScalableImage.FitType.FIT_XY);
        image.setAnimated(true);
        ComponentUtils.makeMouseEventTransparent(image);
        panel.setMargin(0, 0, 25, 0);
        panel.add(image);
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        super.onBlurApply(parameter, component);
        if(returnOnInvisible(parameter, component))
            return;

        if(component == image){
            Rectangle bounds = parameter.getShape().getBounds();
            parameter.setShape(ShapeUtils.createRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15, TOP_LEFT, TOP_RIGHT));

            parameter.setShadowSize(0);
        }
    }

    public void setImage(BufferedImage image){
        this.image.setImage(image);
    }

    public BlurScalableImage getBlurScalableImage(){
        return image;
    }
}
