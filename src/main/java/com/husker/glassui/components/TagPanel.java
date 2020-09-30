package com.husker.glassui.components;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.label.WebLabel;
import com.husker.glassui.GlassUI;
import com.husker.launcher.components.LabelButton;
import com.husker.launcher.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TagPanel extends TransparentPanel {

    private final Screen screen;
    private final WebLabel tag;
    private final LabelButton button;
    private final TransparentPanel content;

    public TagPanel(Screen screen, String text){
        this(screen);
        setTag(text);
    }

    public TagPanel(Screen screen){
        this.screen = screen;
        tag = GlassUI.createTagLabel(screen, "Tag");
        button = new LabelButton();
        button.setVisible(false);

        setLayout(new BorderLayout());
        add(new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(tag);
            add(button);
        }}, BorderLayout.NORTH);
        add(content = new TransparentPanel(){{
            setMargin(0, 5, 0, 0);
            setLayout(new VerticalFlowLayout());
        }});
    }

    public void setContentLayout(LayoutManager manager){
        content.setLayout(manager);
    }

    public void addContent(Component component){
        content.add(component);
    }

    public void setTag(String text){
        tag.setText(text);
    }

    public void setButtonIcons(BufferedImage def, BufferedImage selected){
        button.setVisible(def != null);
        if(def == null){
            button.setIcon(null);
            button.setSelectedIcon(null);
        }else {
            button.setIcon(new ImageIcon(def.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
            button.setSelectedIcon(new ImageIcon(selected.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        }
    }
}
