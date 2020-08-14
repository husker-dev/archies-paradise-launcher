package com.husker.launcher.utils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

public class ShapeUtils {

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public static Shape createRoundRectangle(double x, double y, double width, double height, double roundW, double roundH, Corner... corners){
        ArrayList<Corner> cornerArray = new ArrayList<>(Arrays.asList(corners));

        roundW = Math.min(roundW, width);
        roundH = Math.min(roundH, height);

        Area roundedRectangle = new Area(new RoundRectangle2D.Double(x, y, width, height, roundW, roundH));

        double cornerW = roundW / 2;
        double cornerH = roundH / 2;

        if(!cornerArray.contains(Corner.TOP_LEFT))
            roundedRectangle.add(new Area(new Rectangle2D.Double(x, y, cornerW, cornerH)));
        if(!cornerArray.contains(Corner.TOP_RIGHT))
            roundedRectangle.add(new Area(new Rectangle2D.Double(x + width - cornerW, y, cornerW, cornerH)));
        if(!cornerArray.contains(Corner.BOTTOM_LEFT))
            roundedRectangle.add(new Area(new Rectangle2D.Double(x, y + height - cornerH, cornerW, cornerH)));
        if(!cornerArray.contains(Corner.BOTTOM_RIGHT))
            roundedRectangle.add(new Area(new Rectangle2D.Double(x + width - cornerW, y + height - cornerH, cornerW, cornerH)));

        return roundedRectangle;
    }
}