package com.husker.glassui.screens.main.social;

import com.husker.glassui.components.social.youtube.YoutubeVideoPanel;
import com.husker.launcher.ui.Screen;

public class YoutubePanel extends SocialTabPanel {

    public YoutubePanel(Screen screen) {
        super(screen);
    }

    public void onLoad() {
        new Thread(() -> {
            setLogo(getScreen().getLauncher().NetManager.Social.getYoutubeLogo());
            setTitle(getScreen().getLauncher().NetManager.Social.getYoutubeTitle());
            setDescription(getScreen().getLauncher().NetManager.Social.getYoutubeSubscribers());
            setLink(getScreen().getLauncher().NetManager.Social.getYoutubeUrl());
        }).start();

        getScreen().getLauncher().NetManager.Social.getYoutubeVideoParametersAsync(4, posts -> {
            int count = Math.min(4, posts.length);
            setSocialPanelCount(count);

            for(int i = 0; i < count; i++) {
                YoutubeVideoPanel panel = new YoutubeVideoPanel(getScreen(), posts[i]);
                panel.setInner(true);
                addSocialPanel(panel);
            }
        });
    }
}
