package com.husker.glassui.screens.main.social;

import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.vk.VkPostPanel;



public class VkGroupPanel extends SocialTabPanel {

    public VkGroupPanel(Screen screen) {
        super(screen);
    }

    public void onLoad() {
        new Thread(() -> {
            setLogo(getScreen().getLauncher().NetManager.Social.getVkLogo());
            setTitle(getScreen().getLauncher().NetManager.Social.getVkTitle());
            setDescription(getScreen().getLauncher().NetManager.Social.getVkDescription());
            setLink(getScreen().getLauncher().NetManager.Social.getVkUrl());
        }).start();

        getScreen().getLauncher().NetManager.Social.getVKPostParametersAsync(4, posts -> {
            int count = Math.min(4, posts.length);
            setSocialPanelCount(count);

            for(int i = 0; i < count; i++) {
                VkPostPanel panel = VkPostPanel.create(getScreen(), posts[i]);
                panel.setInner(true);
                addSocialPanel(panel);
            }
        });
    }

}
