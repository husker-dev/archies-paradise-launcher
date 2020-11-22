package com.husker.launcher.managers;

import com.husker.launcher.managers.social.InstPhotoInfo;
import com.husker.launcher.managers.social.VkPostInfo;
import com.husker.launcher.managers.social.YoutubeVideoInfo;
import com.husker.launcher.Launcher;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.MinecraftStarter;
import com.husker.net.Get;
import com.husker.net.UrlBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class NetManager {

    public static boolean enable = true;

    public static final int DEFAULT_AUTH_SERVER_PORT = 15565;



    public ArrayList<NetManager.ServerStatus> statusList = new ArrayList<>();
    private final Launcher launcher;

    public NetManager(Launcher launcher){
        this.launcher = launcher;
        new Thread(() -> {
            try {
                if(enable) {
                    while(true) {
                        if (launcher != null) {
                            statusList = new ArrayList<>(getServerOnlineStatus());
                            Thread.sleep(launcher.getConfig().Net.Minecraft.getTimeout() + launcher.getConfig().Net.Internet.getTimeout() + launcher.getConfig().Net.Auth.getTimeout() + 3000);
                        } else
                            Thread.sleep(200);
                    }
                }
            } catch (InterruptedException e) {
            }
        }).start();
    }

    public void updateStatusLabel(JLabel label){
        if(label == null)
            return;

        Color red = new Color(160, 0, 0);
        Color yellow = new Color(140, 140, 0);
        Color green = new Color(0, 160, 0);

        if(statusList.contains(NetManager.ServerStatus.INTERNET_OFFLINE)){
            label.setText("Нет интернета");
            label.setForeground(yellow);
        }else if(statusList.contains(NetManager.ServerStatus.AUTH_ONLINE) && statusList.contains(NetManager.ServerStatus.MINECRAFT_SERVER_ONLINE)){
            label.setText("Онлайн");
            label.setForeground(green);
        } else if(!statusList.contains(NetManager.ServerStatus.AUTH_ONLINE) && statusList.contains(NetManager.ServerStatus.MINECRAFT_SERVER_ONLINE)){
            label.setText("Авторизация недоступна");
            label.setForeground(yellow);
        }else if(statusList.contains(NetManager.ServerStatus.AUTH_ONLINE) && !statusList.contains(NetManager.ServerStatus.MINECRAFT_SERVER_ONLINE)){
            label.setText("Доступна авторизация");
            label.setForeground(yellow);
        }else{
            label.setText("Офлайн");
            label.setForeground(red);
        }
    }

    public enum ServerStatus{
        INTERNET_OFFLINE,
        INTERNET_ONLINE,
        AUTH_OFFLINE,
        AUTH_ONLINE,
        MINECRAFT_SERVER_OFFLINE,
        MINECRAFT_SERVER_ONLINE,
    }

    public List<ServerStatus> getServerOnlineStatus(){
        ArrayList<ServerStatus> status = new ArrayList<>();
        try {
            if(ping(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort(), launcher.getConfig().Net.Auth.getTimeout()))
                status.add(ServerStatus.AUTH_ONLINE);
            else
                status.add(ServerStatus.AUTH_OFFLINE);
        }catch (Exception ex){
            status.add(ServerStatus.AUTH_OFFLINE);
        }
        try{
            if(ping(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort(), launcher.getConfig().Net.Auth.getTimeout()))
                status.add(ServerStatus.MINECRAFT_SERVER_ONLINE);
            else
                status.add(ServerStatus.MINECRAFT_SERVER_OFFLINE);
        }catch (Exception ex){
            ex.printStackTrace();
            status.add(ServerStatus.MINECRAFT_SERVER_OFFLINE);
        }
        try{
            if(InetAddress.getByName(launcher.getConfig().Net.Internet.getIp()).isReachable(launcher.getConfig().Net.Internet.getTimeout()))
                status.add(ServerStatus.INTERNET_ONLINE);
            else
                status.add(ServerStatus.INTERNET_OFFLINE);
        }catch (Exception ex){
            status.add(ServerStatus.INTERNET_OFFLINE);
        }
        return status;
    }

    public boolean ping(String ip, int port, int timeout){
        boolean out = false;
        try{
            Socket client = new Socket();
            client.connect(new InetSocketAddress(ip, port), timeout);
            out = client.isConnected();
            client.close();
        }catch (Exception ignored){
        }
        return out;
    }

    public void openLink(String url){
        if(Desktop.isDesktopSupported()){
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ignored) { }
        }else{
            try {
                Runtime.getRuntime().exec("xdg-open " + url);
            } catch (Exception ignored) {
            }
        }
    }

    public String getURLContent(String url) throws IOException {
        if(url == null)
            return null;

        Get get = new Get(url);
        get.execute();
        return get.getHtmlContent();
    }

    public static class MinecraftServer{

        public static JSONObject info(String ip, int port) throws IOException {
            InetSocketAddress host = new InetSocketAddress(ip, port);
            Socket socket = new Socket();
            socket.connect(host, 10000);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            byte [] handshakeMessage = createHandshakeMessage(ip, port);

            // C->S : Handshake State=1
            // send packet length and packet
            writeVarInt(output, handshakeMessage.length);
            output.write(handshakeMessage);

            // C->S : Request
            output.writeByte(0x01); //size is only 1
            output.writeByte(0x00); //packet id for ping

            // S->C : Response
            int size = readVarInt(input);
            int packetId = readVarInt(input);

            if (packetId == -1)
                throw new IOException("Premature end of stream.");

            if (packetId != 0x00)
                throw new IOException("Invalid packetID");
            int length = readVarInt(input); //length of json string

            if (length == -1)
                throw new IOException("Premature end of stream.");

            if (length == 0)
                throw new IOException("Invalid string length.");

            byte[] in = new byte[length];
            input.readFully(in);  //read json string
            String json = new String(in);

            // C->S : Ping
            long now = System.currentTimeMillis();
            output.writeByte(0x09); //size of packet
            output.writeByte(0x01); //0x01 for ping
            output.writeLong(now); //time!?

            // S->C : Pong
            long pingTime = readVarInt(input);
            packetId = readVarInt(input);
            if (packetId == -1)
                throw new IOException("Premature end of stream.");

            if (packetId != 0x01)
                throw new IOException("Invalid packetID");

            input.readLong(); //read response

            JSONObject object = new JSONObject(json);
            object.put("ping", pingTime);

            return object;
        }

        public static byte [] createHandshakeMessage(String host, int port) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            DataOutputStream handshake = new DataOutputStream(buffer);
            handshake.writeByte(0x00); //packet id for handshake
            writeVarInt(handshake, 4); //protocol version
            writeString(handshake, host, StandardCharsets.UTF_8);
            handshake.writeShort(port); //port
            writeVarInt(handshake, 1); //state (1 for handshake)

            return buffer.toByteArray();
        }

        public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
            byte [] bytes = string.getBytes(charset);
            writeVarInt(out, bytes.length);
            out.write(bytes);
        }

        public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
            while (true) {
                if ((paramInt & 0xFFFFFF80) == 0) {
                    out.writeByte(paramInt);
                    return;
                }

                out.writeByte(paramInt & 0x7F | 0x80);
                paramInt >>>= 7;
            }
        }

        public static int readVarInt(DataInputStream in) throws IOException {
            int i = 0;
            int j = 0;
            while (true) {
                int k = in.readByte();
                i |= (k & 0x7F) << j++ * 7;
                if (j > 5) throw new RuntimeException("VarInt too big");
                if ((k & 0x80) != 128) break;
            }
            return i;
        }
    }




}
