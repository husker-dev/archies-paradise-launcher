package com.husker.launcher.server.services.http;

import org.json.JSONObject;

public class SimpleJSON extends JSONObject {

    public static SimpleJSON create(){
        return new SimpleJSON();
    }

    public static SimpleJSON create(String key, Object value){
        return new SimpleJSON(key, value);
    }

    public SimpleJSON(){}

    public SimpleJSON(String key, Object value){
        put(key, value);
    }

    public SimpleJSON put(String key, Object value){
        super.put(key, value);
        return this;
    }
}
