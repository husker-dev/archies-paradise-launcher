package com.husker.launcher.ui.utils;

import com.husker.launcher.ui.blur.BlurParameter;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

public class UIUtils {

    public enum ShadowSide{
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    public static Shape keepShadow(BlurParameter parameter, ShadowSide... shadows){
        int round = 0;
        if(parameter.getShape() instanceof RoundRectangle2D)
            round = (int)((RoundRectangle2D)parameter.getShape()).getArcWidth();
        return keepShadow(parameter, round, shadows);
    }

    public static Shape keepShadow(BlurParameter parameter, int round, ShadowSide... shadows){
        ArrayList<ShadowSide> sides = new ArrayList<>(Arrays.asList(shadows));

        int shadow = parameter.getShadowSize();

        Rectangle rectangle = parameter.getShape().getBounds();

        Area out = new Area();

        if(sides.contains(ShadowSide.TOP))
            out.add(new Area(new Rectangle(rectangle.x, rectangle.y - shadow, rectangle.width, shadow + round)));
        if(sides.contains(ShadowSide.LEFT))
            out.add(new Area(new Rectangle(rectangle.x - shadow, rectangle.y, shadow + round, rectangle.height)));
        if(sides.contains(ShadowSide.RIGHT))
            out.add(new Area(new Rectangle(rectangle.x + rectangle.width - round, rectangle.y, shadow + round, rectangle.height)));
        if(sides.contains(ShadowSide.BOTTOM))
            out.add(new Area(new Rectangle(rectangle.x, rectangle.y + rectangle.height - round, rectangle.width, shadow + round)));

        return out;
    }
}
