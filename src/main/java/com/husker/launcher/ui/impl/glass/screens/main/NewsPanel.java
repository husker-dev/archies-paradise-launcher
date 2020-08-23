package com.husker.launcher.ui.impl.glass.screens.main;

import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.components.social.SocialLoadGrid;
import com.husker.launcher.ui.impl.glass.components.social.vk.VkPostPanel;
import com.husker.launcher.ui.impl.glass.components.social.youtube.YoutubeVideoPanel;

public class NewsPanel extends SocialLoadGrid {

    public NewsPanel(Screen screen){
        super(screen, 3, 1, 3);


        screen.getLauncher().BrowserManager.getYoutubeVideoParametersAsync(parameters -> {
            for(int i = 0; i < Math.min(parameters.length - 1, 1); i++)
                addSocialPanel(new YoutubeVideoPanel(screen, parameters[i]));
            screen.getLauncher().updateUI();
        });

        screen.getLauncher().BrowserManager.getVkPostParametersAsync(parameters -> {
            int start = 0;
            for (int i = start; i < Math.min(parameters.length, start + 2); i++)
                addSocialPanel(VkPostPanel.create(screen, parameters[i]));

            screen.getLauncher().updateUI();
        });

    }
}
