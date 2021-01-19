package com.husker.glassui.components;


import com.alee.laf.button.WebButton;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.RenderUtils;
import com.husker.launcher.utils.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class BlurButton extends WebButton implements BlurComponent{

    private RoundRectangle2D.Double shape;
    private final Screen screen;
    private boolean pressed;
    private boolean hovered = false;
    private Color color = GlassUI.Colors.buttonDefault;
    private boolean disposed = false;
    private BufferedImage image;

    public BlurButton(Screen screen){
        this(screen, (String)null);
    }

    public BlurButton(Screen screen, BufferedImage image){
        this(screen, (String)null);
        setImage(image);
    }

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

        setFont(Resources.Fonts.getChronicaProExtraBold());
        setPreferredHeight(30);
        setForeground(GlassUI.Colors.labelText);
        setPadding(3, 15, 0, 15);
        setMargin(0);
    }

    public void setPadding(int left, int right){
        setPadding(getPadding().top, left, getPadding().bottom, right);
    }

    public void setColor(Color color){
        this.color = color;
    }

    private void updateShape(){
        try {
            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);
            shape = new RoundRectangle2D.Double(location.getX(), location.getY(), getWidth(), getHeight(), 25, 25);
        }catch (Exception ignored){}
    }

    public void paint(Graphics g) {
        screen.getLauncher().repaint();
        super.paint(g);

        if(image != null) {
            Graphics2D g2d = (Graphics2D) g;

            RenderUtils.enableAntialiasing(g2d);
            RenderUtils.enableInterpolation(g2d);

            Rectangle iconBounds = getIconBounds();
            if (iconBounds != null) {
                if(SystemUtils.getWindowScaleFactor(getGraphicsConfiguration()) == 1) {
                    Image scaled = image.getScaledInstance(iconBounds.width, iconBounds.height, Image.SCALE_SMOOTH);
                    g2d.drawImage(scaled, iconBounds.x, iconBounds.y - 1, iconBounds.width, iconBounds.height, null);
                }else
                    g2d.drawImage(image, iconBounds.x, iconBounds.y - 1, iconBounds.width, iconBounds.height, null);

            }
        }
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
                if(shape.contains(mouse)) {
                    hovered = true;
                    newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), GlassUI.Colors.buttonHoveredAlpha);
                }else
                    hovered = false;
                if(pressed)
                    newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), GlassUI.Colors.buttonPressedAlpha);
            }

            parameter.setAdditionColor(newColor);
            parameter.setShape(shape);
        }
    }

    public boolean isHovered(){
        return hovered;
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

    public void setIcon(Icon icon) {
        super.setIcon(icon);
        try{
            throw new Exception("Can't set icon to HiDPI aware label!");
        }catch (Exception ex){
            if(!ex.getStackTrace()[1].getClassName().equals("javax.swing.JLabel"))
                ex.printStackTrace();
        }
    }

    public void setImage(BufferedImage image){
        this.image = image;
        //if(image == null)
        //    super.setIcon(null);
    }

    public void setImage(BufferedImage image, int size){
        setImage(image, size, size);
    }

    public void setImage(BufferedImage image, int width, int height){
        setImage(image);
        setImageSize(width, height);
    }

    public BufferedImage getImage(){
        return image;
    }

    public void setImageSize(int width, int height){
        super.setIcon(new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)));
    }

    public void setImageSize(int size){
        setImageSize(size, size);
    }

    // From JLabel -> getTextRectangle()
    private Rectangle getIconBounds(){
        String text = getText();
        Icon icon = (isEnabled()) ? getIcon() : getDisabledIcon();

        if ((icon == null) && (text == null))
            return null;

        Rectangle paintIconR = new Rectangle();
        Rectangle paintTextR = new Rectangle();
        Rectangle paintViewR = new Rectangle();
        Insets paintViewInsets = new Insets(0, 0, 0, 0);

        paintViewInsets = getInsets(paintViewInsets);
        paintViewR.x = paintViewInsets.left;
        paintViewR.y = paintViewInsets.top;
        paintViewR.width = getWidth() - (paintViewInsets.left + paintViewInsets.right);
        paintViewR.height = getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

        SwingUtilities.layoutCompoundLabel(
                this,
                getFontMetrics(getFont()),
                text,
                icon,
                getVerticalAlignment(),
                getHorizontalAlignment(),
                getVerticalTextPosition(),
                getHorizontalTextPosition(),
                paintViewR,
                paintIconR,
                paintTextR,
                getIconTextGap());

        return paintIconR;
    }

    public static class Flat extends BlurButton{

        public Flat(Screen screen) {
            super(screen);
        }

        public Flat(Screen screen, String text) {
            super(screen, text);
        }

        public void onBlurApply(BlurParameter parameter, Component component) {
            super.onBlurApply(parameter, component);
            if(returnOnInvisible(parameter, component))
                return;
            if(component == this){
                parameter.setAdditionColor(new Color(0, 0, 0, 0));
                parameter.setBlurFactor(0);
                parameter.setShadowSize(2);

                if(isHovered())
                    parameter.setShadowColor(new Color(0, 0, 0, 90));
            }
        }
    }
}
