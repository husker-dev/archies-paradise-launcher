package com.husker.launcher.server.api;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

public class ApiMinecraft extends ApiClass {

    public JSONObject getServerInfo(){
        return SimpleJSON.create()
                .put("ip", ServerMain.Settings.getMinecraftServerIP())
                .put("port", ServerMain.Settings.getMinecraftServerPort());
    }
}

