package com.husker.glassui.components;

import com.husker.launcher.ui.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ShapeUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;


public class BlurScalableImage extends ScalableImage implements BlurComponent {

    private BufferedImage oldImg;
    private int oldWidth = -1;
    private int oldHeight = -1;
    private BufferedImage img;
    private final Screen screen;
    private boolean disposed = false;
    private float alpha = 0;
    private boolean isAnimated = false;

    public BlurScalableImage(Screen screen, BufferedImage image){
        this.screen = screen;
        setImage(image);
        setFitType(FitType.FILL_Y);
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
        if(oldWidth != getWidth() || oldHeight != getHeight() || oldImg != getImage()) {
            oldWidth = getWidth();
            oldHeight = getHeight();
            oldImg = getImage();

            img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics gr = img.createGraphics();
            super.paint(gr);
        }
    }

    public void setImage(BufferedImage image){
        super.setImage(image);
        screen.getLauncher().updateUI();
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);
        if (returnOnInvisible(parameter, component))
            return;

        parameter.setBlurFactor(0);
        parameter.setShadowSize(5);
        parameter.setShape(ShapeUtils.createRoundRectangle(screen.getLauncher(), this, 15, 15, ALL_CORNERS));
        parameter.setTextureAlpha(alpha / 255f);

        if (img == null)
            repaint();

        if (img != null) {
            parameter.setUseTexture(true);
            parameter.setTexture(img);
        }
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
