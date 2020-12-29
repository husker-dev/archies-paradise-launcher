package com.husker.launcher.discord;



import com.husker.launcher.api.API;
import com.husker.launcher.discord.discordipc.IPCClient;
import com.husker.launcher.discord.discordipc.IPCListener;
import com.husker.launcher.discord.discordipc.entities.RichPresence;
import com.husker.launcher.discord.discordipc.exceptions.NoDiscordClientException;
import com.husker.launcher.managers.NetManager;
import com.husker.launcher.settings.LauncherConfig;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;

import static com.husker.launcher.discord.discordipc.entities.pipe.PipeStatus.CONNECTED;


public class Discord {

    public static class Texts{
        public static final String Loading = "Loading...";
        public static final String InMainMenu = "In Main Menu";
        public static final String InLogin = "Login in account";
        public static final String InSkins = "Changing Skin";
        public static final String InRegistration = "Register an account";
        public static final String InGame = "In Game";
    }

    public static final Logger log = LogManager.getLogger(Discord.class);

    private static String state = "Do nothing...";
    private static JSONObject serverInfo;
    private static IPCClient client;

    private static OffsetDateTime time;

    public static void setState(String state){
        Discord.state = state;
    }

    public static void init(){
        time = OffsetDateTime.now();
        client = new IPCClient(788855948826378271L);
        client.setListener(new IPCListener(){
            public void onReady(IPCClient client) {
                startMinecraftServerTimer();
                startDiscordTimer();
            }
        });
        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            e.printStackTrace();
        }
    }

    private static void startDiscordTimer(){
        new Timer().schedule(new TimerTask() {
            public void run() {
                try {
                    if(client.getStatus() != CONNECTED)
                        return;
                    RichPresence.Builder builder = new RichPresence.Builder();
                    builder.setState("Players");
                    builder.setDetails(state);
                    builder.setStartTimestamp(time);
                    builder.setLargeImage("icon_1024", LauncherConfig.getTitle());
                    try {
                        int online = serverInfo.getJSONObject("players").getInt("online");
                        int max = serverInfo.getJSONObject("players").getInt("max");
                        if(online == 0)
                            throw new RuntimeException("Empty");
                        builder.setParty("Online", online, max);
                    }catch (Exception ex){
                        builder.setState("Server is empty");
                    }

                    client.sendRichPresence(builder.build());
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    private static void startMinecraftServerTimer(){
        new Timer().schedule(new TimerTask() {
            public void run() {
                try {
                    if(client.getStatus() != CONNECTED)
                        return;
                    API.Minecraft.ServerInfo info = API.Minecraft.getServerInfo();
                    serverInfo = NetManager.MinecraftServer.info(info.getIP(), info.getPort());
                }catch (Exception ex){
                    serverInfo = new JSONObject()
                        .put("online", 0)
                        .put("max", 0);
                }
            }
        }, 0, 5000);
    }
}
