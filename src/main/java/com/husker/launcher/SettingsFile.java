package com.husker.launcher;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsFile {

    private final HashMap<String, String> parameters = new HashMap<>();
    private String path;
    private boolean isSaveEnabled = true;

    public SettingsFile(String resourcePath){

        init(new BufferedReader(new InputStreamReader(Resources.get(resourcePath, false))).lines().collect(Collectors.toList()));
        isSaveEnabled = false;
    }


    public SettingsFile(File file){
        try {
            init(Files.readAllLines(Paths.get(file.getAbsolutePath())));
            path = file.getAbsolutePath();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public SettingsFile(URL url){
        try {
            init(Files.readAllLines(Paths.get(url.toURI())));
            path = url.getPath();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void init(List<String> lines){
        for(String line : lines)
            parameters.put(line.split("=")[0].trim(), line.split("=")[1].trim());
    }

    public String get(String parameter){
        if(!parameters.containsKey(parameter))
            return null;
        return parameters.get(parameter);
    }

    public String get(String parameter, String defaultValue){
        String value = get(parameter);
        return value == null ? defaultValue : value;
    }

    public void set(String parameter, String value){
        if(!isSaveEnabled)
            throw new RuntimeException("Saving is unable!");

        parameters.put(parameter, value);

        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(convertToText());
            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertToText(){
        StringBuilder text = new StringBuilder();
        for(Map.Entry<String, String> entry : parameters.entrySet())
            text.append(entry.getKey()).append("=").append(entry.getValue()).append("\r\n");
        return text.toString();
    }
}
