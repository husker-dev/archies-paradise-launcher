package com.husker.launcher.server.services.browser;

import com.husker.launcher.server.GetRequest;
import org.json.JSONObject;

import java.awt.image.BufferedImage;

public class YoutubeVideoParameters {
    private final BufferedImage image;
    private final String title;
    private final String url;
    private final long date;

    public YoutubeVideoParameters(BufferedImage image, String title, String url, long date){
        this.image = image;
        this.title = title;
        this.url = url;
        this.date = date;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public long getDate() {
        return date;
    }

    public JSONObject getJSON(){
        JSONObject object = new JSONObject();
        object.put("title", title);
        object.put("url", url);
        object.put("date", date);
        object.put("image", GetRequest.toBase64(image));
        return object;
    }
}