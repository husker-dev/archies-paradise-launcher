package com.husker.launcher.ui.blur.impl;

import java.awt.image.BufferedImage;

public interface GaussianBlurExecutor {

    BufferedImage blur(BufferedImage source);
}
