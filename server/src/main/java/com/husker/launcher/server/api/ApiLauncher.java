package com.husker.launcher.server.api;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

public class ApiLauncher extends ApiClass {

    public JSONObject getVersion(){
        return SimpleJSON.create("version", ServerMain.Settings.getLauncherVersion());
    }
}
