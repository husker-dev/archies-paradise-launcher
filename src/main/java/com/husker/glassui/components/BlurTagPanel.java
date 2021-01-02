package com.husker.glassui.components;

import com.alee.extended.layout.VerticalFlowLayout;
import com.husker.glassui.GlassUI;
import com.husker.launcher.ui.components.LabelButton;
import com.husker.launcher.ui.components.MLabel;
import com.husker.launcher.ui.components.TransparentPanel;
import com.husker.launcher.ui.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BlurTagPanel extends TransparentPanel {


    private final MLabel tag;
    private final LabelButton button;
    private final TransparentPanel content;

    public BlurTagPanel(Screen screen, String text){
        this(screen);
        setTag(text);
    }

    public BlurTagPanel(Screen screen){
        tag = GlassUI.createTagLabel(screen, "Tag");
        button = new LabelButton();
        button.setImageSize(20);
        button.setVisible(false);

        setLayout(new BorderLayout());
        add(new TransparentPanel(){{
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
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

    public TransparentPanel getContent(){
        return content;
    }

    public void setTag(String text){
        tag.setText(text);
    }

    public void addButtonAction(Runnable action){
        button.addActionListener(action);
    }

    public void setButtonIcons(BufferedImage def, BufferedImage selected){
        button.setVisible(def != null);
        if(def == null){
            button.setImage(null);
            button.setSelectedImage(null);
        }else {
            button.setImage(def);
            button.setSelectedImage(selected);
        }
    }
}
