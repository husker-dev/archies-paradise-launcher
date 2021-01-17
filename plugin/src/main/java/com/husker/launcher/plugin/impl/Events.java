package com.husker.launcher.plugin.impl;

import com.husker.launcher.plugin.AAuthPlugin;
import com.husker.launcher.plugin.core.API;
import com.husker.launcher.plugin.core.EventUtils;
import com.husker.launcher.plugin.core.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

public class Events {

    public static void init(){
        EventUtils utils = new EventUtils(AAuthPlugin.instance);

        utils.onPlayerJoinEvent(event -> Auth.updateRoles(event.getPlayer()));
        utils.onPlayerQuitEvent(event -> Auth.logout(event.getPlayer()));

        utils.onPlayerLoginEvent(event -> {
            if(!AAuthConfig.isEnabled()){
                event.allow();
                return;
            }
            try {
                Auth.auth(event.getPlayer(), event.getAddress().getHostName());
                event.allow();
            }catch (API.BadAuthException badAuthException) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, AAuthConfig.getAuthErrorMessage());
            }
        });

        utils.onEntityPickupItemEvent(event -> {
            if(event.getEntity() instanceof Player){
                if(!Auth.getRole((Player) event.getEntity()).isCanPickupItems()) {
                    PlayerUtils.printGuestMessage((Player)event.getEntity());
                    event.setCancelled(true);
                }
            }
        });

        utils.onEntityDamageByEntityEvent(event -> {
            if(event.getDamager() instanceof Player){
                if(!Auth.getRole((Player) event.getDamager()).isCanFight()) {
                    PlayerUtils.printGuestMessage((Player) event.getDamager());
                    event.setCancelled(true);
                }
            }
        });

        utils.onPlayerInteractEvent(event -> {
            if(!Auth.getRole(event.getPlayer()).isCanBuild()) {
                PlayerUtils.printGuestMessage(event.getPlayer());
                event.setCancelled(true);
            }
        });
    }
}
