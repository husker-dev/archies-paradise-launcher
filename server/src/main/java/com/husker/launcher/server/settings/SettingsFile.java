package com.husker.launcher.server.settings;


import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsFile {

    private Map<?, ?> map;
    private final String path;
    private final Yaml yaml;

    public SettingsFile(String path){
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        yaml = new Yaml(options);

        this.path = path;
        load();
    }

    private void load(){
        try {
            InputStream is;
            if (path.startsWith("/"))
                is = getClass().getResourceAsStream(path);
            else {
                if(!Files.exists(Paths.get(path)))
                    Files.createFile(Paths.get(path));
                is = new FileInputStream(new File(path));
            }

            String text = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            is.close();
            text = text.replace("\t", "  ");
            try {
                map = yaml.load(text);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            if(map == null)
                map = new LinkedHashMap<>();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void save(){
        try {
            if(path.startsWith("/"))
                return;
            if(!Files.exists(Paths.get(path)))
                Files.createFile(Paths.get(path));
            yaml.dump(map, new OutputStreamWriter(new FileOutputStream(new File(path)), StandardCharsets.UTF_8));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setDefault(String varPath, Object value){
        if(!containsVar(varPath))
            set(varPath, value);
    }

    private Map getMapByPath(String path, boolean createIfMiss){
        if(path.isEmpty())
            return map;
        String[] pathElements = path.split("\\.");
        Map currentLevel = map;
        for(String pathElement : pathElements){
            if(!currentLevel.containsKey(pathElement))
                if(createIfMiss)
                    currentLevel.put(pathElement, new HashMap<String, String>());
                else
                    return null;
            currentLevel = (Map<?, ?>)currentLevel.get(pathElement);
        }
        return currentLevel;
    }

    public boolean containsMap(String path){
        return getMapByPath(path, false) != null;
    }

    public boolean containsVar(String varPath){
        String var = getVarFromVarPath(varPath);
        String path = getPathFromVarPath(varPath);
        if(!containsMap(path))
            return false;
        return getMapByPath(path, false).containsKey(var);
    }

    public boolean getBoolean(String varPath){
        return Boolean.parseBoolean(get(varPath));
    }

    public int getInt(String varPath){
        return Integer.parseInt(get(varPath));
    }

    public String get(String varPath){
        load();
        if(!containsVar(varPath))
            return null;
        return getMapByPath(getPathFromVarPath(varPath), false).get(getVarFromVarPath(varPath)).toString();
    }

    public void set(String varPath, Object value){
        if(path.startsWith("/"))
            return;
        getMapByPath(getPathFromVarPath(varPath), true).put(getVarFromVarPath(varPath), value);
        save();
    }

    private String getVarFromVarPath(String varPath){
        if(varPath.contains("."))
            return varPath.substring(varPath.lastIndexOf(".") + 1);
        else
            return varPath;
    }

    private String getPathFromVarPath(String varPath){
        if(varPath.contains("."))
            return varPath.substring(0, varPath.lastIndexOf("."));
        else
            return "";
    }
}
