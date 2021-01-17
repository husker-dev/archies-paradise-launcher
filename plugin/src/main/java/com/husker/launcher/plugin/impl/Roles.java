package com.husker.launcher.plugin.impl;

import com.husker.launcher.plugin.core.PlayerRole;
import org.bukkit.GameMode;

import java.util.HashMap;

public class Roles{

    public static final HashMap<String, PlayerRole> roles = new HashMap<>();

    public static void init(){
        roles.put("Модератор", new PlayerRole(){{
            setGroup("player_with_admin");
            setAdmin(true);
        }});
        roles.put("Игрок", new PlayerRole(){{
            setGameMode(GameMode.SURVIVAL);
            setGroup("player");
        }});
        roles.put("Гость", new PlayerRole(){{
            setGameMode(GameMode.SURVIVAL);
            setGroup("default");
            setCanFight(false);
            setCanPickup(false);
            setCanBuild(false);
        }});
        roles.put("Администратор", new PlayerRole(){{
            setGroup("player_with_admin");
            setAdmin(true);
            setGameMode(GameMode.SURVIVAL);
        }});
    }

    public static PlayerRole getRole(String name){
        return roles.getOrDefault(name, null);
    }

    public boolean containsRole(String name){
        return roles.containsKey(name);
    }

}
