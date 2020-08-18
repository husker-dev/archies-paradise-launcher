package com.husker.launcher.ui.impl.glass.components.social.vk;

import java.awt.image.BufferedImage;

public class VkPostParameter {

    private final String text;

    public VkPostParameter(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static class Picture extends VkPostParameter{

        private final BufferedImage img;

        public Picture(String text, BufferedImage img) {
            super(text);
            this.img = img;
        }

        public BufferedImage getImage() {
            return img;
        }
    }

    public static class Video extends Picture{

        public Video(String text, BufferedImage img) {
            super(text, img);
        }
    }

    public static class Youtube extends Video{

        public Youtube(String text, BufferedImage img) {
            super(text, img);
        }
    }



    public static class Snippet extends VkPostParameter{

        private final BufferedImage img;
        private final String snippetTitle;
        private final String snippetAuthor;

        public Snippet(String text, BufferedImage img, String snippetTitle, String snippetAuthor) {
            super(text);
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
