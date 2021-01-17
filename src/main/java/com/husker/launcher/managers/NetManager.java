package com.husker.launcher.managers;

import com.husker.launcher.Launcher;
import com.husker.launcher.api.API;
import com.husker.launcher.settings.LauncherConfig;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class NetManager {

    private static ArrayList<NetManager.ServerStatus> statusList = new ArrayList<>();

    static {
        new Timer().schedule(new TimerTask() {
            public void run() {
                statusList = new ArrayList<>(getServerOnlineStatus());
            }
        }, 0, 12000);
    }

    public static boolean isInternetOnline(){
        return statusList.contains(ServerStatus.INTERNET_ONLINE);
    }

    public static boolean isAuthOnline(){
        return statusList.contains(ServerStatus.AUTH_ONLINE);
    }

    public static boolean isMinecraftOnline(){
        return statusList.contains(ServerStatus.MINECRAFT_SERVER_ONLINE);
    }

    public static void updateStatusLabel(Launcher launcher, JLabel label){
        if(label == null)
            return;
        if(!launcher.isVisible())
            return;

        Color red = new Color(160, 0, 0);
        Color yellow = new Color(140, 140, 0);
        Color green = new Color(0, 160, 0);

        if(!isInternetOnline()){
            label.setText("Нет интернета");
            label.setForeground(yellow);
            return;
        }
        if(isAuthOnline() && isMinecraftOnline()){
            label.setText("Онлайн");
            label.setForeground(green);
            return;
        }
        if(!isAuthOnline() && isMinecraftOnline()){
            label.setText("Авторизация недоступна");
            label.setForeground(yellow);
            return;
        }
        if(isAuthOnline()){
            label.setText("Доступна авторизация");
            label.setForeground(yellow);
            return;
        }
        label.setText("Офлайн");
        label.setForeground(red);
    }

    public enum ServerStatus{
        INTERNET_ONLINE,
        AUTH_ONLINE,
        MINECRAFT_SERVER_ONLINE,
    }

    public static List<ServerStatus> getServerOnlineStatus(){
        ArrayList<ServerStatus> status = new ArrayList<>();
        try {
            if(ping(LauncherConfig.getAuthIp(), LauncherConfig.getAuthPort(), 3000))
                status.add(ServerStatus.AUTH_ONLINE);
        }catch (Exception ignored){
        }
        try{
            API.Minecraft.ServerInfo info = API.Minecraft.getServerInfo();
            if(ping(info.getIP(), info.getPort(), 3000))
                status.add(ServerStatus.MINECRAFT_SERVER_ONLINE);
        }catch (Exception ignored){
        }
        try{
            if(InetAddress.getByName("google.com").isReachable(3000))
                status.add(ServerStatus.INTERNET_ONLINE);
        }catch (Exception ignored){
        }
        return status;
    }

    public static boolean ping(String ip, int port, int timeout){
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

    public static void openLink(String url){
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
            readVarInt(input);
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

    public static class ProgressHttpEntityWrapper extends HttpEntityWrapper {

        private final ProgressCallback progressCallback;

        public interface ProgressCallback {
            public void progress(float progress);
        }

        public ProgressHttpEntityWrapper(final HttpEntity entity, final ProgressCallback progressCallback) {
            super(entity);
            this.progressCallback = progressCallback;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            this.wrappedEntity.writeTo(out instanceof ProgressFilterOutputStream ? out : new ProgressFilterOutputStream(out, this.progressCallback, getContentLength()));
        }

        static class ProgressFilterOutputStream extends FilterOutputStream {

            private final ProgressCallback progressCallback;
            private long transferred;
            private long totalBytes;

            ProgressFilterOutputStream(final OutputStream out, final ProgressCallback progressCallback, final long totalBytes) {
                super(out);
                this.progressCallback = progressCallback;
                this.transferred = 0;
                this.totalBytes = totalBytes;
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                //super.write(byte b[], int off, int len) calls write(int b)
                out.write(b, off, len);
                this.transferred += len;
                this.progressCallback.progress(getCurrentProgress());
            }

            @Override
            public void write(final int b) throws IOException {
                out.write(b);
                this.transferred++;
                this.progressCallback.progress(getCurrentProgress());
            }

            private float getCurrentProgress() {
                return ((float) this.transferred / this.totalBytes) * 100;
            }

        }

    }




}
