package com.husker.launcher.server.services.http;

import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;

public class ApiClass {

    private HttpExchange exchange;

    public void setExchange(HttpExchange exchange){
        this.exchange = exchange;
    }

    public HttpExchange getExchange(){
        return exchange;
    }

    public boolean containsAttribute(String attribute){
        return getAttributeMap().containsKey(attribute);
    }

    public HashMap<String, String> getAttributeMap(){
        HashMap<String, String> result = new HashMap<>();

        if(exchange.getRequestURI().getQuery() != null) {
            for (String param : exchange.getRequestURI().getQuery().split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1)
                    result.put(entry[0], entry[1]);
                else
                    result.put(entry[0], "");
            }
        }
        return result;
    }

    public String getAttribute(String attribute){
        if(!containsAttribute(attribute))
            throw new AttributeNotFoundException(attribute);
        try {
            return getAttributeMap().get(attribute);
        }catch (Exception ex){
            throw new AttributeNotFoundException(attribute);
        }
    }
}
