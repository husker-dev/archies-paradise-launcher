package com.husker.launcher.managers.social;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class VkPostInfo {

    private final String text;
    private final String url;
    private final int index;
    private final String previewUrl;
    private BufferedImage preview;

    public VkPostInfo(int index, String text, String previewUrl, String url){
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

    public static class Picture extends VkPostInfo {

        public Picture(int index, String text, String previewUrl, String url) {
            super(index, text, previewUrl, url);
        }
    }

    public static class Video extends Picture{

        public Video(int index, String text, String previewUrl, String url) {
            super(index, text, previewUrl, url);
        }
    }

    public static class Snippet extends VkPostInfo {

        private final String snippetTitle;
        private final String snippetAuthor;

        public Snippet(int index, String text, String previewUrl, String url, String snippetTitle, String snippetAuthor) {
            super(index, text, previewUrl, url);
            this.snippetTitle = snippetTitle;
            this.snippetAuthor = snippetAuthor;
        }

        public String getSnippetTitle() {
            return snippetTitle;
        }

        public String getSnippetAuthor() {
            return snippetAuthor;
        }
    }
}
