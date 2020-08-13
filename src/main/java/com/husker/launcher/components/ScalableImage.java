package com.husker.launcher.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScalableImage extends JComponent {

    public enum FitType{
        FILL,
        FIT_X,
        FIT_Y,
        FIT_XY
    }

    private BufferedImage image;
    private FitType fitType = FitType.FIT_X;

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
    }

    public void setImage(BufferedImage image){
        this.image = image;
    }

    public BufferedImage getImage(){
        return image;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));

        int newWidth = 0;
        int newHeight = 0;

        if(fitType == FitType.FIT_X){
            float scalePercent = (float)getWidth() / (float)image.getWidth(null);

            newWidth = getWidth();
            newHeight = (int)(image.getHeight(null) * scalePercent);
        }

        if(fitType == FitType.FIT_Y){
            float scalePercent = (float)getHeight() / (float)image.getHeight(null);

            newWidth = (int)(image.getWidth(null) * scalePercent);
            newHeight = getHeight();
        }

        if(fitType == FitType.FIT_XY){
            float scalePercent = (float)getWidth() / (float)image.getWidth(null);

            newWidth = getWidth();
            newHeight = (int)(image.getHeight(null) * scalePercent);

            if(newHeight < getHeight()){
                scalePercent = (float)getHeight() / (float)image.getHeight(null);

                newWidth = (int)(image.getWidth(null) * scalePercent);
                newHeight = getHeight();
            }
        }

        if(fitType == FitType.FILL){
            newWidth = getWidth();
            newHeight = getHeight();
        }

        g2d.drawImage(image, (getWidth() - newWidth) / 2 - 1, (getHeight() - newHeight) / 2 - 1, newWidth + 2, newHeight + 2, null);
    }

    public void setFitType(FitType fitType){
        this.fitType = fitType;
    }

    public FitType getFitType() {
        return fitType;
    }
}
