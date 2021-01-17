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
    private final boolean internal;

    public SettingsFile(String path){
        this(path, false);
    }

    public SettingsFile(String path, boolean internal){
        this.internal = internal;
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        yaml = new Yaml(options);

        this.path = path;
        reload();
    }

    private void reload(){
        InputStream is = null;
        try {
            if (internal)
                is = getClass().getResourceAsStream(path);
            else {
                Files.createDirectories(Paths.get(path).getParent());
                File file = new File(path);
                if(!file.exists())
                    file.createNewFile();
                is = new FileInputStream(file);
            }

            String text = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            text = text.replace("\t", "  ");
            try {
                map = yaml.load(text);
            }catch (Exception ignored){ }
            if(map == null)
                map = new LinkedHashMap<>();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if(is != null) {
            try {
                is.close();
            } catch (IOException ignored) {}
        }
    }

    private void save(){
        try {
            if(internal)
                return;
            if(!Files.exists(Paths.get(path)))
                Files.createFile(Paths.get(path));
            FileOutputStream os = new FileOutputStream(new File(path));
            yaml.dump(map, new OutputStreamWriter(os, StandardCharsets.UTF_8));
            os.close();
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
        if(!containsVar(varPath))
            return null;
        return getMapByPath(getPathFromVarPath(varPath), false).get(getVarFromVarPath(varPath)).toString();
    }

    public void set(String varPath, Object value){
        if(value == null)
            value = "";
        if(internal)
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
