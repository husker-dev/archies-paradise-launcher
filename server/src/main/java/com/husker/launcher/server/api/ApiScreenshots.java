package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Screenshots;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.ImageLink;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

import java.io.IOException;

public class ApiScreenshots extends ApiClass {

    public JSONObject getCount(){
        return SimpleJSON.create("count", Screenshots.getCount());
    }

    public ImageLink get() throws IOException {
        int index = Integer.parseInt(getAttribute("index"));
        String type = containsAttribute("size") ? getAttribute("size") : "default";
        ImageLink image;
        switch (type) {
            case "large":
                image = Screenshots.getFull(index);
                break;
            case "small":
                image = Screenshots.getPreview(index);
                break;
            default:
                image = Screenshots.get(index);
                break;
        }
        if(image == null)
            throw new ApiException("Can't find screenshot with index: '" + index + "'", 1);
        return image;
    }
}
