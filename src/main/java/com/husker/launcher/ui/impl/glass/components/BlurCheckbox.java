package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.label.WebLabel;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class BlurCheckbox extends WebLabel implements BlurComponent {

    private final Screen screen;
    private boolean checked = false;
    private Runnable runnable;
    private boolean disposed = false;

    public BlurCheckbox(Screen screen){
        this(screen, "", false);
    }

    public BlurCheckbox(Screen screen, String text){
        this(screen, text, false);
    }

    public BlurCheckbox(Screen screen, boolean checked){
        this(screen, "", checked);
    }

    public BlurCheckbox(Screen screen, String text, boolean checked){
        this.screen = screen;
        setText(text);
        setChecked(checked);
        setVerticalAlignment(CENTER);
        setForeground(GlassUI.Colors.labelText);
        setFont(Resources.Fonts.ChronicaPro_ExtraBold);
        screen.addBlurSegment(parameter -> onBlurApply(parameter, this));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setChecked(!isChecked());
                if(runnable != null)
                    runnable.run();
            }
        });
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);

        GlassUI.applyBottomLayer(parameter);

        Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);
        location.x += 4;
        location.y += 4;

        if(isDisplayable() && isVisible()) {
            parameter.setShape(new RoundRectangle2D.Double(location.x, location.y, 16, 16, 5, 5));
        }
        parameter.setShadowSize(3);
        parameter.setDebugName("Chackbox." + getName());
        parameter.setShadowType(BlurParameter.ShadowType.INNER);
        parameter.setVisible(isDisplayable() && isVisible());
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


    public void setOnAction(Runnable runnable){
        this.runnable = runnable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;

        if(checked)
            setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Checkbox_On.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
        else
            setIcon(new ImageIcon(screen.getLauncher().Resources.Icon_Checkbox_Off.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
    }
}
