package com.husker.launcher.plugin.core;

import org.bukkit.GameMode;


public class PlayerRole {

    private String group = "";
    private GameMode gameMode = null;
    private boolean canPickup = true;
    private boolean canFight = true;
    private boolean canBuild = true;
    private boolean isAdmin = false;

    public boolean isCanPickupItems() {
        return canPickup;
    }

    public void setCanPickup(boolean canPickup) {
        this.canPickup = canPickup;
    }

    public boolean isCanFight() {
        return canFight;
    }

    public void setCanFight(boolean canFight) {
        this.canFight = canFight;
    }

    public PlayerRole setGroup(String group){
        this.group = group;
        return this;
    }

    public PlayerRole setGameMode(GameMode gameMode){
        this.gameMode = gameMode;
        return this;
    }

    public String getGroup(){
        return group;
    }

    public GameMode getGameMode(){
        return gameMode;
    }

    public PlayerRole clone(){
        try {
            return (PlayerRole) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean isCanBuild() {
        return canBuild;
    }

    public void setCanBuild(boolean caBuild) {
        this.canBuild = caBuild;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
