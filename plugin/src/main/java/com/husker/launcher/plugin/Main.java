package com.husker.launcher.plugin;


import com.husker.net.Get;
import com.husker.net.HttpUrlBuilder;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Main extends JavaPlugin {

    private final FileConfiguration config = this.getConfig();
    private final HashMap<String, String> ids = new HashMap<>();
    private CoolTimer timer;

    private static LuckPerms luckPerms;

    private static final HashMap<String, PlayerRole> roles = new HashMap<String, PlayerRole>(){{
        put("Модератор", new PlayerRole(){{
            addAccessedGroups("admin");
            setGroup("player_with_admin");
        }});
        put("Игрок", new PlayerRole(){{
            setGameMode(GameMode.SURVIVAL);
            setGroup("player");
        }});
        put("Гость", new PlayerRole(){{
            setGameMode(GameMode.ADVENTURE);
            setGroup("default");
        }});
        put("Администратор", get("Модератор").setGameMode(GameMode.SURVIVAL).clone());
    }};

    private HashMap<String, PlayerRole> cachedStates = new HashMap<>();

    public void onEnable() {
        try {
            luckPerms = LuckPermsProvider.get();
        }catch (Exception ex){
            getLogger().info("AAuth require LuckPerms for working");
        }

        config.addDefault("enabled", true);
        config.addDefault("ip", "127.0.0.1");
        config.addDefault("port", 15565);
        config.addDefault("auth_message", "You can't join to the server! Please restart the game.");
        config.options().copyDefaults(true);
        saveConfig();

        timer = new CoolTimer(() -> updatePlayers(false), 2 * 1000);

        getCommand("id").setExecutor((commandSender, command, s, strings) -> {
            try {
                Get get = new Get(new HttpUrlBuilder(getMethodUrl("profiles.getId")).set("name", strings[0]));
                JSONObject json = new JSONObject(get.getHtmlContent());
                if(json.has("id")){
                    commandSender.sendMessage(ChatColor.GRAY + "ID игрока " + strings[0] + ": " + ChatColor.YELLOW + json.getInt("id"));
                    return true;
                }
            }catch (Exception ignored){}
            commandSender.sendMessage("Игрок с ником " + strings[0] + " не найден.");
            return true;
        });

        CommandExecutor moderatorMode = (commandSender, command, s, strings) -> {
            if(!(commandSender instanceof Player)){
                commandSender.sendMessage("Консоль не может выполнить эту команду");
                return true;
            }

            List<String> yes = Arrays.asList("yes", "1", "true", "on", "enable");
            List<String> no = Arrays.asList("no", "0", "false", "off", "disable");
            Player player = (Player) commandSender;

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
            setPlayerGroup(player, state ? "admin" : "player_with_admin");
            commandSender.sendMessage(ChatColor.GRAY + "Режим модератора: " + (state ? ChatColor.GREEN + "Вкл" : ChatColor.RED + "Выкл"));
            return true;
        };
        getCommand("moderatormode").setExecutor(moderatorMode);
        getCommand("mm").setExecutor(moderatorMode);

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerJoin(PlayerJoinEvent e) {
                updatePlayers(true);
            }
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerLogin(PlayerLoginEvent e) {
                if(!config.getBoolean("enabled")){
                    e.allow();
                    return;
                }
                try {
                    Player player = e.getPlayer();
                    String realName = authUser(player.getName(), e.getAddress().getHostName());
                    ids.put(realName, player.getName());
                    setName(player, realName);

                    for (Player p : Bukkit.getServer().getOnlinePlayers())
                        player.hidePlayer(Main.this, p);
                    for (Player p : Bukkit.getServer().getOnlinePlayers())
                        player.showPlayer(Main.this, p);

                    e.allow();
                }catch (BadAuthException badAuthException) {
                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.getString("error_message"));
                }
            }
        }, this);
    }

    private void setName(Player player, String name) {
        try {
            player.setDisplayName(name);
            player.setPlayerListName(name);
            player.setCustomName(name);
            player.setCustomNameVisible(true);

            Object profile = player.getClass().getMethod("getProfile").invoke(player);
            Field nome = profile.getClass().getDeclaredField("name");
            nome.setAccessible(true);
            nome.set(profile, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String authUser(String id, String ip) throws BadAuthException{
        try{
            Get get = new Get(new HttpUrlBuilder(getMethodUrl("profiles.isIPBound")).set("id", id).set("ip", ip));
            JSONObject json = new JSONObject(get.getHtmlContent());
            getLogger().info(json.toString());
            if(json.has("bound") && json.getBoolean("bound"))
                return json.getString("name");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        throw new BadAuthException();
    }

    private String getMethodUrl(String methodName){
        return "http://" + config.getString("ip") + ":" + config.getInt("port") + "/api/method/" + methodName;
    }

    private static class UnsupportedCoreException extends Exception { }
    private static class BadAuthException extends Exception { }

    private void updatePlayers(boolean onLogin){
        timer.reset();
        try {
            StringBuilder playersIds = new StringBuilder();
            Bukkit.getOnlinePlayers().forEach(player -> playersIds.append(",").append(ids.get(player.getName())));
            if(Bukkit.getOnlinePlayers().size() == 0){
                cachedStates = new HashMap<>();
                return;
            }

            Get get = new Get(new HttpUrlBuilder(getMethodUrl("profiles.getStatuses")).set("ids", playersIds.substring(1)));
            JSONObject json = new JSONObject(get.getHtmlContent());
            JSONObject statuses = json.getJSONObject("statuses");

            HashMap<String, PlayerRole> newCached = new HashMap<>();
            for(Player player : Bukkit.getOnlinePlayers()){
                String id = ids.get(player.getName());

                PlayerRole role = null;
                if(statuses.has(id))
                    role = roles.getOrDefault(statuses.getString(id), null);

                if(role == null){
                    kickPlayer(player, "Невозможно определить ваш статус O_o");
                    continue;
                }

                if(!cachedStates.containsKey(id) || cachedStates.get(id) != role){
                    setPlayerGroup(player, role.group);
                    if(!onLogin)
                        sendToChat(player, ChatColor.GRAY + "Ваш новый статус: " + ChatColor.LIGHT_PURPLE + statuses.getString(id));
                    if(role.gameMode != null)
                        setPlayerGameMode(player, role.gameMode);
                }

                newCached.put(id, role);
            }
            cachedStates = newCached;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void setPlayerGroup(Player player, String group){
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().clear(NodeType.INHERITANCE::matches);
            Node node = InheritanceNode.builder(luckPerms.getGroupManager().getGroup(group)).build();
            user.data().add(node);
        });
    }

    private void setPlayerGameMode(Player player, GameMode mode){
        Bukkit.getScheduler().runTask(this, () -> player.setGameMode(mode));
    }

    private void kickPlayer(Player player, String text){
        Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(text));
    }

    private void sendToChat(Player player, String text){
        Bukkit.getScheduler().runTask(this, () -> player.sendMessage(text));
    }

    private static boolean isPlayerInGroup(Player player, String group) {
        return player.hasPermission("group." + group);
    }

    private static class PlayerRole implements Cloneable{

        private String group = "";
        private final List<String> accessedGroups = new ArrayList<>();
        private GameMode gameMode = null;

        public PlayerRole setGroup(String group){
            this.group = group;
            return this;
        }

        public PlayerRole setGameMode(GameMode gameMode){
            this.gameMode = gameMode;
            return this;
        }

        public PlayerRole addAccessedGroups(String... groups){
            accessedGroups.addAll(Arrays.asList(groups));
            return this;
        }

        public String[] getAllGroups(){
            ArrayList<String> all = new ArrayList<>();
            all.add(group);
            all.addAll(accessedGroups);
            return all.toArray(new String[0]);
        }

        protected PlayerRole clone(){
            try {
                return (PlayerRole) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

}
