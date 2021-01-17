package com.husker.glassui.components.social;

import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.SystemUtils;

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
            try {
                panel.update();
                new Thread(() -> {
                    SystemUtils.sleep(100);
                    screen.getLauncher().updateUI();
                }).start();
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }


    }
}
