package com.husker.glassui.components;

import com.husker.glassui.screens.main.play.ScreenshotsPanel;
import com.husker.launcher.ui.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ShapeUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;


public class BlurScalableImage extends ScalableImage implements BlurComponent {

    private static final Logger log = LogManager.getLogger(BlurScalableImage.class);
    private static final int speed = 15;

    private enum AnimState{
        NONE,
        HIDING,
        SHOWING
    }

    private BufferedImage newImg;
    private int oldWidth = -1;
    private int oldHeight = -1;
    private boolean disposed = false;
    private float alpha = 255;
    private boolean isAnimated = false;

    private BufferedImage showingImage;

    private AnimState animState = AnimState.NONE;

    private final Screen screen;

    public BlurScalableImage(Screen screen, BufferedImage image){
        this.screen = screen;
        setImage(image);
        setFitType(FitType.FILL_Y);
        screen.addBlurSegment("ScalableImage", parameter -> onBlurApply(parameter, this));

        new Timer().schedule(new TimerTask() {
            public void run() {
                if(!isDisplayable())
                    return;
                if(getWidth() <= 0 || getHeight() <= 0)
                    return;

                if(animState == AnimState.HIDING){
                    alpha -= speed;
                    if(alpha <= 0){
                        alpha = 0;
                        if(newImg != null) {
                            BlurScalableImage.super.setImage(newImg);
                            updateImage();
                            newImg = null;
                        }
                        animState = AnimState.SHOWING;
                    }
                    screen.getLauncher().updateUI();
                }
                if(animState == AnimState.SHOWING){
                    alpha += speed;
                    if(alpha >= 255) {
                        alpha = 255;
                        animState = AnimState.NONE;
                    }
                    screen.getLauncher().updateUI();
                }
            }
        }, 0, 10);
    }

    public BlurScalableImage(Screen screen){
        this(screen, null);
    }

    public void paint(Graphics g) {
        if(oldWidth != getWidth() || oldHeight != getHeight()) {
            oldWidth = getWidth();
            oldHeight = getHeight();

            updateImage();
        }
    }

    private void updateImage(){
        if(getWidth() <= 0 || getHeight() <= 0)
            return;
        showingImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        super.paint(showingImage.createGraphics());
    }

    public void setImage(BufferedImage image){
        newImg = image;
        if(isAnimated) {
            if(super.getImage() == null)
                alpha = 0;
            animState = AnimState.HIDING;
        }else if(animState != AnimState.HIDING) {
            SwingUtilities.invokeLater(() -> {
                super.setImage(newImg);

                updateImage();
                newImg = null;
                screen.getLauncher().updateUI();
            });
        }
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);
        if (returnOnInvisible(parameter, component))
            return;

        parameter.setBlurFactor(0);
        parameter.setShadowSize(5);
        parameter.setShape(ShapeUtils.createRoundRectangle(this, 15, 15, ALL_CORNERS));
        parameter.setTextureAlpha(alpha / 255f);

        if(showingImage == null)
            updateImage();

        if (showingImage != null) {
            parameter.setUseTexture(true);
            parameter.setTexture(showingImage);
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
