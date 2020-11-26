package com.husker.glassui.components.social.impl;

import com.husker.launcher.Resources;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.social.Social;
import com.husker.launcher.social.YouTubeVideoInfo;
import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.ImageSocialPanel;

public class YoutubeVideoPanel extends ImageSocialPanel {

    public int index;
    public String url;

    public YoutubeVideoPanel(Screen screen, int index){
        super(screen);
        this.index = index;
        setIcon(Resources.Logo_Youtube);
    }

    public void update(){
        YouTubeVideoInfo info = Social.YouTube.getVideo(index);
        if(info == null)
            return;

        setTitle(info.getTitle());
        url = info.getUrl();

        new Thread(() -> setImage(info.getPreview())).start();
    }

    public void onClick() {
        NetManager.openLink(url);
    }

    public static YoutubeVideoPanel create(Screen screen, int index){
        return new YoutubeVideoPanel(screen, index);
    }
}
