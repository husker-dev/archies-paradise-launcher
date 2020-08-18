package com.husker.launcher.ui.impl.glass.components;

import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;


public class BlurScalableImage extends ScalableImage implements BlurComponent {

    private int oldWidth = -1;
    private int oldHeight = -1;
    private BufferedImage img;
    private final Screen screen;

    public BlurScalableImage(Screen screen, BufferedImage image){
        super(image);
        this.screen = screen;
        setFitType(FitType.FIT_Y);
        screen.addBlurSegment(parameter -> onBlurApply(parameter, this));
    }

    public BlurScalableImage(Screen screen){
        this.screen = screen;
        setFitType(FitType.FIT_Y);
        screen.addBlurSegment(parameter -> onBlurApply(parameter, this));
    }

    public void paint(Graphics g) {
        if(getWidth() != oldWidth || getHeight() != oldHeight) {
            oldWidth = getWidth();
            oldHeight = getHeight();

            img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics gr = img.createGraphics();
            super.paint(gr);
        }
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        parameter.setBlurFactor(0);
        parameter.setShadowSize(5);

        if(img == null)
            repaint();

        if(isDisplayable() && isVisible()){
            parameter.setDebugName("ScalableImage." + getName());
            parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), this, 15, 15, ALL_CORNERS));
            parameter.setTextureAlpha(1);
            parameter.setUseTexture(true);
            parameter.setTexture(img);
        }

        parameter.setVisible(isDisplayable() && isVisible());
    }

    public Screen getScreen() {
        return null;
    }
}
