package com.husker.launcher.ui.impl.glass.components;

import com.husker.launcher.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.ShapeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.utils.ShapeUtils.ALL_CORNERS;


public class BlurScalableImage extends ScalableImage implements BlurComponent {

    private int oldWidth = -1;
    private int oldHeight = -1;
    private BufferedImage img;
    private final Screen screen;
    private boolean disposed = false;
    private float alpha = 0;
    private boolean isAnimated = false;

    public BlurScalableImage(Screen screen, BufferedImage image){
        super(image);
        this.screen = screen;
        setFitType(FitType.FIT_Y);
        screen.addBlurSegment("ScalableImage", parameter -> onBlurApply(parameter, this));

        new Timer().schedule(new TimerTask() {
            public void run() {
                if(!isDisplayable())
                    return;
                alpha += 8;
                if(alpha >= 255 || !isAnimated) {
                    alpha = 255;
                    screen.getLauncher().repaint();
                    cancel();
                }
                screen.getLauncher().repaint();
            }
        }, 0, 10);
    }

    public BlurScalableImage(Screen screen){
        this(screen, null);
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
        checkForDispose(parameter);
        if(returnOnInvisible(parameter, component))
            return;

        parameter.setBlurFactor(0);
        parameter.setShadowSize(5);

        parameter.setTextureAlpha(alpha / 255f);

        if(img == null)
            repaint();

        parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), this, 15, 15, ALL_CORNERS));
        parameter.setUseTexture(true);
        parameter.setTexture(img);
    }

    public Screen getScreen() {
        return null;
    }

    public void dispose() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }
}
