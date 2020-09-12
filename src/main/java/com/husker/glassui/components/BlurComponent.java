package com.husker.glassui.components;

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

    default boolean returnOnInvisible(BlurParameter parameter, Component component){
        if(!component.isDisplayable() || !component.isVisible()) {
            parameter.setVisible(false);
            return true;
        }
        return false;
    }

    static boolean isReturnOnInvisible(BlurParameter parameter, Component component){
        if(!component.isDisplayable()) {
            parameter.setVisible(false);
            return true;
        }
        return false;
    }
}
