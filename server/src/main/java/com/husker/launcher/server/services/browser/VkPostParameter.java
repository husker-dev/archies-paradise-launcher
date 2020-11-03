package com.husker.launcher.server.services.browser;

import com.husker.launcher.server.GetRequest;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VkPostParameter {

    protected final String text;
    protected final String url;
    protected String type = "text";

    protected String image;

    public VkPostParameter(String text, String url){
        this.text = text;
        this.url = url;
    }

    public JSONObject getJSON(String... fields) {
        return new JSONObject(new HashMap<String, String>(){{
            List<String> list = Arrays.asList(fields);
            if(list.contains("type"))
                put("type", type);
            if(list.contains("text"))
                put("text", text);
            if(list.contains("url"))
                put("url", url);
            if(list.contains("image"))
                put("image", image);
        }});
    }

    public void setImage(BufferedImage image){
        setImage(GetRequest.toBase64(image));
    }

    public void setImage(String image){
        this.image = image;
    }

    public void setType(String type){
        this.type = type;
    }


    public static class Picture extends VkPostParameter{

        public Picture(String text, String url, BufferedImage img) {
            super(text, url);
            setImage(img);
            setType("picture");
        }
    }

    public static class Video extends Picture{

        public Video(String text, String url, BufferedImage img) {
            super(text, url, img);
            setType("video");
        }
    }

    public static class Youtube extends Video{

        public Youtube(String text, String url, BufferedImage img) {
            super(text, url, img);
            setType("youtube");
        }
    }

    public static class Snippet extends VkPostParameter{

        private final String snippetTitle;
        private final String snippetAuthor;

        public Snippet(String text, String url, BufferedImage img, String snippetTitle, String snippetAuthor) {
            super(text, url);
            setType("snippet");
            setImage(img);

            this.snippetTitle = snippetTitle;
            this.snippetAuthor = snippetAuthor;
        }

        public JSONObject getJSON() {
            JSONObject object = super.getJSON();
            object.put("title", snippetTitle);
            object.put("author", snippetAuthor);
            return object;
        }
    }
}
