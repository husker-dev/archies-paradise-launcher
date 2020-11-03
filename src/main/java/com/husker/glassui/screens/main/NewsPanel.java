package com.husker.glassui.screens.main;

import com.husker.glassui.components.social.SocialPanel;
import com.husker.glassui.components.social.youtube.YoutubeVideoPanel;
import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.SocialLoadGrid;
import com.husker.glassui.components.social.vk.VkPostPanel;

public class NewsPanel extends SocialLoadGrid {

    private boolean loaded = false;


    public NewsPanel(Screen screen){
        super(screen, 1, 3);

        for(int i = 0; i < 1; i++) {
            try {
                addSocialPanel(YoutubeVideoPanel.create(screen, i));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        for(int i = 0; i < 2; i++) {
            try {
                addSocialPanel(VkPostPanel.create(screen, i));
            }catch (Exception ex){
                ex.printStackTrace();
            }
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
