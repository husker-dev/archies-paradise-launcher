package com.husker.launcher.blur;

import java.awt.image.BufferedImage;

public interface GaussianBlurExecutor {

    BufferedImage blur(BufferedImage source);
}
