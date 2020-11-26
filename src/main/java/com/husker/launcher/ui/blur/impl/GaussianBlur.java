package com.husker.launcher.ui.blur.impl;

import java.awt.image.BufferedImage;

public class GaussianBlur {

    public static BufferedImage defaultBlur(BufferedImage source, int radius, int variance){
        return new DefaultGaussianBlur(radius, variance).blur(source);
    }

    public static BufferedImage defaultBlur(BufferedImage source){
        return new DefaultGaussianBlur().blur(source);
    }

    public static BufferedImage fastBlur(BufferedImage source, int factor){
        return new FastGaussianBlur(factor).blur(source);
    }

    public static BufferedImage fastBlur(BufferedImage source){
        return new FastGaussianBlur().blur(source);
    }
}
