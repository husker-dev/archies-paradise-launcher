package com.husker.glassui.screens.main;

import com.husker.glassui.components.social.vk.VkPostParameter;
import com.husker.glassui.components.social.youtube.YoutubeVideoParameters;
import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.SocialLoadGrid;
import com.husker.glassui.components.social.vk.VkPostPanel;
import com.husker.glassui.components.social.youtube.YoutubeVideoPanel;

public class NewsPanel extends SocialLoadGrid {

    private final static int youtubeCount = 1;
    private final static int vkCount = 2;
    private final Screen screen;
    private boolean loaded = false;

    public NewsPanel(Screen screen){
        super(screen, 3, 1, 3);
        this.screen = screen;
    }

    public void load(){
        if(!loaded)
            loaded = true;
        else
            return;

        screen.getLauncher().NetManager.Social.getYoutubeVideoParametersAsync(youtubeCount, parameters -> {
            for(YoutubeVideoParameters parameter : parameters)
                addSocialPanel(new YoutubeVideoPanel(screen, parameter));
            screen.getLauncher().updateUI();
        });

        screen.getLauncher().NetManager.Social.getVKPostParametersAsync(vkCount, parameters -> {
            for(VkPostParameter parameter : parameters)
                addSocialPanel(VkPostPanel.create(screen, parameter));
            screen.getLauncher().updateUI();
        });
    }
}
