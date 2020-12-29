package com.husker.glassui.components;

import com.alee.laf.text.WebPasswordField;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ComponentUtils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class BlurPasswordField extends WebPasswordField implements BlurComponent{

    private RoundRectangle2D.Double shape;
    private final Screen screen;
    private boolean disposed = false;

    public BlurPasswordField(Screen screen) {
        super(StyleId.passwordfieldTransparent);
        this.screen = screen;

        screen.addBlurSegment("PasswordField", parameter -> onBlurApply(parameter, this));

        setMargin(3, 7, 0, 5);
        setPreferredHeight(30);
        setFont(Resources.Fonts.getChronicaPro(15));
        setForeground(GlassUI.Colors.textFieldText);

        setCaret(new BlurTextField.CustomCaret(screen.getLauncher()));
        getActionMap().put(DefaultEditorKit.deletePrevCharAction, new BlurTextField.BeepSouncActionDeletion());
    }

    public void addFastAction(Runnable runnable){
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    runnable.run();
            }
        });
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

    public String getText(){
        return new String(getPassword());
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
                consumer.accept(new String(getPassword()));
            }
        });
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);
        if(returnOnInvisible(parameter, component))
            return;

        if(component == this){
            parameter.setAdditionColor(GlassUI.Colors.textField);
            parameter.setBlurFactor(25);
            parameter.setShadowSize(5);
            parameter.setShadowColor(new Color(0, 0, 0, 40));
            parameter.setShadowType(BlurParameter.ShadowType.INNER);
            parameter.setVisible(isVisible() && isDisplayable());

            updateShape();
            Point mouse = screen.getLauncher().getContentPane().getMousePosition();

            if(mouse != null && shape != null){
                if(shape.contains(mouse))
                    parameter.setAdditionColor(GlassUI.Colors.textFieldHovered);
                if(isFocusOwner()) {
                    parameter.setAdditionColor(GlassUI.Colors.textFieldFocused);
                }
            }

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
