package com.husker.launcher.ui.impl.glass.components;

import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;

public interface BlurComponent {
    void onBlurApply(BlurParameter parameter);
    Screen getScreen();
}
