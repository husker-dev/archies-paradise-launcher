package com.husker.glassui.components;

import com.alee.laf.text.WebTextField;
import com.alee.managers.style.StyleId;
import com.husker.launcher.Resources;
import com.husker.launcher.ui.AnimationTimer;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.RenderUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class BlurTextField extends WebTextField implements BlurComponent{

    private RoundRectangle2D.Double shape;
    private final Screen screen;
    private boolean disposed = false;

    private String prompt = "";

    public BlurTextField(Screen screen){
        super(StyleId.textfieldTransparent);
        this.screen = screen;

        screen.addBlurSegment("TextField", parameter -> onBlurApply(parameter, this));

        setMargin(3, 7, 0, 5);
        setPreferredHeight(30);
        setFont(Resources.Fonts.getChronicaProExtraBold(15));
        setForeground(GlassUI.Colors.textFieldText);

        setCaret(new CustomCaret(screen.getLauncher()));
        getActionMap().put(DefaultEditorKit.deletePrevCharAction, new BeepSouncActionDeletion());
    }

    public void addFastAction(Runnable runnable){
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                    runnable.run();
            }
        });
    }

    public void setInputPrompt(String text){
        this.prompt = text;
    }

    private void updateShape(){
        try {
            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

            shape = new RoundRectangle2D.Double(location.getX(), location.getY(), getWidth(), getHeight(), 15, 15);
        }catch (Exception ignored){
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
        checkForDispose(parameter);

        if(component == this){
            if(returnOnInvisible(parameter, component))
                return;

            parameter.setAdditionColor(GlassUI.Colors.textField);
            parameter.setBlurFactor(25);
            parameter.setShadowSize(5);
            parameter.setShadowColor(new Color(0, 0, 0, 40));
            parameter.setShadowType(BlurParameter.ShadowType.INNER);
            parameter.setVisible(isVisible() && isDisplayable());

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

    public void dispose() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public static class CustomCaret extends DefaultCaret {

        public static final int FPS = 60;

        double current_x = 0;
        double current_width = 0;
        double x = 0;
        double width = 0;

        private boolean isFocused;

        private final JFrame frame;

        // a
        private int old_from = 0;
        private int old_to = 0;

        public CustomCaret(JFrame frame){
            this.frame = frame;

            new AnimationTimer(60, delta -> {
                try {
                    double speed = 3;

                    current_width += (width - current_width) / speed * delta * 60;
                    current_x += (x - current_x) / speed * delta * 60;

                    int from = getComponent().getSelectionStart();
                    int to = getComponent().getSelectionEnd();

                    if(from == to && old_from != old_to) {
                        onSelectedChanged(from, to, old_from, old_to);
                        if(getComponent() != null)
                            getComponent().repaint();
                    }

                    if (from != to && (old_from != from || old_to != to)) {
                        onSelectedChanged(from, to, old_from, old_to);
                        if(getComponent() != null)
                            getComponent().repaint();
                    }

                    old_from = from;
                    old_to = to;

                }catch (Exception ignored){ }
            });
        }

        private final Highlighter.HighlightPainter painter = (g, offs0, offs1, bounds, c) -> {
            Graphics2D g2d = (Graphics2D) g;

            try {
                Rectangle p0 = c.getUI().modelToView(c, offs0);
                Rectangle p1 = c.getUI().modelToView(c, offs1);
                Rectangle r = p0.union(p1);

                RenderUtils.enableAntialiasing(g2d);

                int offset = 1;
                Shape shape = new RoundRectangle2D.Double(current_x - offset, r.y - 1, current_width + offset * 2d, r.height, 8, 8);
                if(isFocused)
                    RenderUtils.drawOuterShade(g2d, shape, new Color(140, 140, 140), 5);
                g.setColor(isFocused ? GlassUI.Colors.selectedColor : new Color(100, 100, 100, 0));
                g2d.fill(shape);
            } catch (BadLocationException e) {
                // can't render
            }
        };

        public void install(JTextComponent jTextComponent) {
            super.install(jTextComponent);

            jTextComponent.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent keyEvent) {
                    JTextComponent component = getComponent();
                    boolean shift = keyEvent.isShiftDown();

                    if(component.getSelectionStart() == 0 && component.getSelectionEnd() == component.getText().length() && shift)
                        if(keyEvent.getKeyCode() == KeyEvent.VK_LEFT)
                            component.setSelectionEnd(component.getText().length());
                }
            });
        }

        protected Highlighter.HighlightPainter getSelectionPainter() {
            setBlinkRate(500);
            return painter;
        }

        public void paint(Graphics graphics) {
            if(getComponent().getSelectionStart() == getComponent().getSelectionEnd())
                super.paint(graphics);
        }

        public void setSelectionVisible(boolean hasFocus) {
            if (hasFocus != isFocused) {
                isFocused = hasFocus;
                super.setSelectionVisible(false);
                super.setSelectionVisible(true);
            }
        }

        public void onSelectedChanged(int from, int to, int old_from, int old_to){
            try {
                JTextComponent component = getComponent();
                Rectangle r = component.getUI().modelToView(component, from).union(component.getUI().modelToView(component, to));

                if(old_to == old_from && from == 0 && to == getComponent().getText().length())
                    onAllSelected(r, getComponent());
                else if(old_to == old_from)
                    onDefaultSelectedStart(r, getComponent());
                else
                    onDefaultSelected(r, getComponent());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void onAllSelected(Rectangle rectangle, Component component){
            Point componentPoint = ComponentUtils.getComponentLocationOnScreen(frame, component);
            Point mouse = frame.getMousePosition();
            if(mouse == null)
                mouse = new Point(0, 0);

            x = rectangle.x;
            width = rectangle.width;

            current_width = 0;
            current_x = mouse.x - componentPoint.x;

            if(current_x < rectangle.x)
                current_x = rectangle.x;
            if(current_x > rectangle.width + rectangle.x)
                current_x = rectangle.width + rectangle.x;
        }

        public void onDefaultSelected(Rectangle rectangle, Component component){
            x = rectangle.x;
            width = rectangle.width;
        }

        public void onDefaultSelectedStart(Rectangle rectangle, Component component){
            Point componentPoint = ComponentUtils.getComponentLocationOnScreen(frame, component);
            Point mouse = frame.getMousePosition();
            if(mouse == null)
                mouse = new Point(0, 0);

            x = rectangle.x;
            width = rectangle.width;
            current_x = mouse.x - componentPoint.x;
            current_width = 0;

            if(current_x < rectangle.x)
                current_x = rectangle.x;
            if(current_x > rectangle.width + rectangle.x)
                current_x = rectangle.width + rectangle.x;
        }
    }

    public static class BeepSouncActionDeletion extends TextAction {

        public BeepSouncActionDeletion() {
            super(DefaultEditorKit.deletePrevCharAction);
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if ((target != null) && (target.isEditable())) {
                try {
                    Document doc = target.getDocument();
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                    } else if (dot > 0) {
                        int delChars = 1;

                        if (dot > 1) {
                            String dotChars = doc.getText(dot - 2, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);

                            if (c0 >= '\uD800' && c0 <= '\uDBFF' &&
                                    c1 >= '\uDC00' && c1 <= '\uDFFF') {
                                delChars = 2;
                            }
                        }

                        doc.remove(dot - delChars, delChars);
                    }
                } catch (BadLocationException ignored) {
                }
            }

        }
    }
}
