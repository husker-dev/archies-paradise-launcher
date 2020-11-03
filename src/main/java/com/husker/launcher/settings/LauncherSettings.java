package com.husker.launcher.settings;

import com.husker.launcher.Launcher;
import com.husker.launcher.utils.settings.SettingsFile;

import java.io.File;

public class LauncherSettings extends SettingsFile {

    public LauncherSettings(Launcher launcher) {
        super(new File(launcher.getSettingsFolder() + "/launcher.cfg"));

        setDefault("auto_auth", "true");
        setDefault("background", "1");
        setDefault("windowed", "false");
        setDefault("ram", "256");
    }

    public int getBackgroundIndex(){
        try {
            return Integer.parseInt(get("background", "1"));
        }catch (Exception ex){
            return 1;
        }
    }

    public void setBackgroundIndex(int index){
        set("background", index + "");
    }

    public boolean isAutoAuth(){
        return get("auto_auth", "false").equals("true");
    }

    public void setAutoAuth(boolean autoAuth){
        set("auto_auth", autoAuth + "");
    }

    public boolean isWindowed(){
        return get("windowed").equals("true");
    }

    public void setWindowed(boolean windowed){
       set("windowed", windowed + "");
    }

    public int getRAM(){
        try{
            return Integer.parseInt(get("ram"));
        }catch (Exception ex){
            return 256;
        }
    }

    public void setRAM(int ram){
        set("ram", ram + "");
    }

    public void setRAM(String ram){
        try{
            Integer.parseInt(ram);
            set("ram", ram + "");
        }catch (Exception ex){
            set("ram", "256");
        }
    }
}
