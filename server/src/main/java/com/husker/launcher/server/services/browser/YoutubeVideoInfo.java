package com.husker.launcher.server.services.browser;

import com.husker.launcher.server.GetRequest;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class YoutubeVideoInfo {
    private final String image;
    private final String title;
    private final String url;

    public YoutubeVideoInfo(BufferedImage image, String title, String url){
        this.image = GetRequest.toBase64(image);
        this.title = title;
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public JSONObject getJSON(String... fields){
        return new JSONObject(new HashMap<String, Object>(){{
            List<String> list = Arrays.asList(fields);
            if(list.contains("title"))
                put("title", title);
            if(list.contains("url"))
                put("url", url);
            if(list.contains("image"))
                put("image", image);
        }});
    }

}
