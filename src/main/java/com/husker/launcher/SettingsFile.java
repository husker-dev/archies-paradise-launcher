package com.husker.launcher;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsFile {

    public static final String SEPARATOR = ":";
    private final HashMap<String, String> parameters = new HashMap<>();
    private String path;
    private boolean isSaveEnabled = true;

    public SettingsFile(String resourcePath){
        init(new BufferedReader(new InputStreamReader(Resources.get(resourcePath, false))).lines().collect(Collectors.toList()));
        isSaveEnabled = false;
    }


    public SettingsFile(File file){
        try {
            if(!file.exists())
                file.createNewFile();

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
        for(String line : lines) {
            if (line.contains(SEPARATOR)) {
                ArrayList<String> parts = new ArrayList<>(Arrays.asList(line.split(SEPARATOR)));
                parameters.put(
                        parts.remove(0),
                        String.join(":", parts.toArray(new String[0])).trim()
                );
            }
        }
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

    public void setDefault(String parameter, String value){
        if(get(parameter) == null)
            set(parameter, value);
    }

    private String convertToText(){
        StringBuilder text = new StringBuilder();
        for(Map.Entry<String, String> entry : parameters.entrySet())
            text.append(entry.getKey()).append(SEPARATOR).append(entry.getValue()).append("\r\n");
        return text.toString();
    }
}
