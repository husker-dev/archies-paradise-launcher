package com.husker.launcher.plugin.impl;

import com.husker.launcher.plugin.AAuthPlugin;
import com.husker.launcher.plugin.core.API;
import com.husker.launcher.plugin.core.PlayerRole;
import com.husker.launcher.plugin.core.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class Commands {

    public static void init(){
        AAuthPlugin aauth = AAuthPlugin.instance;

        aauth.getCommand("id").setExecutor((commandSender, command, s, strings) -> {
            if (!isAdmin(commandSender))
                return true;

            if(strings.length != 1) {
                commandSender.sendMessage(ChatColor.RED + "Мда... Прочитай инструкцию...");
                return true;
            }

            String id = API.getIdByName(strings[0]);
            if(id != null)
                commandSender.sendMessage(ChatColor.GRAY + "ID игрока " + strings[0] + ": " + ChatColor.YELLOW + id);
            else
                commandSender.sendMessage("Игрок с ником " + strings[0] + " не найден.");
            return true;
        });

        aauth.getCommand("moderatormode").setExecutor((commandSender, command, s, strings) -> {
            if(!(commandSender instanceof Player)){
                commandSender.sendMessage("Console can't execute this command");
                return true;
            }
            Player player = (Player) commandSender;

            if (!isAdmin(commandSender))
                return true;

            List<String> yes = Arrays.asList("yes", "1", "true", "on", "enable");
            List<String> no = Arrays.asList("no", "0", "false", "off", "disable");


            boolean state = true;
            if(strings.length != 0){
                if(yes.contains(strings[0]))
                    state = true;
                else if(no.contains(strings[0]))
                    state = false;
                else {
                    commandSender.sendMessage(ChatColor.RED + "Неизвестный параметр");
                    return true;
                }
            }
            PlayerUtils.setPlayerGroup(player, state ? "admin" : "player_with_admin");
            commandSender.sendMessage(ChatColor.GRAY + "Режим модератора: " + (state ? ChatColor.GREEN + "Вкл" : ChatColor.RED + "Выкл"));
            return true;
        });
        aauth.getCommand("mm").setExecutor(aauth.getCommand("moderatormode").getExecutor());

        aauth.getCommand("backup").setExecutor((commandSender, command, s, strings) -> {
            if (!isAdmin(commandSender))
                return true;
            Backup.doBackup(commandSender);
            return true;
        });

        aauth.getCommand("aaban").setExecutor((commandSender, command, s, strings) -> {
            if (!isAdmin(commandSender))
                return true;
            try{
                API.banIP(Integer.parseInt(strings[0]));
            }catch (Exception ignored){
                API.banIP(Integer.parseInt(Auth.getIdByName(strings[0])));
            }
            Auth.updateRoles(null);
            return true;
        });
    }

    public static boolean isAdmin(CommandSender sender){
        if (sender.isOp() || (sender instanceof Player && Auth.getRole((Player) sender).isAdmin()))
            return true;
        else
            if(sender instanceof Player)
                PlayerUtils.sendToChat(((Player) sender), ChatColor.RED + "У вас нет прав на использование этой команды");
        return false;
    }
}
