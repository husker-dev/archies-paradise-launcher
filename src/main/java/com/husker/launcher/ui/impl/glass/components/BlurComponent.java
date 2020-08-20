package com.husker.launcher.ui.impl.glass.components;

import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;

import java.awt.*;

public interface BlurComponent {
    void onBlurApply(BlurParameter parameter, Component component);
    Screen getScreen();
    void dispose();
    boolean isDisposed();

    default boolean checkForDispose(BlurParameter parameter){
        if(isDisposed()) {
            getScreen().removeBlurSegment(parameter);
            return true;
        }
        return false;
    }
}
