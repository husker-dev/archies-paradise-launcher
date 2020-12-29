package com.husker.launcher.ui.components;

import com.husker.launcher.ui.utils.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class ScalableImage extends JComponent {

    public enum FitType{
        FILL,
        FILL_X,
        FILL_Y,
        FILL_XY,
        FIT_XY
    }

    private BufferedImage image;
    private FitType fitType = FitType.FILL_X;
    private int round = 0;

    public ScalableImage(){}

    public ScalableImage(FitType type){
        this.fitType = type;
    }

    public ScalableImage(BufferedImage image){
        setImage(image);
    }

    public ScalableImage(BufferedImage image, FitType type){
        setImage(image);
        this.fitType = type;
        setOpaque(true);
        setBackground(new Color(0, 0, 0, 0));
    }

    public void setImage(BufferedImage image){
        this.image = image;
    }

    public BufferedImage getImage(){
        return image;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int newWidth = 0;
        int newHeight = 0;

        if(image == null)
            return;

        if(fitType == FitType.FILL_X){
            float scalePercent = (float)getWidth() / (float)image.getWidth();

            newWidth = getWidth();
            newHeight = (int)(image.getHeight(null) * scalePercent);
        }

        if(fitType == FitType.FILL_Y){
            float scalePercent = (float)getHeight() / (float)image.getHeight();

            newWidth = (int)(image.getWidth(null) * scalePercent);
            newHeight = getHeight();
        }

        if(fitType == FitType.FILL_XY){
            float scalePercent = (float)getWidth() / (float)image.getWidth();

            newWidth = getWidth();
            newHeight = (int)(image.getHeight(null) * scalePercent);

            if(newHeight < getHeight()){
                scalePercent = (float)getHeight() / (float)image.getHeight();

                newWidth = (int)(image.getWidth(null) * scalePercent);
                newHeight = getHeight();
            }
        }

        if(fitType == FitType.FIT_XY){
            float scale = (float)Math.max(getWidth(), getHeight()) / Math.max(image.getWidth(), image.getHeight());

            newWidth = (int)(image.getWidth() * scale) - 20;
            newHeight = (int)(image.getHeight() * scale) - 20;
        }

        if(fitType == FitType.FILL){
            newWidth = getWidth();
            newHeight = getHeight();
        }

        RoundRectangle2D.Double imageShape = new RoundRectangle2D.Double((getWidth() - newWidth) / 2d - 1, (getHeight() - newHeight) / 2d - 1, newWidth + 2, newHeight + 2, round, round);
        RoundRectangle2D.Double componentShape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), round, round);

        RenderUtils.enableAntialiasing(g2d);
        RenderUtils.enableInterpolation(g2d);

        g2d.setPaint(new TexturePaint(image, imageShape.getBounds()));
        g2d.setClip(imageShape.getBounds());
        g2d.fill(componentShape);
    }

    public void setFitType(FitType fitType){
        this.fitType = fitType;
    }

    public FitType getFitType() {
        return fitType;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }
}
