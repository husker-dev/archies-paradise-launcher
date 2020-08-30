package com.husker.launcher.utils.settings;

import com.husker.launcher.Resources;
import com.husker.launcher.utils.ConsoleUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsFile implements SettingsContainer{
    private String path;
    private boolean isSaveEnabled = true;

    private TreeElement rootElement;

    public SettingsFile(String resourcePath){
        resourcePath = "/" + resourcePath;
        ConsoleUtils.printDebug(SettingsFile.class, "Reading in-jar config file: " + resourcePath);

        InputStream inputStream = Resources.class.getResourceAsStream(resourcePath);

        if(inputStream != null) {
            init(new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList()));
            isSaveEnabled = false;
        }else{
            ConsoleUtils.printResult("ERROR");
            throw new RuntimeException("Can't read in-jar config file: " + resourcePath);
        }
        ConsoleUtils.printResult("OK");
    }

    public SettingsFile(File file){
        ConsoleUtils.printDebug(SettingsFile.class, "Reading config file: " + file.getAbsolutePath());

        try {
            if(!file.exists())
                file.createNewFile();

            init(Files.readAllLines(Paths.get(file.getAbsolutePath())));
            path = file.getAbsolutePath();
        }catch (Exception ex){
            ConsoleUtils.printResult("ERROR");
        }
        ConsoleUtils.printResult("OK");
    }

    private void init(List<String> lines){
        rootElement = new TreeElement("root", lines);
    }

    private TreeElement getTreeElement(String path){
        String[] split = path.split("\\.");
        TreeElement currentElement = rootElement;
        for (int i = 0; i < split.length - 1; i++)
            currentElement = (TreeElement) currentElement.getValue(split[i]);

        return currentElement;
    }

    public SettingsContainer getParentSettingsContainer() {
        return null;
    }

    public String getTitle() {
        return "<root>";
    }

    public String get(String path){
        if(path.contains(".")){
            try {
                String[] split = path.split("\\.");
                return getTreeElement(path).getValue(split[split.length - 1]).toString();
            }catch (Exception ex){
                return null;
            }
        }else {
            if(rootElement.containsValue(path))
                return rootElement.getValue(path).toString();
            else
                return null;
        }
    }

    public String get(String parameter, String defaultValue){
        String value = get(parameter);
        return value == null ? defaultValue : value;
    }

    public void set(String path, String value){
        if(!isSaveEnabled)
            throw new RuntimeException("Saving is unable!");

        TreeElement element = getTreeElement(path);
        element.setValue(path.split("\\.")[path.split("\\.").length - 1], value);
        save();
    }

    public void save(){
        try {
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(toString());
            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDefault(String parameter, String value){
        if(get(parameter) == null)
            set(parameter, value);
    }

    public String toString(){
        return rootElement.toString(0);
    }

    public static class TreeElement {
        public static final String SEPARATOR = ":";

        private final String title;
        private final LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        public TreeElement(String title, List<String> lines){
            this.title = title;

            for(int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains(SEPARATOR)) {
                    if (line.contains("{")) {
                        String new_title = line.split(SEPARATOR)[0];
                        ArrayList<String> new_lines = new ArrayList<>();

                        int grapes = 1;
                        i++;
                        while(true) {
                            line = lines.get(i).replaceAll("\\s", "");
                            if(line.contains(":{"))
                                grapes++;
                            if(line.equals("}")) {
                                grapes--;
                                if(grapes == 0)
                                    break;
                            }
                            new_lines.add(line);
                            i++;
                        }
                        parameters.put(new_title, new TreeElement(new_title, new_lines));
                    } else
                        parameters.put(line.split(SEPARATOR)[0], line.substring(line.split(SEPARATOR)[0].length() + 1).trim());
                }
            }
        }

        public String toString(){
            return toString(0);
        }

        public String toString(int tab){
            StringBuilder out = new StringBuilder();
            if(tab > 0)
                out.append("{").append(System.lineSeparator());

            for(Map.Entry<String, Object> entry : parameters.entrySet()) {
                out.append(createTab(tab)).append(entry.getKey()).append(SEPARATOR).append(" ");

                Object o = entry.getValue();
                if(o instanceof TreeElement){
                    out.append(((TreeElement)o).toString(tab + 1));
                }else
                    out.append(o.toString());
                out.append(System.lineSeparator());
            }

            if(tab > 0)
                out.append(createTab(tab - 1)).append("}");
            return out.toString();
        }

        public String getTitle(){
            return title;
        }

        public Object getValue(String parameter){
            return parameters.get(parameter);
        }

        public Object setValue(String parameter, String value){
            return parameters.put(parameter, value);
        }

        public boolean containsValue(String parameter){
            return parameters.containsKey(parameter);
        }


        private String createTab(int count){
            StringBuilder out = new StringBuilder();
            for(int i = 0; i < count; i++)
                out.append("\t");
            return out.toString();
        }
    }
}
