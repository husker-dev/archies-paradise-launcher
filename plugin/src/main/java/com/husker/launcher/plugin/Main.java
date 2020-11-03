package com.husker.launcher.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;


public class Main extends JavaPlugin {

    private Socket socket;
    private final FileConfiguration config = this.getConfig();

    public void onEnable() {
        config.addDefault("ip", "127.0.0.1");
        config.addDefault("port", "15565");
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerLogin(PlayerLoginEvent e) {
                Player tryingToLogin = e.getPlayer();

                getLogger().info("Checking user: " + e.getPlayer().getDisplayName());

                try {
                    if (checkUser(tryingToLogin.getName(), e.getAddress().getHostAddress()))
                        e.allow();
                    else
                        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You can't join the server! Please restart game.");
                }catch (Exception ex){
                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Can't connect to auth server! Try again later.");
                    ex.printStackTrace();
                }
            }
        }, this);
    }


    public void onDisable() {
    }

    public boolean checkUser(String uuid, String ip) throws IOException {
        Socket socket = new Socket(config.get("ip"), Integer.parseInt(config.get("port")));

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        out.println("{\"request\":\"check_ip\",\"ip\":\"" + ip + "\",\"name\":\"" + uuid + "\"}\n");

        String input = in.readLine();

        socket.close();
        socket = null;
        return input.contains("\"allow\":true");
    }
}
