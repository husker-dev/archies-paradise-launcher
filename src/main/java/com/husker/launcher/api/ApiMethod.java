package com.husker.launcher.api;


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ApiMethod {

    public static ApiMethod create(String methodName){
        return new ApiMethod(methodName);
    }

    private final String methodName;
    private final HashMap<String, String> parameters = new HashMap<>();

    public ApiMethod(String methodName){
        this.methodName = methodName;
    }

    public ApiMethod set(String parameter, Object value){
        parameters.put(parameter, value.toString());
        return this;
    }

    public String getUrl(){
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(methodName).append("?");
            for (Map.Entry<String, String> entry : parameters.entrySet())
                builder.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
            return builder.substring(0, builder.toString().length() - 1);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}