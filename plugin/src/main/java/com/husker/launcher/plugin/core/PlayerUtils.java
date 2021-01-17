package com.husker.launcher.plugin.core;

import com.husker.launcher.plugin.AAuthPlugin;
import com.husker.launcher.plugin.core.utils.HotbarMessager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PlayerUtils {


    public static void setPlayerGroup(Player player, String group){
        try {
            LuckPerms luckPerms = AAuthPlugin.luckPerms;
            luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
                try {
                    user.data().clear(NodeType.INHERITANCE::matches);
                    Node node = InheritanceNode.builder(luckPerms.getGroupManager().getGroup(group)).build();
                    user.data().add(node);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void setPlayerGameMode(Player player, GameMode mode){
        Bukkit.getScheduler().runTask(AAuthPlugin.instance, () -> {
            try {
                if(player != null && mode != null)
                    player.setGameMode(mode);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    public static void kickPlayer(Player player, String text){
        Bukkit.getScheduler().runTask(AAuthPlugin.instance, () -> {
            try {
                if(player != null && text != null)
                    player.kickPlayer(text);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    public static void sendToChat(Player player, String text){
        Bukkit.getScheduler().runTask(AAuthPlugin.instance, () -> {
            try {
                if(player != null && text != null)
                    player.sendMessage(text);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    public static void setName(Player player, String name) {
        try {
            player.setDisplayName(name);
            player.setPlayerListName(name);
            player.setCustomName(name);
            player.setCustomNameVisible(true);

            Object profile = player.getClass().getMethod("getProfile").invoke(player);
            Field nome = profile.getClass().getDeclaredField("name");
            nome.setAccessible(true);
            nome.set(profile, name);

            for (Player p : Bukkit.getServer().getOnlinePlayers())
                player.hidePlayer(AAuthPlugin.instance, p);
            for (Player p : Bukkit.getServer().getOnlinePlayers())
                player.showPlayer(AAuthPlugin.instance, p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printGuestMessage(Player player){
        try {
            HotbarMessager.sendHotBarMessage(player, "Вы не можете взаимодействовать с миром");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
