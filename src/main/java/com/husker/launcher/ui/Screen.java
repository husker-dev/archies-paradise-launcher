package com.husker.launcher.ui;

import com.husker.launcher.Launcher;
import com.husker.launcher.ui.blur.BlurPainter;
import com.husker.launcher.ui.blur.BlurParameter;
import com.husker.launcher.ui.blur.BlurSegment;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public abstract class Screen extends JPanel {

    private static final Logger log = LogManager.getLogger(Screen.class);

    private LauncherUI launcherUI;
    private final ArrayList<BlurPainter> blurPainters = new ArrayList<>();
    private Parameters parameters = new Parameters();

    public Screen(){
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
    }

    public abstract void onInit();

    public void onShow(){
    }

    public void addBlurSegment(String name, BlurSegment segment){
        blurPainters.add(new BlurPainter(getLauncher(), segment, name));
    }

    public void removeBlurSegment(BlurSegment segment){
        BlurPainter toRemove = null;
        for(BlurPainter painter : blurPainters) {
            if (painter.getBlurSegment() == segment) {
                toRemove = painter;
                break;
            }
        }
        if(toRemove != null)
            blurPainters.remove(toRemove);

        log.info("Blur segment was removed");
    }

    public void removeBlurSegment(BlurParameter parameter){
        removeBlurSegment(parameter.getBlurSegment());
    }

    public BlurPainter[] getBlurPainters(){
        return blurPainters.toArray(new BlurPainter[0]);
    }

    public LauncherUI getLauncherUI() {
        return launcherUI;
    }

    public void setLauncherUI(LauncherUI launcherUI) {
        this.launcherUI = launcherUI;
    }

    public Launcher getLauncher(){
        return getLauncherUI().getLauncher();
    }

    public Parameters getParameters() {
        return parameters;
    }

    public String getParameterValue(String value) {
        return getParameters().get(value).toString();
    }

    public String getParameterValue(String value, String default_value) {
        if(!getParameters().containsKey(value))
            return default_value;
        return getParameterValue(value);
    }

    public Object getParameter(String value) {
        return getParameters().get(value);
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public static class Parameters extends LinkedHashMap<String, Object> {

        public Parameters(){

        }

        public Parameters(String... values){
            for(int i = 0; i < values.length; i+=2)
                put(values[i], values[i + 1]);
        }

        public Parameters(String text){
            while(!text.isEmpty()){
                String name = readNextValueName(text);
                text = text.substring(name.length() + 1);

                String value = readNextValue(text);
                text = text.substring(value.length());

                if(text.startsWith(","))
                    text = text.substring(1);

                if(value.startsWith("{") && value.endsWith("}"))
                    put(name, new Parameters(value.substring(1, value.length() - 1)));
                else
                    put(name, value);
            }
        }

        private String readNextValueName(String text){
            return text.substring(0, text.indexOf('='));
        }

        private String readNextValue(String text){
            if(!text.contains(","))
                return text;

            for(String s : text.split(","))
                if(getCharCount(s, '{') == getCharCount(s, '}'))
                    return s;
            return "null";
        }

        private int getCharCount(String text, char ch){
            int count = 0;
            for(int i = 0; i < text.length(); i++)
                if(text.charAt(i) == ch)
                    count ++;
            return count;
        }

        public String toString(){
            StringBuilder out = new StringBuilder();

            int i = 0;
            for(Map.Entry<String, Object> entry : entrySet()) {
                if(entry.getValue() instanceof Parameters)
                    out.append(entry.getKey()).append("={").append(entry.getValue()).append("}");
                else
                    out.append(entry.getKey()).append("=").append(entry.getValue());
                if(i < size() - 1)
                    out.append(",");
                i++;
            }
            return out.toString();
        }
    }
}
