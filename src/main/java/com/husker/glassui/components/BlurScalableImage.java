package com.husker.glassui.components;

import com.husker.launcher.Launcher;
import com.husker.launcher.ui.AnimationTimer;
import com.husker.launcher.ui.components.ScalableImage;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ShapeUtils;
import com.husker.launcher.utils.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Predicate;

import static com.husker.launcher.ui.utils.ShapeUtils.ALL_CORNERS;


public class BlurScalableImage extends ScalableImage implements BlurComponent {

    private static final Logger log = LogManager.getLogger(BlurScalableImage.class);
    private static final double speed = 600;

    private static final ArrayList<Predicate<Double>> painters = new ArrayList<>();
    private static Launcher launcher;


    static{
        new AnimationTimer(delta -> {
            boolean repaint = false;
            for(Predicate<Double> painter : painters)
                if(painter.test(delta))
                    repaint = true;
            if(repaint && launcher != null)
                launcher.updateUI();
        });
    }

    private enum AnimState{
        NONE,
        HIDING,
        SHOWING
    }

    private BufferedImage newImg;
    private int oldWidth = -1;
    private int oldHeight = -1;
    private boolean disposed = false;
    private double alpha = 255;
    private boolean isAnimated = false;

    private BufferedImage showingImage;

    private AnimState animState = AnimState.NONE;

    private final Screen screen;

    public BlurScalableImage(Screen screen, BufferedImage image){
        this.screen = screen;
        launcher = screen.getLauncher();
        setImage(image);
        setFitType(FitType.FILL_Y);
        screen.addBlurSegment("ScalableImage", parameter -> onBlurApply(parameter, this));

        painters.add(delta -> {
            if(!isDisplayable())
                return false;
            if(getWidth() <= 0 || getHeight() <= 0)
                return false;

            if(animState == AnimState.HIDING){
                alpha -= speed * delta;
                if(alpha <= 0){
                    alpha = 0;
                    if(newImg != null) {
                        super.setImage(newImg);
                        updateImage();
                        newImg = null;
                    }
                    animState = AnimState.SHOWING;
                }
                return true;
            }
            if(animState == AnimState.SHOWING){
                alpha += speed * delta;
                if(alpha >= 255) {
                    alpha = 255;
                    animState = AnimState.NONE;
                }
                return true;
            }
            return false;
        });

        new AnimationTimer(screen.getLauncher(), delta -> {
            if(!isDisplayable())
                return;
            if(getWidth() <= 0 || getHeight() <= 0)
                return;

            if(animState == AnimState.HIDING){
                alpha -= speed * delta;
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
                alpha += speed * delta;
                if(alpha >= 255) {
                    alpha = 255;
                    animState = AnimState.NONE;
                }
                screen.getLauncher().updateUI();
            }
        });
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
        int scaledWidth = (int)((double)getWidth() * SystemUtils.getWindowScaleFactor());
        int scaledHeight = (int)(getHeight() * SystemUtils.getWindowScaleFactor());
        showingImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = showingImage.createGraphics();
        super.drawImage(graphics, scaledWidth, scaledHeight);
        graphics.transform(AffineTransform.getScaleInstance(SystemUtils.getWindowScaleFactor(), SystemUtils.getWindowScaleFactor()));
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
        parameter.setTextureAlpha(alpha / 255d);

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
