package com.husker.launcher.social;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class YouTubeVideoInfo {

    private final String title;
    private final String url;
    private final String previewUrl;
    private BufferedImage preview;

    private final int index;

    public YouTubeVideoInfo(int index, String title, String previewUrl, String url){
        this.title = title;
        this.url = url;
        this.previewUrl = previewUrl;
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
        if(preview == null) {
            try {
                preview = ImageIO.read(new URL(previewUrl));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return preview;
    }
}
