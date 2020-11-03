package com.husker.launcher.managers.net;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlBuilder implements CharSequence {

    private final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
    private final String domain;

    public UrlBuilder(String domain){
        if(domain.startsWith("https://"))
            domain = domain.replace("https://", "");
        if(domain.startsWith("http://"))
            domain = domain.replace("http://", "");
        this.domain = domain;
    }

    public UrlBuilder set(String key, Object value){
        parameters.put(key, value + "");
        return this;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("https://").append(domain).append((!domain.contains("&") && parameters.size() > 0) ? "?" : "");

        for(Map.Entry<String, String> entry : parameters.entrySet()){
            if(builder.toString().charAt(builder.toString().length() - 1) != '?')
                builder.append("&");
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return builder.toString();
    }

    public int length() {
        return toString().length();
    }

    public char charAt(int index) {
        return toString().charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }
}
