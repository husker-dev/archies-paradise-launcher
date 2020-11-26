package com.husker.launcher.ui.blur.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.util.ArrayList;

public class FastGaussianBlur implements GaussianBlurExecutor{

    private int factor = 5;

    public FastGaussianBlur(){}

    public FastGaussianBlur(int factor){
        setFactor(factor);
    }

    public BufferedImage blur(BufferedImage source) {
        return ProcessImage(source, factor);
    }

    public void setFactor(int factor){
        this.factor = factor;
    }

    public int getFactor(){
        return factor;
    }

    private static BufferedImage ProcessImage(BufferedImage image, int radius) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] changedPixels = new int[pixels.length];

        FastGaussianBlur(pixels, changedPixels, width, height, radius);

        BufferedImage newImage = new BufferedImage(width, height, image.getType());
        newImage.setRGB(0, 0, width, height, changedPixels, 0, width);

        return newImage;
    }


    private static void FastGaussianBlur(int[] source, int[] output, int width, int height, int radius) {
        ArrayList<Integer> gaussianBoxes = CreateGaussianBoxes(radius, 3);
        BoxBlur(source, output, width, height, (gaussianBoxes.get(0) - 1) / 2);
        BoxBlur(output, source, width, height, (gaussianBoxes.get(1) - 1) / 2);
        BoxBlur(source, output, width, height, (gaussianBoxes.get(2) - 1) / 2);
    }

    private static ArrayList<Integer> CreateGaussianBoxes(double sigma, int n) {
        double idealFilterWidth = Math.sqrt((12 * sigma * sigma / n) + 1);

        int filterWidth = (int) Math.floor(idealFilterWidth);

        if (filterWidth % 2 == 0)
            filterWidth--;

        int filterWidthU = filterWidth + 2;

        double mIdeal = (12 * sigma * sigma - n * filterWidth * filterWidth - 4 * n * filterWidth - 3 * n) / (-4 * filterWidth - 4);
        double m = Math.round(mIdeal);

        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < n; i++)
            result.add(i < m ? filterWidth : filterWidthU);

        return result;
    }

    private static void BoxBlur(int[] source, int[] output, int width, int height, int radius) {
        System.arraycopy(source, 0, output, 0, source.length);

        BoxBlurHorizontal(output, source, width, height, radius);
        BoxBlurVertical(source, output, width, height, radius);
    }

    private static void BoxBlurHorizontal(int[] sourcePixels, int[] outputPixels, int width, int height, int radius) {
        try {
            float iarr = 1f / (radius + radius + 1);
            for (int i = 0; i < height; i++) {
                int ti = i * width;
                int li = ti;
                int ri = ti + radius;

                Color fv = new Color(sourcePixels[ti]);
                Color lv = new Color(sourcePixels[ti + width - 1]);

                float current_r = fv.getRed() * (radius + 1);
                float current_g = fv.getGreen() * (radius + 1);
                float current_b = fv.getBlue() * (radius + 1);

                for (int j = 0; j < radius; j++) {
                    Color pixel = new Color(sourcePixels[ti + j]);
                    current_r += pixel.getRed();
                    current_g += pixel.getGreen();
                    current_b += pixel.getBlue();
                }

                for (int j = 0; j <= radius; j++) {
                    Color pixel = new Color(sourcePixels[ri++]);
                    current_r += pixel.getRed() - fv.getRed();
                    current_g += pixel.getGreen() - fv.getGreen();
                    current_b += pixel.getBlue() - fv.getBlue();

                    outputPixels[ti++] = getRGB(current_r * iarr, current_g * iarr, current_b * iarr);
                }

                for (int j = radius + 1; j < width - radius; j++) {
                    Color first_pixel = new Color(sourcePixels[ri++]);
                    Color second_pixel = new Color(sourcePixels[li++]);

                    current_r += first_pixel.getRed() - second_pixel.getRed();
                    current_g += first_pixel.getGreen() - second_pixel.getGreen();
                    current_b += first_pixel.getBlue() - second_pixel.getBlue();

                    outputPixels[ti++] = getRGB(current_r * iarr, current_g * iarr, current_b * iarr);
                }

                for (int j = width - radius; j < width; j++) {
                    Color pixel = new Color(sourcePixels[li++]);
                    current_r += lv.getRed() - pixel.getRed();
                    current_g += lv.getGreen() - pixel.getGreen();
                    current_b += lv.getBlue() - pixel.getBlue();

                    outputPixels[ti++] = getRGB(current_r * iarr, current_g * iarr, current_b * iarr);
                }
            }
        }catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    private static void BoxBlurVertical(int[] sourcePixels, int[] outputPixels, int width, int height, int radius) {
        try {
            float iarr = 1f / (radius + radius + 1);
            for (int i = 0; i < width; i++) {
                int ti = i;
                int li = ti;
                int ri = ti + radius * width;

                Color fv = new Color(sourcePixels[ti]);
                Color lv = new Color(sourcePixels[ti + width * (height - 1)]);

                float current_r = fv.getRed() * (radius + 1);
                float current_g = fv.getGreen() * (radius + 1);
                float current_b = fv.getBlue() * (radius + 1);

                for (int j = 0; j < radius; j++) {
                    Color pixel = new Color(sourcePixels[ti + j * width]);
                    current_r += pixel.getRed();
                    current_g += pixel.getGreen();
                    current_b += pixel.getBlue();
                }
                for (int j = 0; j <= radius; j++) {
                    Color pixel = new Color(sourcePixels[ri]);

                    current_r += pixel.getRed() - fv.getRed();
                    current_g += pixel.getGreen() - fv.getGreen();
                    current_b += pixel.getBlue() - fv.getBlue();

                    outputPixels[ti] = getRGB(current_r * iarr, current_g * iarr, current_b * iarr);

                    ri += width;
                    ti += width;
                }
                for (int j = radius + 1; j < height - radius; j++) {
                    Color first_pixel = new Color(sourcePixels[ri]);
                    Color second_pixel = new Color(sourcePixels[li]);

                    current_r += first_pixel.getRed() - second_pixel.getRed();
                    current_g += first_pixel.getGreen() - second_pixel.getGreen();
                    current_b += first_pixel.getBlue() - second_pixel.getBlue();

                    outputPixels[ti] = getRGB(current_r * iarr, current_g * iarr, current_b * iarr);

                    li += width;
                    ri += width;
                    ti += width;
                }
                for (int j = height - radius; j < height; j++) {
                    Color pixel = new Color(sourcePixels[li]);

                    current_r += lv.getRed() + pixel.getRed();
                    current_g += lv.getGreen() + pixel.getGreen();
                    current_b += lv.getBlue() + pixel.getBlue();

                    outputPixels[ti] = getRGB(current_r * iarr, current_g * iarr, current_b * iarr);

                    li += width;
                    ti += width;
                }
            }
        }catch (Exception ex){}
    }

    private static int getRGB(float R, float G, float B){
        if((int)R > 255)
            R = 255;
        if((int)G > 255)
            G = 255;
        if((int)B > 255)
            B = 255;
        return new Color((int)R, (int)G, (int)B).getRGB();
    }
}
