package com.husker.launcher.server.api;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.core.Profile;
import com.husker.launcher.server.services.http.ApiClass;
import com.husker.launcher.server.services.http.ApiException;
import com.husker.launcher.server.services.http.SimpleJSON;
import org.json.JSONObject;

public class ApiInstagram extends ApiClass {

    public JSONObject getInfo(){
        return SimpleJSON.create("id", ServerMain.Settings.getInstagramId());
    }

    public void setInfo(Profile profile){
        if(profile.getDataValue(Profile.STATUS).equals("Администратор"))
            ServerMain.Settings.setInstagramId(getAttribute("id"));
        else
            throw new ApiException("The account has no rights to change this value", 1);
    }
}
