package com.husker.glassui.components.social.impl;

import com.husker.glassui.components.social.ImageSocialPanel;
import com.husker.launcher.Resources;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.social.InstagramPhotoInfo;
import com.husker.launcher.social.Social;
import com.husker.launcher.ui.Screen;

public class InstPhotoPanel extends ImageSocialPanel {

    public int index;
    public String url;

    public InstPhotoPanel(Screen screen, int index){
        super(screen);
        this.index = index;
        setIcon(Resources.Logo_Instagram);
    }

    public void update(){
        InstagramPhotoInfo info = Social.Instagram.getPhoto(index);
        if(info == null)
            return;

        setTitle(info.getText());
        url = info.getUrl();

        new Thread(() -> setImage(info.getPreview())).start();
    }

    public void onClick() {
        NetManager.openLink(url);
    }

    public static InstPhotoPanel create(Screen screen, int index){
        return new InstPhotoPanel(screen, index);
    }
}
