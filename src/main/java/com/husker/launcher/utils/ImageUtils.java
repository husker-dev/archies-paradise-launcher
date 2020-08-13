package com.husker.launcher.utils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

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

}
