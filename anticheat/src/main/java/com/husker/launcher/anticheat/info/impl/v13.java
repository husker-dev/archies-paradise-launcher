package com.husker.launcher.anticheat.info.impl;


import com.husker.launcher.anticheat.system.SystemUtils;
import com.husker.launcher.anticheat.info.MinecraftClientInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
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
            put("-Djava.library.path", getVersionFolder().getAbsolutePath() + File.separator + "natives");
            put("-Dminecraft.launcher.brand", parameters.getLauncherName());
            put("-Dminecraft.launcher.version", parameters.getLauncherVersion());
            if(SystemUtils.isWindows())
                put("-XX:HeapDumpPath", "MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }};
        StringBuilder builder = new StringBuilder();
        map.forEach((key, val) -> builder.append(" ").append(key).append("=\"").append(val).append("\""));
        builder.append(" -Xmx").append(parameters.getRam()).append("M");
        builder.append(" -classpath \"").append(String.join(File.pathSeparator, getLibraries())).append("\" ");
        builder.append("-XX:+UseG1GC -Xmx").append(parameters.getRam()).append("M -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M ");
        if(getType() == ClientType.FORGE)
            builder.append("-Dfml.ignoreInvalidMinecraftCertificates=true -Dfml.ignorePatchDiscrepancies=true ");

        builder.append(getMainClass());
        return builder.toString().trim();
    }

    public String[] getLibraries() {
        ArrayList<String> libraries = new ArrayList<>(getAdditionLibraries());

        JSONArray array = getJSON().getJSONArray("libraries");
        for(int i = 0; i < array.length(); i++){
            JSONObject instance = array.getJSONObject(i);

            // Если есть условия, их их не проходит
            if(instance.has("rules") && !acceptRule(instance.getJSONArray("rules")))
                continue;

            if(instance.has("artifact"))
                libraries.add(instance.getJSONObject("artifact").getString("path"));
            else if(instance.has("name")){
                try {
                    String name = instance.getString("name");
                    String path = name.split(":")[0].replace(".", "/") + "/";
                    path += name.split(":")[1] + "/";
                    path += name.split(":")[2];

                    File file = Objects.requireNonNull(new File(getClientFolder() + "/libraries/" + path).listFiles((dir, name1) -> name1.endsWith(".jar")))[0];
                    path += "/" + file.getName();
                    libraries.add(path);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            if(instance.has("classifies")){
                if(instance.getJSONObject("classifies").has(SystemUtils.getOSName()))
                    libraries.add(instance.getJSONObject("classifies").getJSONObject(SystemUtils.getOSName()).getString("path"));
                else if(instance.getJSONObject("classifies").has("natives-" + SystemUtils.getOSName()))
                    libraries.add(instance.getJSONObject("classifies").getJSONObject("natives-" + SystemUtils.getOSName()).getString("path"));
                else if(SystemUtils.getOSName().equals("windows")){
                    if(instance.getJSONObject("classifies").has("windows-" + SystemUtils.getArch()))
                        libraries.add(instance.getJSONObject("classifies").getJSONObject("windows-" + SystemUtils.getArch()).getString("path"));
                }
            }
        }
        String librariesPath = getClientFolder().getAbsolutePath() + "\\libraries\\";
        ArrayList<String> fullPath = new ArrayList<>();
        libraries.forEach(val -> fullPath.add((librariesPath + val).replace("/", "\\").replace("\\libraries\\libraries", "\\libraries")));
        fullPath.add(getJar().getAbsolutePath());

        for(int i = 0; i < fullPath.size(); i++)
            fullPath.set(i, SystemUtils.fixPath(fullPath.get(i)));
        return fullPath.toArray(new String[0]);
    }

    public boolean acceptRule(JSONArray rules){
        for(int i = 0; i < rules.length(); i++){
            JSONObject rule = rules.getJSONObject(i);
            boolean allow = rule.getString("action").equals("allow");
            boolean out = true;

            if(rule.has("os")){
                JSONObject os = rule.getJSONObject("os");
                if(os.has("name") && !os.getString("name").equals(SystemUtils.getOSName()))
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
