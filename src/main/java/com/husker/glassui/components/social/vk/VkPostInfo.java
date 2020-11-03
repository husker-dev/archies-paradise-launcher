package com.husker.glassui.components.social.vk;

import java.awt.image.BufferedImage;

public class VkPostInfo {

    private final String text;
    private final String url;
    private final int index;
    private final BufferedImage preview;

    public VkPostInfo(int index, String text, BufferedImage preview, String url){
        this.text = text;
        this.url = url;
        this.index = index;
        this.preview = preview;
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
        return preview;
    }

    public static class Picture extends VkPostInfo {

        public Picture(int index, String text, BufferedImage preview, String url) {
            super(index, text, preview, url);
        }
    }

    public static class Video extends Picture{

        public Video(int index, String text, BufferedImage preview, String url) {
            super(index, text, preview, url);
        }
    }

    public static class Snippet extends VkPostInfo {

        private final String snippetTitle;
        private final String snippetAuthor;

        public Snippet(int index, String text, BufferedImage preview, String url, String snippetTitle, String snippetAuthor) {
            super(index, text, preview, url);
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
