package com.husker.glassui.screens.main;

import com.husker.glassui.components.social.impl.InstPhotoPanel;
import com.husker.glassui.components.social.impl.YoutubeVideoPanel;
import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.SocialLoadGrid;
import com.husker.glassui.components.social.impl.VkPostPanel;

public class NewsPanel extends SocialLoadGrid {

    private boolean loaded = false;

    public NewsPanel(Screen screen){
        super(screen, 1, 3);

        try {
            addSocialPanel(YoutubeVideoPanel.create(screen, 0));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        try {
            addSocialPanel(VkPostPanel.create(screen, 0));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        try {
            addSocialPanel(InstPhotoPanel.create(screen, 0));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void load(){
        if(!loaded)
            loaded = true;
        else
            return;

        new Thread(this::updatePanels).start();
    }
}
