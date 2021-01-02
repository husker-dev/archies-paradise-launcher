package com.husker.glassui.components;

import com.husker.launcher.Resources;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.utils.ComponentUtils;
import com.husker.launcher.ui.utils.ShapeUtils;


import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import static com.husker.launcher.ui.utils.ShapeUtils.Corner.*;

public class BlurButtonLineChooser extends TransparentPanel implements BlurComponent{

    private final Screen screen;
    private int selected = 0;
    private final HashMap<MLabel, Boolean> active = new HashMap<>();
    private final ArrayList<Consumer<Integer>> selectedListeners = new ArrayList<>();
    private boolean disposed = false;

    public BlurButtonLineChooser(Screen screen){
        this.screen = screen;

        setLayout(new GridBagLayout());

        // Selected segment
        screen.addBlurSegment("ButtonLineChooser.Selected", parameter -> {
            checkForDispose(parameter);
            if(returnOnInvisible(parameter, BlurButtonLineChooser.this))
                return;

            int x = 0;
            int width = 0;
            for(int i = 0; i < getComponentCount(); i++){
                if(i < selected)
                    x += getComponent(i).getWidth();
                if(i == selected){
                    width = getComponent(i).getWidth();
                    break;
                }
            }

            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

            GlassUI.applyTopLayer(parameter);
            parameter.setShadowSize(2);
            if (selected == 0)
                parameter.setShape(ShapeUtils.createRoundRectangle(location.x + x, location.y, width, getHeight(), 25, 25, TOP_LEFT, BOTTOM_LEFT));
            if (selected > 0 && selected < getComponentCount() - 1)
                parameter.setShape(ShapeUtils.createRoundRectangle(location.x + x, location.y, width, getHeight(), 25, 25));
            if (selected == getComponentCount() - 1)
                parameter.setShape(ShapeUtils.createRoundRectangle(location.x + x, location.y, width, getHeight(), 25, 25, TOP_RIGHT, BOTTOM_RIGHT));
        });

        // Left segment
        screen.addBlurSegment("ButtonLineChooser.Left", parameter -> {
            checkForDispose(parameter);
            if(returnOnInvisible(parameter, BlurButtonLineChooser.this))
                return;

            int width = 0;
            for(int i = 0; i < getComponentCount(); i++){
                if(i < selected)
                    width += getComponent(i).getWidth();
                else
                    break;
            }

            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

            GlassUI.applyTopLayer(parameter);
            parameter.setShadowSize(5);
            parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
            parameter.setShape(ShapeUtils.createRoundRectangle(location.x, location.y, width, getHeight(), 25, 25, TOP_LEFT, BOTTOM_LEFT));
        });

        // Right segment
        screen.addBlurSegment("ButtonLineChooser.Right", parameter -> {
            checkForDispose(parameter);
            if(returnOnInvisible(parameter, BlurButtonLineChooser.this))
                return;

            int width = 0;
            int x = 0;
            for(int i = 0; i < getComponentCount(); i++)
                if(i <= selected)
                    x += getComponent(i).getWidth();
                else
                    width += getComponent(i).getWidth();

            Point location = ComponentUtils.getComponentLocationOnScreen(screen.getLauncher(), this);

            GlassUI.applyTopLayer(parameter);
            parameter.setShadowSize(5);

            parameter.setAdditionColor(GlassUI.Colors.buttonDefault);
            parameter.setShape(ShapeUtils.createRoundRectangle(location.x + x, location.y, width, getHeight(), 25, 25, TOP_RIGHT, BOTTOM_RIGHT));
        });
    }

    public void addButton(String text){
        MLabel label = createLabel();
        int index = getComponentCount();
        label.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(active.get(label) && getSelectedIndex() != index)
                    setSelected(index);
            }
        });
        label.setText(text);
        label.setPadding(2, 12, 0, 12);
        add(label, new GridBagConstraints() {{
            this.weightx = 1;
            this.fill = 1;
            this.insets = new Insets(0, 0, 0, 0);
        }});
        setEnabled(index, true);
    }

    public void setEnabled(int index, boolean isEnabled){
        MLabel label = (MLabel)getComponent(index);
        active.put(label, isEnabled);

        if(isEnabled)
            label.setForeground(GlassUI.Colors.labelLightText);
        else
            label.setForeground(new Color(170,170, 170));
    }

    public void setSelected(int index){
        SwingUtilities.invokeLater(() -> {
            selected = index;
            screen.getLauncher().repaint();
            for(Consumer<Integer> listener : selectedListeners)
                listener.accept(index);
        });
    }

    public int getSelectedIndex(){
        return selected;
    }

    public MLabel createLabel(){
        return new MLabel(){{
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            setPreferredHeight(30);
            setForeground(GlassUI.Colors.labelLightText);
            setFont(Resources.Fonts.getChronicaProExtraBold());
        }};
    }

    public void onBlurApply(BlurParameter parameter, Component component) {
        checkForDispose(parameter);
        if(returnOnInvisible(parameter, BlurButtonLineChooser.this))
            return;
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(new Color(100, 100, 100, 100));

        int cur_x = 0;
        for(int i = 0; i < getComponentCount(); i++){
            cur_x += getComponent(i).getWidth();

            if(i != selected && i != selected - 1 && i != getComponentCount() - 1)
                g.drawLine(cur_x, 10, cur_x, getHeight() - 10);
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

    public void addSelectedListener(Consumer<Integer> listener){
        selectedListeners.add(listener);
    }
}
