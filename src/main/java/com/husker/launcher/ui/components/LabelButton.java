package com.husker.launcher.ui.components;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class LabelButton extends MLabel {

    private BufferedImage def;
    private BufferedImage selected;

    public LabelButton(){
        this(null, null);
    }

    public LabelButton(BufferedImage def, BufferedImage selected, int width, int height){
        this(def, selected);
        setImageSize(width, height);
    }

    public LabelButton(BufferedImage def, BufferedImage selected){
        setImage(def);
        this.def = def;
        this.selected = selected;

        setHorizontalAlignment(CENTER);

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
        super.setImage(hovered ? selected : def);
    }

    public void setImage(BufferedImage icon){
        super.setImage(icon);
        this.def = icon;
    }

    public void setSelectedImage(BufferedImage icon){
        selected = icon;
    }
}
