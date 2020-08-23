package com.husker.launcher.utils;

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

    public static BufferedImage getSubImage(BufferedImage image, Shape shape){
        Area toRender = new Area(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
        toRender.intersect(new Area(shape));
        if(toRender.getBounds().width > 0 && toRender.getBounds().height > 0)
            return image.getSubimage(toRender.getBounds().x, toRender.getBounds().y, toRender.getBounds().width, toRender.getBounds().height);
        return null;
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

}
