package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.button.WebButton;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class BlurButton extends WebButton implements BlurComponent{

    private RoundRectangle2D.Double shape;
    private final Screen screen;
    private boolean pressed;
    private Color color = GlassUI.Colors.buttonDefault;
    private boolean disposed = false;

    public BlurButton(Screen screen, String text){
        super(StyleId.buttonUndecorated, text);
        this.screen = screen;

        screen.addBlurSegment("Button", parameter -> onBlurApply(parameter, this));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                pressed = true;
            }
            public void mouseReleased(MouseEvent e) {
                pressed = false;
            }
            public void mouseEntered(MouseEvent e) {
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                repaint();
            }
        });

        setFont(Resources.Fonts.ChronicaPro_ExtraBold);
        setPreferredHeight(30);
        setForeground(GlassUI.Colors.labelText);
        setMargin(5, 0, 0, 0);
    }

    public void setColor(Color color){
        this.color = color;
    }

    private void updateShape(){
        try {
            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

            shape = new RoundRectangle2D.Double(location.getX(), location.getY(), getWidth(), getHeight(), 25, 25);
        }catch (Exception ex){}
    }

    public void paint(Graphics g) {
        screen.getLauncher().repaint();
        super.paint(g);
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);

        if(component == this){
            if(returnOnInvisible(parameter, component))
                return;

            Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), GlassUI.Colors.buttonDefaultAlpha);

            parameter.setBlurFactor(25);

            parameter.setShadowSize(5);
            parameter.setShadowColor(new Color(0, 0, 0, 40));

            updateShape();
            Point mouse = screen.getLauncher().getContentPane().getMousePosition();

            if(mouse != null && shape != null && isEnabled()){
                if(shape.contains(mouse))
                    newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), GlassUI.Colors.buttonHoveredAlpha);
                if(pressed)
                    newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), GlassUI.Colors.buttonPressedAlpha);
            }

            parameter.setAdditionColor(newColor);
            parameter.setShape(shape);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void dispose() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }
}
