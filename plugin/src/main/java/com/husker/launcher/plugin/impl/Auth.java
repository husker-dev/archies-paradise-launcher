package com.husker.launcher.plugin.impl;

import com.husker.launcher.plugin.core.API;
import com.husker.launcher.plugin.core.CoolTimer;
import com.husker.launcher.plugin.core.PlayerRole;
import com.husker.launcher.plugin.core.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.husker.launcher.plugin.core.PlayerUtils.kickPlayer;

public class Auth {

    public static CoolTimer refreshTimer;

    // Name -> Id
    private static final HashMap<String, String> ids = new HashMap<>();

    // Id -> Role
    private static HashMap<String, PlayerRole> cachedRoles = new HashMap<>();

    public static void init(){
        refreshTimer = new CoolTimer(Auth::updateRoles, 2 * 1000);
    }

    public static void auth(Player player, String ip) throws API.BadAuthException {
        String name = API.authUser(player.getName(), ip);
        ids.put(name, player.getName());
        PlayerUtils.setName(player, name);
    }

    public static void logout(Player player){
        ids.remove(getIdByName(player.getName()));
    }

    public static String getIdByName(String name){
        if(ids.containsKey(name))
            return ids.get(name);
        else
            return API.getIdByName(name);
    }

    public static void updateRoles(){
        updateRoles(null);
    }

    public static void updateRoles(Player joinedPlayer){
        refreshTimer.reset();
        try {
            if(Bukkit.getOnlinePlayers().size() == 0){
                cachedRoles = new HashMap<>();
                return;
            }
            ArrayList<String> onlineIds = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> onlineIds.add(getIdByName(player.getName())));
            JSONObject statuses = API.getStatuses(onlineIds.toArray(new String[0]));

            HashMap<String, PlayerRole> newCached = new HashMap<>();
            for(Player player : Bukkit.getOnlinePlayers()){
                String id = getIdByName(player.getName());

                PlayerRole role = null;
                if(statuses.has(id))
                    role = Roles.getRole(statuses.getString(id));

                if(role == null){
                    kickPlayer(player, "Невозможно определить ваш статус O_o");
                    continue;
                }

                if(!cachedRoles.containsKey(id) || cachedRoles.get(id) != role){
                    PlayerUtils.setPlayerGroup(player, role.getGroup());
                    if(joinedPlayer != player)
                        PlayerUtils.sendToChat(player, ChatColor.GRAY + "Ваш новый статус: " + ChatColor.LIGHT_PURPLE + statuses.getString(id));
                    if(role.getGameMode() != null)
                        PlayerUtils.setPlayerGameMode(player, role.getGameMode());
                }

                newCached.put(id, role);
            }
            cachedRoles = newCached;
        } catch (ConnectException ignored){

        } catch (Exception ex){
            ex.printStackTrace();
        }
        //AAuthPlugin.instance.getLogger().info("Roles updated!");
    }

    public static PlayerRole getRole(Player player){
        return cachedRoles.get(getIdByName(player.getName()));
    }
}
