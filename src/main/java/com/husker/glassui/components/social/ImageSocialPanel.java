package com.husker.glassui.components.social;

import com.husker.glassui.components.BlurScalableImage;
import com.husker.launcher.ui.components.ScalableImage;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.ShapeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.husker.launcher.ui.utils.ShapeUtils.Corner.TOP_LEFT;
import static com.husker.launcher.ui.utils.ShapeUtils.Corner.TOP_RIGHT;

public abstract class ImageSocialPanel extends SocialPanel{

    private BlurScalableImage image;

    public ImageSocialPanel(Screen screen) {
        super(screen);
    }

    public void onContentInit(TransparentPanel panel) {
        image = new BlurScalableImage(getScreen()){
            public void onBlurApply(BlurParameter parameter, Component component) {
                super.onBlurApply(parameter, component);
                ImageSocialPanel.this.onBlurApply(parameter, image);
            }
        };
        image.setAnimated(true);
        image.setFitType(ScalableImage.FitType.FILL_XY);
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

            if(isInner()) {
                parameter.setShadowType(BlurParameter.ShadowType.INNER);
                parameter.setShadowSize(5);
            }
        }
    }

    public void setImage(BufferedImage image){
        this.image.setImage(image);
    }

    public BlurScalableImage getBlurScalableImage(){
        return image;
    }
}
