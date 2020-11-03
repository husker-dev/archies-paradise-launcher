package com.husker.glassui.screens.main.social;

import com.husker.glassui.components.social.youtube.YoutubeVideoPanel;
import com.husker.launcher.ui.Screen;

public class YoutubePanel extends SocialTabPanel {

    public YoutubePanel(Screen screen) {
        super(screen);

        for(int i = 0; i < 4; i++)
            addSocialPanel(YoutubeVideoPanel.create(getScreen(), i));
    }

    public void onLoad() {
        setLogo(getScreen().getLauncher().NetManager.Social.getYoutubeLogo());
        setTitle(getScreen().getLauncher().NetManager.Social.getYoutubeTitle());
        setDescription(getScreen().getLauncher().NetManager.Social.getYoutubeSubscribers());
        setLink(getScreen().getLauncher().NetManager.Social.getYoutubeUrl());
    }
}
