package com.husker.launcher.discord;



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
        public static final String InMainMenu = "In main menu";
        public static final String InLogin = "Login in account";
        public static final String InSkins = "Changing skin";
        public static final String InRegistration = "Register an account";
        public static final String InGame = "In game";
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
                    builder.setParty("Online", serverInfo.getJSONObject("players").getInt("online"), serverInfo.getJSONObject("players").getInt("max"));

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
                    serverInfo = NetManager.MinecraftServer.info("mc.hypixel.net", 25565);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }, 0, 5000);
    }
}
