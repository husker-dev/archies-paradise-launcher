package com.husker.launcher.ui.impl.glass.components.social;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.utils.ConsoleUtils;

import javax.swing.*;
import java.awt.*;

import static java.awt.FlowLayout.CENTER;

public class SocialLoadGrid extends WebPanel {

    private final Screen screen;
    private final WebPanel blankContent;
    private final WebPanel content;
    private int max;
    private int current = 0;

    public SocialLoadGrid(Screen screen, int max, int gridWidth, int gridHeight) {
        super(StyleId.panelTransparent);
        setMargin(12);

        this.screen = screen;

        super.setLayout(new OverlayLayout(this));
        add(content = new WebPanel(StyleId.panelTransparent));
        add(blankContent = new WebPanel(StyleId.panelTransparent));

        content.setLayout(new GridLayout(gridHeight, gridWidth, 12, 12));
        blankContent.setLayout(new GridLayout(gridHeight, gridWidth, 12, 12));

        setMaxCount(max);
    }

    public void setMaxCount(int max){
        if(this.max != max) {
            this.max = max;

            blankContent.removeAll();
            content.removeAll();
            for (int i = 0; i < max; i++)
                blankContent.add(new BlankSocialPanel(screen));
            for (int i = 0; i < max; i++)
                content.add(new WebPanel(StyleId.panelTransparent));
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
