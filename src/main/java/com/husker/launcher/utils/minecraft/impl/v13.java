package com.husker.launcher.utils.minecraft.impl;

import com.husker.launcher.utils.minecraft.MinecraftClientInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class v13 extends MinecraftClientInfo {

    public v13(File clientFolder) {
        super(clientFolder);
    }

    public String getVersion() {
        if(getJSON().has("jar"))
            return getJSON().getString("jar");
        else if(getJSON().has("inheritsFrom"))
            return getJSON().getString("inheritsFrom");
        else
            return getJSON().getString("id");
    }

    public String getAssetsId() {
        if(getJSON().has("assets"))
            return getJSON().getString("assets");
        else if(getJSON().has("assetIndex"))
            return getJSON().getJSONObject("assetIndex").getString("id");
        else
            return null;
    }

    public String getGameArguments() {
        GameArgumentsParameters parameters = getGameParameters();
        String arguments = formatArguments(getJSON().getString("minecraftArguments"));

        if(parameters.hasCustomResolution()){
            arguments += " --width " + parameters.getResolutionWidth();
            arguments += " --height " + parameters.getResolutionHeight();
        }
        if(parameters.isFullscreen())
            arguments += " --fullscreen";
        return arguments.trim();
    }

    public String getJVMArguments() {
        JvmArgumentsParameters parameters = getJvmParameters();
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("-Djava.library.path", getVersionFolder().getAbsolutePath() + "\\natives");
            put("-Dminecraft.launcher.brand", parameters.getLauncherName());
            put("-Dminecraft.launcher.version", parameters.getLauncherVersion());
            if(isWindows())
                put("-XX:HeapDumpPath", "MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }};
        StringBuilder builder = new StringBuilder();
        map.forEach((key, val) -> builder.append(" ").append(key).append("=\"").append(val).append("\""));
        builder.append(" -Xmx").append(parameters.getRam()).append("m");
        builder.append(" -classpath \"").append(String.join(";", getLibraries())).append("\" ");
        builder.append(getMainClass());
        return builder.toString().trim();
    }

    public String[] getLibraries() {
        ArrayList<String> libraries = new ArrayList<>();

        JSONArray array = getJSON().getJSONArray("libraries");
        for(int i = 0; i < array.length(); i++){
            JSONObject instance = array.getJSONObject(i);

            // Если есть условия, их их не проходит
            if(instance.has("rules") && !acceptRule(instance.getJSONArray("rules")))
                continue;

            if(instance.has("artifact"))
                libraries.add(instance.getJSONObject("artifact").getString("path"));

            if(instance.has("classifies")){
                if(instance.getJSONObject("classifies").has(getOSName()))
                    libraries.add(instance.getJSONObject("classifies").getJSONObject(getOSName()).getString("path"));
                else if(instance.getJSONObject("classifies").has("natives-" + getOSName()))
                    libraries.add(instance.getJSONObject("classifies").getJSONObject("natives-" + getOSName()).getString("path"));
                else if(getOSName().equals("windows")){
                    if(instance.getJSONObject("classifies").has("windows-" + getArch()))
                        libraries.add(instance.getJSONObject("classifies").getJSONObject("windows-" + getArch()).getString("path"));
                }
            }
        }
        String librariesPath = getClientFolder().getAbsolutePath() + "\\libraries\\";
        ArrayList<String> fullPath = new ArrayList<>();
        libraries.forEach(val -> fullPath.add((librariesPath + val).replace("/", "\\").replace("\\libraries\\libraries", "\\libraries")));
        fullPath.add(getJar().getAbsolutePath());
        return fullPath.toArray(new String[0]);
    }

    public boolean acceptRule(JSONArray rules){
        for(int i = 0; i < rules.length(); i++){
            JSONObject rule = rules.getJSONObject(i);
            boolean allow = rule.getString("action").equals("allow");
            boolean out = true;

            if(rule.has("os")){
                JSONObject os = rule.getJSONObject("os");
                if(os.has("name") && !os.getString("name").equals(getOSName()))
                    out = false;
                if(os.has("version") && !Pattern.compile(os.getString("version")).matcher(System.getProperty("os.version")).lookingAt())
                    out = false;
            }
            if(rule.has("features")){
                JSONObject os = rule.getJSONObject("features");
                if(os.has("has_custom_resolution") && os.getBoolean("has_custom_resolution") != getGameParameters().hasCustomResolution())
                    out = false;
                if(os.has("is_demo_user") && os.getBoolean("is_demo_user"))
                    out = false;
            }

            if(allow != out)
                return false;
        }
        return true;
    }
}
