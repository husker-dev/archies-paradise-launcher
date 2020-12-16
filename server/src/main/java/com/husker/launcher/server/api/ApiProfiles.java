package com.husker.launcher.server.api;

import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.core.ProfileUtils;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

public class ApiProfiles extends ApiClass {

    public JSONObject isIPBound(){
        return SimpleJSON.create("bound", ProfileUtils.isValidIp(getAttribute("name"), getAttribute("ip")));
    }

    public JSONObject isLoginTaken(){
        return SimpleJSON.create("exist", ProfileUtils.isNicknameExist(getAttribute(Profile.LOGIN)));
    }
}
