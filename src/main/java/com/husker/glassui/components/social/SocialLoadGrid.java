package com.husker.glassui.components.social;

import com.alee.managers.style.StyleId;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SocialLoadGrid extends TransparentPanel {

    private final Screen screen;
    private int current = 0;

    public SocialLoadGrid(Screen screen, int gridWidth, int gridHeight) {
        setMargin(12);

        this.screen = screen;

        setLayout(new GridLayout(gridHeight, gridWidth, 12, 12));
    }

    public void setIndent(int size){
        ((GridLayout)getLayout()).setHgap(size);
        ((GridLayout)getLayout()).setVgap(size);
    }

    public void addSocialPanel(SocialPanel panel){
        if(panel == null)
            return;
        add(panel, current);
        current++;
        screen.getLauncher().updateUI();
    }

    public SocialPanel[] getSocialPanels(){
        ArrayList<SocialPanel> panels = new ArrayList<>();

        for(Component component : getComponents())
            if(component instanceof SocialPanel)
                panels.add((SocialPanel) component);
        return panels.toArray(new SocialPanel[0]);
    }

    public void updatePanels(){
        for(SocialPanel panel : getSocialPanels()) {
            panel.update();
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                screen.getLauncher().updateUI();
            }).start();
        }


    }
}
