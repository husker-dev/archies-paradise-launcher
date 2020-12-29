package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

import java.io.IOException;

public class ApiAuth extends ApiClass {

    public JSONObject getAccessToken(){
        Profile profile = Profile.get(getAttribute("login"), getAttribute("password"));
        if(profile != null)
            return SimpleJSON.create("access_token", profile.Token.create());
        else
            throw new ApiException("Wrong login or password", 1);
    }

    public void create() throws IOException {
        Profile.create(getAttribute(Profile.LOGIN), getAttribute(Profile.PASSWORD));
    }
}
