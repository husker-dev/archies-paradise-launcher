package com.husker.launcher.ui.blur.impl;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class DefaultGaussianBlur implements GaussianBlurExecutor {

    private int radius = 5;
    private int variance = 10;

    public DefaultGaussianBlur(){}

    public DefaultGaussianBlur(int radius, int variance){
        setRadius(radius);
        setVariance(variance);
    }

    public BufferedImage blur(BufferedImage source) {
        return getGaussBlurredImage(source, generateWeightMatrix(radius, variance));
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public void setVariance(int variance){
        this.variance = variance;
    }

    public int getRadius(){
        return radius;
    }
    public int getVariance(){
        return variance;
    }

    private static double[][] generateWeightMatrix(int radius, double variance){
        double[][] weights = new double[radius][radius];

        double summation = 0;
        for(int i = 0; i < weights.length; i++){
            for(int j = 0; j < weights[0].length; j++){
                weights[i][j] = getGaussianModel(i - radius / 2, j - radius / 2, variance);
                summation += weights[i][j];
            }
        }
        for(int i = 0; i < weights.length; i++)
            for(int j = 0; j < weights[0].length; j++)
                weights[i][j] /= summation;

        return weights;
    }

    private static BufferedImage getGaussBlurredImage(BufferedImage image, int radius, int variance){
        return getGaussBlurredImage(image, generateWeightMatrix(radius, variance));
    }

    private static BufferedImage getGaussBlurredImage(BufferedImage image, double[][] weights){
        int radius = weights.length;

        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), TYPE_INT_ARGB);
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){

                double[][] distributedColorRed = new double[radius][radius];
                double[][] distributedColorGreen = new double[radius][radius];
                double[][] distributedColorBlue = new double[radius][radius];

                for(int weightX = 0; weightX < radius; weightX++){
                    for(int weightY = 0; weightY < radius; weightY++){
                        int sampleX = Math.abs(x + weightX - (radius / 2));
                        int sampleY = Math.abs(y + weightY - (radius / 2));

                        if(sampleX >= image.getWidth())
                            sampleX = 2 * image.getWidth() - sampleX - 1;
                        if(sampleY >= image.getHeight())
                            sampleY = 2 * image.getHeight() - sampleY - 1;

                        double currentWeight = weights[weightX][weightY];

                        Color sampledColor = new Color(image.getRGB(sampleX, sampleY));

                        distributedColorRed[weightX][weightY] = currentWeight * sampledColor.getRed();
                        distributedColorGreen[weightX][weightY] = currentWeight * sampledColor.getGreen();
                        distributedColorBlue[weightX][weightY] = currentWeight * sampledColor.getBlue();
                    }
                }

                out.setRGB(x, y, new Color(setWeightedColorValue(distributedColorRed), setWeightedColorValue(distributedColorGreen), setWeightedColorValue(distributedColorBlue)).getRGB());
            }
        }

        return out;
    }

    private static int setWeightedColorValue(double[][] weightedColor){
        double summation = 0;

        for(int i = 0; i < weightedColor.length; i++)
            for(int j = 0; j < weightedColor[i].length; j++)
                summation += weightedColor[i][j];

        return (int) summation;
    }

    private static double getGaussianModel(double x, double y, double variance){
        return 1 / (2 * Math.PI * Math.pow(variance, 2)) * Math.exp(-(Math.pow(x, 2) + Math.pow(y, 2)) / (2 * Math.pow(variance, 2)));
    }
}
