package com.husker.launcher.server.api;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;


public class ApiAbout extends ApiClass {

    public JSONObject getOwnerInfo(){
        return SimpleJSON.create()
                .put("name", ServerMain.Settings.getOwnerName())
                .put("url", ServerMain.Settings.getOwnerUrl());
    }

    public JSONObject getSupportInfo(){
        return SimpleJSON.create()
                .put("name", ServerMain.Settings.getSupportName())
                .put("url", ServerMain.Settings.getSupportUrl());
    }
}
