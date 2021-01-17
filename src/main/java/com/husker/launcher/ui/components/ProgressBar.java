package com.husker.launcher.ui.components;


import com.alee.utils.swing.extensions.SizeMethodsImpl;
import com.husker.glassui.GlassUI;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.AnimationTimer;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.RenderUtils;
import com.husker.launcher.ui.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;

import static com.husker.launcher.ui.utils.ShapeUtils.*;
import static javax.swing.SwingConstants.*;

public class ProgressBar extends JComponent {

    private double currentValue = 0;
    private double value = 0;

    private final JLabel textLabel;
    private final JLabel valueLabel;
    private final JLabel speedLabel;

    public ProgressBar(){
        setOpaque(false);

        setLayout(new BorderLayout(0, 0));
        add(textLabel = createLabel(), BorderLayout.WEST);
        add(speedLabel = createLabel(), BorderLayout.CENTER);
        add(valueLabel = createLabel(), BorderLayout.EAST);

        textLabel.setForeground(GlassUI.Colors.labelText);
        textLabel.setHorizontalAlignment(LEFT);

        valueLabel.setForeground(GlassUI.Colors.labelText);
        valueLabel.setHorizontalAlignment(RIGHT);
        valueLabel.setPreferredSize(new Dimension(115, (int)valueLabel.getPreferredSize().getHeight()));

        speedLabel.setForeground(GlassUI.Colors.labelLightText);
        speedLabel.setHorizontalAlignment(RIGHT);

        new AnimationTimer(60, delta -> {
            if(!isDisplayable() || ComponentUtils.isParentInvisible(this))
                return;
            currentValue += (value - currentValue) / 10d * (delta * 100d);
            if((int)currentValue != (int)value)
                repaint();
        });
    }

    private JLabel createLabel(){
        return new JLabel(){
            {
                setForeground(GlassUI.Colors.labelText);
                //SizeMethodsImpl.setPreferredWidth(this, 16);
                setFont(Resources.Fonts.getChronicaProExtraBold());
                setBackground(new Color(0, 0, 0, 0));
            }
        };
    }

    public void setText(String text){
        textLabel.setText(text);
        repaint();
    }

    public void setPreferredWidth(int size){
        SizeMethodsImpl.setPreferredWidth(this, size);
    }

    public void setPreferredHeight(int size){
        SizeMethodsImpl.setPreferredHeight(this, size);
    }

    public void paint(Graphics gr) {
        super.paint(gr);

        Graphics2D g2d = (Graphics2D)gr;
        RenderUtils.enableAntialiasing(g2d);

        int height = 5;
        Shape shape = ShapeUtils.createRoundRectangle(0, getHeight() - height, getWidth(), height, height, height, ALL_CORNERS);

        //System.out.println(Math.max(currentValue / 100d * getWidth(), height));
        Shape progressShape = ShapeUtils.createRoundRectangle(0, getHeight() - height, Math.max(currentValue / 100d * getWidth(), height), height, height, height, ALL_CORNERS);

        g2d.setColor(new Color(190, 190, 190));
        g2d.fill(shape);

        g2d.setColor(new Color(0, 200, 0, 200));
        g2d.fill(progressShape);
    }

    public void setValue(double value){
        if(value > 100 || Double.isNaN(value))
            value = 0;
        this.value = value;
        repaint();
    }

    public void setValueText(String text){
        valueLabel.setText(text);
        repaint();
    }

    public void setSpeedText(String text){
        speedLabel.setText(text);
        repaint();
    }
}
