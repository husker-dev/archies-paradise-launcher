package com.husker.glassui.screens.main.social;

import com.husker.glassui.components.social.impl.YoutubeVideoPanel;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.Screen;

public class YoutubePanel extends SocialTabPanel {

    public YoutubePanel(Screen screen) {
        super(screen);

        for(int i = 0; i < 4; i++)
            addSocialPanel(YoutubeVideoPanel.create(getScreen(), i));
    }

    public void onLoad() {
        setLogo(Social.YouTube.getLogo());
        setTitle(Social.YouTube.getTitle());
        setDescription(Social.YouTube.getSubscribers());
        setLink(Social.YouTube.getUrl());
    }
}
