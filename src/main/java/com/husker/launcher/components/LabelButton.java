package com.husker.launcher.components;

import com.alee.laf.label.WebLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabelButton extends WebLabel {

    private Icon def;
    private Icon selected;

    public LabelButton(){
        this((ImageIcon)null, null);
    }

    public LabelButton(Image def, Image selected){
        this(new ImageIcon(def), new ImageIcon(selected));
    }

    public LabelButton(Image def, Image selected, int width, int height){
        this(new ImageIcon(def.getScaledInstance(width, height, Image.SCALE_SMOOTH)), new ImageIcon(selected.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
    }

    public LabelButton(ImageIcon def, ImageIcon selected){
        super(def);
        this.def = def;
        this.selected = selected;
        setIcon(def);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent mouseEvent) {
                updateState();
            }
            public void mouseExited(MouseEvent mouseEvent) {
                updateState();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                updateState();
            }
            public void componentMoved(ComponentEvent componentEvent) {
                updateState();
            }
            public void componentShown(ComponentEvent componentEvent) {
                updateState();
            }
            public void componentHidden(ComponentEvent componentEvent) {
                updateState();
            }
        });
    }

    public void addActionListener(Runnable runnable){
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                runnable.run();
            }
        });
    }

    private void updateState(){
        boolean hovered = getMousePosition() != null;
        super.setIcon(hovered ? selected : def);
    }

    public void setIcon(Icon icon){
        super.setIcon(icon);
        this.def = icon;
    }

    public void setSelectedIcon(Icon icon){
        selected = icon;
    }
}
