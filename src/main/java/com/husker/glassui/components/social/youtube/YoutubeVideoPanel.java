package com.husker.glassui.components.social.youtube;

import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.ImageSocialPanel;

public class YoutubeVideoPanel extends ImageSocialPanel {

    private YoutubeVideoParameters parameters;

    public YoutubeVideoPanel(Screen screen, YoutubeVideoParameters parameters){
        super(screen);
        this.parameters = parameters;
        setTitle(parameters.getTitle());
        setIcon(getScreen().getLauncher().Resources.Logo_Youtube);
        setImage(parameters.getImage());
    }

    public void onClick() {
        getScreen().getLauncher().NetManager.openLink(parameters.getUrl());
    }
}