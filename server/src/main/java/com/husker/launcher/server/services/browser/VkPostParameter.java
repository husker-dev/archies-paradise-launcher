package com.husker.launcher.server.services.browser;

import com.husker.launcher.server.GetRequest;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class VkPostParameter {

    protected final String text;
    protected final String url;

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

    public JSONObject getJSON() {
        return new JSONObject(new HashMap<String, String>(){{
            put("type", "default");
            put("text", text);
            put("url", url);
        }});
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

        public JSONObject getJSON() {
            JSONObject object = super.getJSON();
            object.put("type", "picture");
            object.put("image", GetRequest.toBase64(img));
            return object;
        }
    }

    public static class Video extends Picture{

        public Video(String text, String url, BufferedImage img) {
            super(text, url, img);
        }

        public JSONObject getJSON() {
            JSONObject object = super.getJSON();
            object.put("type", "video");
            return object;
        }

    }

    public static class Youtube extends Video{

        public Youtube(String text, String url, BufferedImage img) {
            super(text, url, img);
        }

        public JSONObject getJSON() {
            JSONObject object = super.getJSON();
            object.put("type", "youtube");
            return object;
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

        public JSONObject getJSON() {
            JSONObject object = super.getJSON();
            object.put("type", "snippet");
            object.put("title", snippetTitle);
            object.put("author", snippetAuthor);
            return object;
        }
    }
}
