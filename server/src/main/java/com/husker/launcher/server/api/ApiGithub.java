package com.husker.launcher.server.api;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

public class ApiGithub extends ApiClients{

    public JSONObject getInfo(){
        return SimpleJSON.create("repo", ServerMain.Settings.getGitHubId());
    }

    public void setInfo(Profile profile){
        profile.checkStatus("Администратор");
        ServerMain.Settings.setGitHubId(getAttribute("repo"));
    }
}
