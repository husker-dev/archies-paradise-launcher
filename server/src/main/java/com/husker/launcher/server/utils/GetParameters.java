package com.husker.launcher.server.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GetParameters extends HashMap<String, String> {
    private final String title;

    public GetParameters(String title, String... parameters){
        this.title = title;

        if(parameters.length % 2 == 1)
            throw new RuntimeException("Bad parameters");

        for(int i = 0; i < parameters.length; i += 2)
            put(parameters[i], parameters[i + 1]);
    }

    public String toString(){
        StringBuilder values = new StringBuilder();

        int index = 0;
        for(Map.Entry<String, String> entry : entrySet()) {
            values.append(entry.getKey()).append("=").append("\"").append(entry.getValue()).append("\"");

            if(index != size() - 1)
                values.append(";");
            index ++;
        }

        return title + "{" + values + "}";
    }

    public String getTitle(){
        return title;
    }

    public static GetParameters create(String text){
        String title = text.split("\\{")[0];
        ArrayList<String> parameters = new ArrayList<>();

        for(String par : text.split("\\{")[1].split("}")[0].split(";")) {
            parameters.add(par.split("=")[0]);
            parameters.add(par.split("\"")[1].split("\"")[0]);
        }

        return new GetParameters(title, parameters.toArray(new String[0]));
    }
}
