package com.husker.launcher.ui;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.launcher.ui.components.TransparentPanel;

import javax.swing.*;
import java.awt.*;

public abstract class CenteredMenuScreen extends Screen{

    private TransparentPanel centeredPanel;

    public CenteredMenuScreen(){

    }

    public CenteredMenuScreen(int width, int height){
        this();
    }

    public abstract void onMenuInit();

    public void onInit() {
        setLayout(new GridBagLayout());

        add(centeredPanel = new TransparentPanel(){{
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

    public TransparentPanel getCenteredPanel(){
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
