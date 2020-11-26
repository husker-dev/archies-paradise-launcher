package com.husker.launcher.managers;

import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;

public class ProfileApiMethod extends ApiMethod {

    public static ProfileApiMethod create(String methodName, String accessKey){
        return new ProfileApiMethod(methodName, accessKey);
    }

    public ProfileApiMethod(String methodName, String accessKey){
        super(methodName);
        set(API.ACCESS_TOKEN, accessKey);
    }
}
