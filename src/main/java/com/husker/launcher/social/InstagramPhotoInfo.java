package com.husker.launcher.social;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class InstagramPhotoInfo {

    private final String text;
    private final String url;
    private final int index;
    private final String previewUrl;
    private BufferedImage preview;

    public InstagramPhotoInfo(int index, String text, String previewUrl, String url){
        this.text = text;
        this.url = url;
        this.index = index;
        this.previewUrl = previewUrl;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public int getIndex(){
        return index;
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
