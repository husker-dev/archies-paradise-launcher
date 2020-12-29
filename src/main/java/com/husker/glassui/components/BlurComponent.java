package com.husker.glassui.components;

import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.utils.ComponentUtils;

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
        return isReturnOnInvisible(parameter, component);
    }

    static boolean isReturnOnInvisible(BlurParameter parameter, Component component){
        if(component == null)
            return true;
        if(!component.isDisplayable() || !component.isVisible() || ComponentUtils.isParentInvisible(component)) {
            parameter.setVisible(false);
            return true;
        }
        return false;
    }


}
