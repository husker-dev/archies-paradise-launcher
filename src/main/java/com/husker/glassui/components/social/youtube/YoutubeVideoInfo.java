package com.husker.glassui.components.social.youtube;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class YoutubeVideoInfo {
    private final String title;
    private final String url;

    private BufferedImage preview;

    private final int index;

    public YoutubeVideoInfo(int index, String title, BufferedImage preview, String url){
        this.title = title;
        this.url = url;
        this.preview = preview;
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }


    public BufferedImage getPreview(){
        return preview;
    }
}
