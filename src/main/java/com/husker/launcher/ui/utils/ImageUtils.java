package com.husker.launcher.ui.utils;

import com.jhlabs.image.GaussianFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import static java.awt.image.BufferedImage.*;

public class ImageUtils {

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage)
            return (BufferedImage) img;
        if(img == null)
            return null;

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), TYPE_INT_ARGB);

        Graphics2D gr = bimage.createGraphics();
        gr.drawImage(img, 0, 0, null);
        gr.dispose();

        return bimage;
    }

    public static BufferedImage getSubImage(BufferedImage image, int x, int y, int w, int h){
        Area toRender = new Area(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
        toRender.intersect(new Area(new Rectangle(x, y, w, h)));
        if(toRender.getBounds().width > 0 && toRender.getBounds().height > 0)
            return image.getSubimage(toRender.getBounds().x, toRender.getBounds().y, toRender.getBounds().width, toRender.getBounds().height);
        return null;
    }

    public static BufferedImage getSubImage(BufferedImage image, Shape shape){
        return getSubImage(image, shape.getBounds().x, shape.getBounds().y, shape.getBounds().width, shape.getBounds().height);
    }

    public static BufferedImage getScaledInstance(BufferedImage image, int x, int y, int hints){
        return toBufferedImage(image.getScaledInstance(x, y, hints));
    }

    public static VolatileImage createVolatileImage(JFrame frame, int width, int height){
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        VolatileImage image = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);

        int validateResult = image.validate(gc);
        if(validateResult == VolatileImage.IMAGE_RESTORED) {
            Graphics2D g2d = image.createGraphics();
            g2d.setComposite(AlphaComposite.Src);
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.dispose();
        }
        if(validateResult == VolatileImage.IMAGE_INCOMPATIBLE)
            return createVolatileImage(frame, width, height);

        return image;
    }

    public static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }


    private static BufferedImage generateMask(BufferedImage imgSource, Color color, float alpha) {
        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgBlur = new BufferedImage(imgWidth, imgHeight, TYPE_INT_ARGB);
        Graphics2D g2 = imgBlur.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2.setColor(color);

        g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2.dispose();

        return imgBlur;
    }

    private static BufferedImage generateBlur(BufferedImage imgSource, int size, Color color, float alpha) {
        GaussianFilter filter = new GaussianFilter(size);

        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgBlur = new BufferedImage(imgWidth, imgHeight, TYPE_INT_ARGB);
        Graphics2D g2 = imgBlur.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2.setColor(color);

        g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2.dispose();

        imgBlur = filter.filter(imgBlur, null);

        return imgBlur;
    }

    public static BufferedImage applyDefaultShadow(BufferedImage image){
        return applyShadow(image, 10, Color.black, 0.6f);
    }

    public static BufferedImage applyShadow(BufferedImage imgSource, int size, Color color, float alpha) {
        BufferedImage result = new BufferedImage(imgSource.getWidth(), imgSource.getHeight(), TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(generateShadow(imgSource, size, color, alpha), 0, 0, imgSource.getWidth(), imgSource.getHeight(), null);
        g2d.drawImage(imgSource, 0, 0, null);
        g2d.dispose();

        return result;
    }

    private static BufferedImage generateShadow(BufferedImage imgSource, int size, Color color, float alpha) {
        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgMask = new BufferedImage(imgWidth, imgHeight, TYPE_INT_ARGB);
        Graphics2D g2 = imgMask.createGraphics();
        applyQualityRenderingHints(g2);

        int x = Math.round((imgWidth - imgSource.getWidth()) / 2f);
        int y = Math.round((imgHeight - imgSource.getHeight()) / 2f);
        g2.drawImage(imgSource, x, y, null);
        g2.dispose();

        // ---- Blur here ---

        BufferedImage imgGlow = generateBlur(imgMask, (size * 2), color, alpha);

        return imgGlow;
    }

    private static Image applyMask(BufferedImage sourceImage, BufferedImage maskImage) {
        return applyMask(sourceImage, maskImage, AlphaComposite.DST_IN);
    }

    private static BufferedImage applyMask(BufferedImage sourceImage, BufferedImage maskImage, int method) {
        BufferedImage maskedImage = null;
        if (sourceImage != null) {

            int width = maskImage.getWidth(null);
            int height = maskImage.getHeight(null);

            maskedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = maskedImage.createGraphics();

            int x = (width - sourceImage.getWidth(null)) / 2;
            int y = (height - sourceImage.getHeight(null)) / 2;

            mg.drawImage(sourceImage, x, y, null);
            mg.setComposite(AlphaComposite.getInstance(method));

            mg.drawImage(maskImage, 0, 0, null);

            mg.dispose();
        }
        return maskedImage;
    }

}
