package com.husker.launcher.ui.utils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class ComponentUtils {

    public static Point getComponentLocationOnScreen(JFrame frame, Component component){
        try {
            Point rootPaneOrigin = frame.getRootPane().getContentPane().getLocationOnScreen();
            if (!component.isDisplayable())
                return new Point(0, 0);
            Point myComp2Origin = component.getLocationOnScreen();
            return new Point(
                    (int) (myComp2Origin.getX() - rootPaneOrigin.getX()),
                    (int) (myComp2Origin.getY() - rootPaneOrigin.getY()));
        }catch (Exception ex){
            return new Point(0, 0);
        }
    }

    public static void makeMouseEventTransparent(Component component){
        makeMouseEventTransparent(component, component.getParent());
    }

    public static void makeMouseEventTransparent(Component component, Component parent){
        component.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
            public void mouseExited(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
            public void mouseClicked(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
            public void mousePressed(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
            public void mouseReleased(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
        });
        component.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
            public void mouseDragged(MouseEvent e) {
                if(parent != null)
                    parent.dispatchEvent(e);
            }
        });
    }

    public static boolean isParentInvisible(Component component){
        Component current = component;
        while((current = current.getParent()) != null){
            if(!current.isVisible())
                return true;
        }
        return false;
    }

    public static int getStringWidth(String text, Font font){
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);

        return (int)(font.getStringBounds(text, frc).getWidth()) + 5;
    }

    public static void setPreferredWidth(Component component, int width){
        component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
    }

    public static void setPreferredHeight(Component component, int height){
        component.setPreferredSize(new Dimension(component.getPreferredSize().width, height));
    }

    public static void setMargin(JComponent component, int top, int left, int bottom, int right){
        component.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }
}
