package com.husker.launcher.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SkinUtils {

    public static boolean isMale(BufferedImage skin){
        if(skin == null)
            return true;
        try {
            ArrayList<Rectangle> empty_areas = new ArrayList<>();
            empty_areas.add(new Rectangle(50, 16, 2, 4));
            empty_areas.add(new Rectangle(54, 20, 2, 12));
            empty_areas.add(new Rectangle(50, 32, 2, 4));
            empty_areas.add(new Rectangle(42, 48, 2, 4));
            empty_areas.add(new Rectangle(46, 52, 2, 12));
            empty_areas.add(new Rectangle(54, 36, 2, 12));
            empty_areas.add(new Rectangle(58, 48, 2, 4));
            empty_areas.add(new Rectangle(62, 52, 2, 12));

            for (Rectangle empty_area : empty_areas)
                for (int x = empty_area.x; x < empty_area.x + empty_area.width; x++)
                    for (int y = empty_area.y; y < empty_area.y + empty_area.height; y++)
                        if (skin.getRGB(x, y) != 0)
                            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}
