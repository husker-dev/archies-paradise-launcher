package com.husker.glassui.components.social;

import com.alee.managers.style.StyleId;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import java.awt.*;

public class SocialLoadGrid extends TransparentPanel {

    private final Screen screen;
    private final TransparentPanel blankContent;
    private final TransparentPanel content;
    private int max;
    private int current = 0;

    public SocialLoadGrid(Screen screen, int max, int gridWidth, int gridHeight) {
        setMargin(12);

        this.screen = screen;

        super.setLayout(new OverlayLayout(this));
        add(content = new TransparentPanel());
        add(blankContent = new TransparentPanel());

        content.setLayout(new GridLayout(gridHeight, gridWidth, 12, 12));
        blankContent.setLayout(new GridLayout(gridHeight, gridWidth, 12, 12));

        setMaxCount(max);
    }

    public void setIndent(int size){
        ((GridLayout)content.getLayout()).setHgap(size);
        ((GridLayout)content.getLayout()).setVgap(size);
        ((GridLayout)blankContent.getLayout()).setHgap(size);
        ((GridLayout)blankContent.getLayout()).setVgap(size);
    }

    public void setMaxCount(int max){
        if(this.max != max) {
            this.max = max;

            blankContent.removeAll();
            content.removeAll();
            for (int i = 0; i < max; i++)
                blankContent.add(new BlankSocialPanel(screen));
            for (int i = 0; i < max; i++)
                content.add(new TransparentPanel());
        }
    }

    public void addSocialPanel(SocialPanel panel){
        if(current >= max)
            return;

        content.remove(current);
        content.add(panel, current);

        ((BlankSocialPanel) blankContent.getComponent(current)).hidePanel();

        current++;

        if(current == max)
            blankContent.removeAll();

        screen.getLauncher().updateUI();
    }
}
