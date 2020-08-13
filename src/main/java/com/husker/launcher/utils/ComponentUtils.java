package com.husker.launcher.utils;

import javax.swing.*;
import java.awt.*;

public class ComponentUtils {

    public static Point getComponentLocationOnScreen(JFrame frame, Component component){
        Point rootPaneOrigin = frame.getRootPane().getContentPane().getLocationOnScreen();
        if(!component.isDisplayable())
            return new Point(0, 0);
        Point myComp2Origin = component.getLocationOnScreen();
        return new Point(
                (int) (myComp2Origin.getX() - rootPaneOrigin.getX()),
                (int) (myComp2Origin.getY() - rootPaneOrigin.getY()));
    }
}
