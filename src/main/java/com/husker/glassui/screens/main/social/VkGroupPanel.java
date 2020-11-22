package com.husker.glassui.screens.main.social;

import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.impl.VkPostPanel;



public class VkGroupPanel extends SocialTabPanel {

    public VkGroupPanel(Screen screen) {
        super(screen);
        for(int i = 0; i < 4; i++)
            addSocialPanel(VkPostPanel.create(getScreen(), i));
    }

    public void onLoad() {
        new Thread(() -> {
            setLogo(getScreen().getLauncher().API.Social.getVkLogo());
            setTitle(getScreen().getLauncher().API.Social.getVkTitle());
            setDescription(getScreen().getLauncher().API.Social.getVkDescription());
            setLink(getScreen().getLauncher().API.Social.getVkUrl());
        }).start();
    }

}
