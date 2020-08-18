package com.husker.launcher.ui.impl.glass.components;

import com.alee.laf.text.WebPasswordField;
import com.alee.laf.text.WebTextField;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.impl.glass.GlassUI;
import com.husker.launcher.utils.ComponentUtils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class BlurTextField extends WebTextField implements BlurComponent{

    private RoundRectangle2D.Double shape;
    private final Screen screen;

    public BlurTextField(Screen screen){
        super(StyleId.textfieldTransparent);
        this.screen = screen;

        screen.addBlurSegment(parameter -> onBlurApply(parameter, this));

        setMargin(3, 7, 0, 5);
        setPreferredHeight(30);
        setFont(Resources.Fonts.ChronicaPro_ExtraBold.deriveFont(15f));
    }

    private void updateShape(){
        try {
            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

            shape = new RoundRectangle2D.Double(location.getX(), location.getY(), getWidth(), getHeight(), 15, 15);
        }catch (Exception ex){
        }
    }

    public void paint(Graphics g) {
        screen.getLauncher().repaint();
        super.paint(g);
    }

    public void addTextListener(Consumer<String> consumer){
        getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }
            public void removeUpdate(DocumentEvent e) {
                warn();
            }
            public void insertUpdate(DocumentEvent e) {
                warn();
            }
            public void warn() {
                consumer.accept(getText());
            }
        });
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        parameter.setAdditionColor(GlassUI.Colors.textField);
        parameter.setBlurFactor(25);
        parameter.setShadowSize(5);
        parameter.setShadowColor(new Color(0, 0, 0, 40));
        parameter.setShadowType(BlurParameter.ShadowType.INNER);
        parameter.setDebugName("TextField." + getName());
        parameter.setVisible(isVisible() && isDisplayable());

        if(isDisplayable() && isVisible()) {
            updateShape();
            parameter.setShape(shape);

            Point mouse = screen.getLauncher().getContentPane().getMousePosition();

            if(mouse != null){
                if(shape.contains(mouse))
                    parameter.setAdditionColor(GlassUI.Colors.textFieldHovered);
                if(isFocusOwner()) {
                    parameter.setAdditionColor(GlassUI.Colors.textFieldFocused);
                }
            }
        }
    }

    public Screen getScreen() {
        return screen;
    }
}
