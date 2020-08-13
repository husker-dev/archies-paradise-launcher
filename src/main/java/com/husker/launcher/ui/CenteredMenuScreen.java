package com.husker.launcher.ui;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;

import javax.swing.*;
import java.awt.*;

public abstract class CenteredMenuScreen extends Screen{

    private WebPanel centeredPanel;

    public CenteredMenuScreen(){

    }

    public CenteredMenuScreen(int width, int height){
        this();
    }

    public abstract void onMenuInit();

    public void onInit() {
        setLayout(new GridBagLayout());

        add(centeredPanel = new WebPanel(StyleId.panelTransparent){{
            setPreferredWidth(350);

            setLayout(new VerticalFlowLayout(0, 0));
        }});
        onMenuInit();
    }

    public void addToMenu(Component component){
        centeredPanel.add(component);
    }

    public void addIndent(int height){
        addToMenu(Box.createRigidArea(new Dimension(0, height)));
    }

    public WebPanel getCenteredPanel(){
        return centeredPanel;
    }

    public int getMenuX(){
        return getCenteredPanel().getX();
    }

    public int getMenuY(){
        return getCenteredPanel().getY();
    }

    public int getMenuWidth(){
        return getCenteredPanel().getWidth();
    }

    public int getMenuHeight(){
        return getCenteredPanel().getHeight();
    }

}
