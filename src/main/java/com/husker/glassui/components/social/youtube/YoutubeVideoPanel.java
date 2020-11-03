package com.husker.glassui.components.social.youtube;

import com.husker.launcher.ui.Screen;
import com.husker.glassui.components.social.ImageSocialPanel;

public class YoutubeVideoPanel extends ImageSocialPanel {

    public int index;
    public String url;

    public YoutubeVideoPanel(Screen screen, int index){
        super(screen);
        this.index = index;
        setIcon(getScreen().getLauncher().Resources.Logo_Youtube);
    }

    public void update(){
        YoutubeVideoInfo info = getScreen().getLauncher().NetManager.Social.getYoutubeVideo(index);
        if(info == null)
            return;

        setTitle(info.getTitle());
        url = info.getUrl();

        setImage(info.getPreview());
    }

    public void onClick() {
        getScreen().getLauncher().NetManager.openLink(url);
    }

    public static YoutubeVideoPanel create(Screen screen, int index){
        return new YoutubeVideoPanel(screen, index);
    }
}
