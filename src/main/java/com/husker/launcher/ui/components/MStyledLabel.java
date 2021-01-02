package com.husker.launcher.ui.components;

import com.alee.extended.label.WebStyledLabel;
import com.husker.launcher.ui.utils.RenderUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MStyledLabel extends WebStyledLabel {

    private BufferedImage image;

    public MStyledLabel(String text){
        super(text);
        setImage(null);
    }

    public MStyledLabel(){
        this("");
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
    }

    public void setImage(BufferedImage image, int width, int height){
        setImage(image);
        setImageSize(width, height);
    }

    public void setImageSize(int width, int height){
        super.setIcon(new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)));
    }

    public void setImageSize(int size){
        setImageSize(size, size);
    }

    public void paint(Graphics g) {
        super.paint(g);
        if(image == null)
            return;
        Graphics2D g2d = (Graphics2D) g;

        RenderUtils.enableAntialiasing(g2d);
        RenderUtils.enableInterpolation(g2d);

        Rectangle iconBounds = getIconBounds();
        if(iconBounds != null)
            g2d.drawImage(image, iconBounds.x, iconBounds.y, iconBounds.width, iconBounds.height, null);
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
}
