package com.husker.launcher.components;

import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.utils.RenderUtils;
import com.husker.launcher.utils.ShapeUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.utils.ShapeUtils.*;
import static javax.swing.SwingConstants.*;

public class ProgressBar extends TransparentPanel {

    private boolean disposed = false;
    private final Screen screen;

    private double currentValue = 0;
    private double value = 0;

    private WebLabel textLabel;
    private WebLabel valueLabel;
    private WebLabel speedLabel;

    public ProgressBar(Screen screen){
        this.screen = screen;

        setLayout(new BorderLayout(0, 0));
        add(textLabel = GlassUI.createSimpleLabel(""), BorderLayout.WEST);
        add(speedLabel = GlassUI.createSimpleLabel(""), BorderLayout.CENTER);
        add(valueLabel = GlassUI.createSimpleLabel(""), BorderLayout.EAST);

        textLabel.setForeground(GlassUI.Colors.labelText);
        textLabel.setHorizontalAlignment(LEFT);

        valueLabel.setForeground(GlassUI.Colors.labelText);
        valueLabel.setHorizontalAlignment(RIGHT);
        valueLabel.setPreferredWidth(115);

        speedLabel.setForeground(GlassUI.Colors.labelLightText);
        speedLabel.setHorizontalAlignment(RIGHT);

        setPreferredHeight(30);
        new Timer().schedule(new TimerTask() {
            public void run() {
                currentValue += (value - currentValue) / 10;
            }
        }, 0, 10);
    }

    public void setText(String text){
        textLabel.setText(text);
    }

    public void paint(Graphics graphics) {
        Graphics2D g2d = (Graphics2D)graphics;
        RenderUtils.enableAntialiasing(g2d);

        int height = 5;
        Shape shape = ShapeUtils.createRoundRectangle(0, getHeight() - height, getWidth(), height, height, height, ALL_CORNERS);
        Shape progressShape = ShapeUtils.createRoundRectangle(0, getHeight() - height, Math.max(currentValue / 100d * getWidth(), height), height, height, height, ALL_CORNERS);

        g2d.setColor(new Color(190, 190, 190));
        g2d.fill(shape);

        g2d.setColor(new Color(0, 200, 0, 200));
        g2d.fill(progressShape);

        RenderUtils.disableAntialiasing(g2d);

        super.paint(graphics);
    }

    public void setValue(double value){
        if(value > 100)
            value = 0;
        this.value = value;
        //valueLabel.setText((int)value + "%");
        repaint();
    }

    public void setValueText(String text){
        valueLabel.setText(text);
    }

    public void setSpeedText(String text){
        speedLabel.setText(text);
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
