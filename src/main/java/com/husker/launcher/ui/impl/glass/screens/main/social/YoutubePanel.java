package com.husker.launcher.ui.impl.glass.screens.main.social;

import com.alee.laf.panel.WebPanel;
import com.alee.managers.style.StyleId;
import com.husker.launcher.ui.Screen;
import com.husker.launcher.ui.impl.glass.components.social.SocialLoadGrid;
import com.husker.launcher.ui.impl.glass.components.social.youtube.YoutubeVideoPanel;

import java.awt.*;

public class YoutubePanel extends WebPanel {

    private final Screen screen;
    private final SocialLoadGrid social;
    private boolean loaded = false;

    public YoutubePanel(Screen screen){
        super(StyleId.panelTransparent);
        this.screen = screen;

        setLayout(new BorderLayout());

        add(social = new SocialLoadGrid(screen, 4, 2, 2));
        social.setMargin(20);
    }

    public void onShow(){
        if(loaded)
            return;
        else
            loaded = true;

        screen.getLauncher().BrowserManager.getYoutubeVideoParametersAsync(posts -> {
            int count = Math.min(4, posts.length);
            social.setMaxCount(count);

            for(int i = 0; i < count; i++)
                social.addSocialPanel(new YoutubeVideoPanel(screen, posts[i]));
        });
    }
}
