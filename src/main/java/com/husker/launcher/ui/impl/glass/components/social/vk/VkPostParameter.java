package com.husker.launcher.ui.impl.glass.components.social.vk;

import java.awt.image.BufferedImage;

public class VkPostParameter {

    private final String text;
    private final String url;

    public VkPostParameter(String text, String url){
        this.text = text;
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }


    public static class Picture extends VkPostParameter{

        private final BufferedImage img;

        public Picture(String text, String url, BufferedImage img) {
            super(text, url);
            this.img = img;
        }

        public BufferedImage getImage() {
            return img;
        }
    }

    public static class Video extends Picture{

        public Video(String text, String url, BufferedImage img) {
            super(text, url, img);
        }
    }

    public static class Youtube extends Video{

        public Youtube(String text, String url, BufferedImage img) {
            super(text, url, img);
        }
    }



    public static class Snippet extends VkPostParameter{

        private final BufferedImage img;
        private final String snippetTitle;
        private final String snippetAuthor;

        public Snippet(String text, String url, BufferedImage img, String snippetTitle, String snippetAuthor) {
            super(text, url);
            this.img = img;
            this.snippetTitle = snippetTitle;
            this.snippetAuthor = snippetAuthor;
        }

        public String getSnippetTitle() {
            return snippetTitle;
        }

        public BufferedImage getImage() {
            return img;
        }

        public String getSnippetAuthor() {
            return snippetAuthor;
        }
    }
}
