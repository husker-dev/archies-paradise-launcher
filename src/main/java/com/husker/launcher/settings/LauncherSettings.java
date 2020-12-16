package com.husker.launcher.settings;

import com.husker.launcher.Launcher;

public class LauncherSettings extends SettingsFile {

    public static final LauncherSettings INSTANCE = new LauncherSettings();

    private static final String AUTO_AUTH = "auto_auth";
    private static final String BACKGROUND = "background";
    private static final String WINDOWED = "windowed";
    private static final String CLIENT_TYPE = "client_type";
    private static final String RAM = "ram";

    private LauncherSettings() {
        super(Launcher.getSettingsFolder() + "/launcher.yaml");

        setDefault(AUTO_AUTH, true);
        setDefault(BACKGROUND, 1);
        setDefault(WINDOWED, false);
        setDefault(CLIENT_TYPE, "any");
        setDefault(RAM, 256);
    }

    public static int getBackgroundIndex(){
        return INSTANCE.getInt(BACKGROUND);
    }

    public static void setBackgroundIndex(int index){
        INSTANCE.set(BACKGROUND, index);
    }

    public static boolean isAutoAuth(){
        return INSTANCE.getBoolean(AUTO_AUTH);
    }

    public static void setAutoAuth(boolean autoAuth){
        INSTANCE.set(AUTO_AUTH, autoAuth);
    }

    public static boolean isWindowed(){
        return INSTANCE.getBoolean(WINDOWED);
    }

    public static void setWindowed(boolean windowed){
        INSTANCE.set(WINDOWED, windowed);
    }

    public static int getRAM(){
        return INSTANCE.getInt(RAM);
    }

    public static void setRAM(int ram){
        INSTANCE.set(RAM, ram);
    }

    public static void setRAM(String ram){
        try{
            Integer.parseInt(ram);
            INSTANCE.set(RAM, ram);
        }catch (Exception ex){
            INSTANCE.set(RAM, 256);
        }
    }

    public static String getClientType(){
        return INSTANCE.get(CLIENT_TYPE);
    }

    public static void setClientType(String clientType){
        INSTANCE.set(CLIENT_TYPE, clientType);
    }
}
