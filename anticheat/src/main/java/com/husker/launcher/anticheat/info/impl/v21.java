package com.husker.launcher.anticheat.info.impl;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class v21 extends v18 {

    public v21(File clientFolder) {
        super(clientFolder);
    }

    public String getJVMArguments() {
        return formatArguments(getValues(getJSON().getJSONObject("arguments").getJSONArray("jvm"))).replace("Windows 10", "Windows_10").trim();
    }

    public String getGameArguments() {
        return formatArguments(getValues(getJSON().getJSONObject("arguments").getJSONArray("game"))).trim();
    }

    public String getValues(JSONArray arguments){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < arguments.length(); i++){
            JSONObject argument = arguments.getJSONObject(i);

            if (argument.has("rules") && !acceptRule(argument.getJSONArray("rules")))
                continue;

            JSONArray values = argument.getJSONArray("values");
            for(int r = 0; r < values.length(); r++)
                builder.append(values.getString(r)).append(" ");
        }

        return builder.toString();
    }
}
