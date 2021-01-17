package com.husker.launcher.plugin.impl;

import com.husker.launcher.plugin.AAuthPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class AAuthConfig {

    private static final FileConfiguration config;

    static {
        config = AAuthPlugin.instance.getConfig();
        config.addDefault("enabled", true);
        config.addDefault("ip", "127.0.0.1");
        config.addDefault("port", 15565);
        config.addDefault("backup_timer", 3600000 * 2); // 2 Hour
        config.addDefault("backup_live_time", 86400000 * 2); // 2 days
        config.addDefault("auth_message", "You can't join to the server! Please restart the game.");
        config.options().copyDefaults(true);
        AAuthPlugin.instance.saveConfig();
    }

    public static boolean isEnabled(){
        return config.getBoolean("enabled");
    }

    public static String getIP(){
        return config.getString("ip");
    }

    public static int getPort(){
        return config.getInt("port");
    }

    public static String getAuthErrorMessage(){
        return config.getString("auth_message");
    }

    public static long getBackupTime(){
        return config.getLong("backup_timer");
    }

    public static long getBackupLiveTime(){
        return config.getLong("backup_live_time");
    }
}
