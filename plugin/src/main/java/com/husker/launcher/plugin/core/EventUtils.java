package com.husker.launcher.plugin.core;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;


public class EventUtils {

    private final JavaPlugin plugin;

    public EventUtils(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public PluginManager getPluginManager(){
        return plugin.getServer().getPluginManager();
    }

    private <T extends Event> void registerEvent(Consumer<T> event, EventPriority priority){
        if(priority == EventPriority.HIGHEST)
            getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.HIGHEST)
                public void event(T e) {
                    event.accept(e);
                }
            }, plugin);
        if(priority == EventPriority.HIGH)
            getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.HIGH)
                public void event(T e) {
                    event.accept(e);
                }
            }, plugin);
        if(priority == EventPriority.NORMAL)
            getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.NORMAL)
                public void event(T e) {
                    event.accept(e);
                }
            }, plugin);
        if(priority == EventPriority.LOW)
            getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.LOW)
                public void event(T e) {
                    event.accept(e);
                }
            }, plugin);
        if(priority == EventPriority.LOWEST)
            getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.LOWEST)
                public void event(T e) {
                    event.accept(e);
                }
            }, plugin);
        if(priority == EventPriority.MONITOR)
            getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.MONITOR)
                public void event(T e) {
                    event.accept(e);
                }
            }, plugin);
    }

    public void onPlayerJoinEvent(Consumer<PlayerJoinEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void event(PlayerJoinEvent e) {
                event.accept(e);
            }
        }, plugin);
    }

    public void onPlayerLoginEvent(Consumer<PlayerLoginEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void event(PlayerLoginEvent e) {
                event.accept(e);
            }
        }, plugin);
    }

    public void onPlayerQuitEvent(Consumer<PlayerQuitEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void event(PlayerQuitEvent e) {
                event.accept(e);
            }
        }, plugin);
    }

    public void onEntityPickupItemEvent(Consumer<EntityPickupItemEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void event(EntityPickupItemEvent e) {
                event.accept(e);
            }
        }, plugin);
    }

    public void onEntityDamageEvent(Consumer<EntityDamageEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void event(EntityDamageEvent e) {
                event.accept(e);
            }
        }, plugin);
    }

    public void onEntityDamageByEntityEvent(Consumer<EntityDamageByEntityEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void event(EntityDamageByEntityEvent e) {
                event.accept(e);
            }
        }, plugin);
    }

    public void onPlayerInteractEvent(Consumer<PlayerInteractEvent> event){
        getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void event(PlayerInteractEvent  e) {
                event.accept(e);
            }
        }, plugin);
    }
}
