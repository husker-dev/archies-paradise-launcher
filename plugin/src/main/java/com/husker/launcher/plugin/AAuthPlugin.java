package com.husker.launcher.plugin;


import com.husker.launcher.plugin.impl.*;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import org.bukkit.plugin.java.JavaPlugin;


public class AAuthPlugin extends JavaPlugin {

    public static AAuthPlugin instance;
    public static LuckPerms luckPerms;

    public void onEnable() {
        try {
            luckPerms = LuckPermsProvider.get();
        }catch (Exception ex){
            throw new NullPointerException("AAuth require LuckPerms for working");
        }
        instance = this;

        Backup.init();
        Roles.init();
        Auth.init();
        Commands.init();
        Events.init();
    }

}
