package com.husker.launcher.settings;


import java.io.File;

public class LauncherConfig extends SettingsFile{

    public static final LauncherConfig INSTANCE = new LauncherConfig();

    private static final String TITLE = "title";
    private static final String FOLDER = "folder";
    private static final String AUTH_IP = "auth.ip";
    private static final String AUTH_PORT = "auth.port";

    private LauncherConfig(){
        super("/launcher_config.yaml", true);
        setDefault(TITLE, "Archie's Paradise Launcher");
        setDefault(FOLDER, "Archie's Paradise");
        setDefault(AUTH_IP, "127.0.0.1");
        setDefault(AUTH_PORT, 25565);
    }

    public static String getFolderName(){
        return INSTANCE.get(FOLDER);
    }

    public static String getTitle(){
        return INSTANCE.get(TITLE);
    }

    public static String getAuthIp(){
        return INSTANCE.get(AUTH_IP);
    }

    public static int getAuthPort(){
        return INSTANCE.getInt(AUTH_PORT);
    }
}
