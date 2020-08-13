package com.husker.launcher;

import com.alee.laf.label.WebLabel;

import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetManager {

    public ArrayList<NetManager.ServerStatus> statusList = new ArrayList<>();

    public NetManager(LauncherWindow launcher){
        new Thread(() -> {
            try {
                if(launcher != null){
                    statusList = new ArrayList<>(NetManager.getServerOnlineStatus(launcher));
                    Thread.sleep(Integer.parseInt(launcher.getConfig().get("connectionTimeout", "3000")) * 3 + 3000);
                }else
                    Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }).start();
    }

    public void updateStatusLabel(WebLabel label){
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

    public static List<ServerStatus> getServerOnlineStatus(LauncherWindow launcherWindow){
        ArrayList<ServerStatus> status = new ArrayList<>();
        int timeout = Integer.parseInt(launcherWindow.getConfig().get("connectionTimeout", "3000"));

        try {
            if(ping(launcherWindow.getConfig().get("authServerIp"), 2540, timeout))
                status.add(ServerStatus.AUTH_ONLINE);
            else
                status.add(ServerStatus.AUTH_OFFLINE);
        }catch (Exception ex){
            status.add(ServerStatus.AUTH_OFFLINE);
        }
        try{
            if(ping(launcherWindow.getConfig().get("minecraftServerIp"), 25565, timeout))
                status.add(ServerStatus.MINECRAFT_SERVER_ONLINE);
            else {
                System.out.println("mo");
                status.add(ServerStatus.MINECRAFT_SERVER_OFFLINE);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            status.add(ServerStatus.MINECRAFT_SERVER_OFFLINE);
        }
        try{
            if(InetAddress.getByName("google.com").isReachable(timeout))
                status.add(ServerStatus.INTERNET_ONLINE);
            else
                status.add(ServerStatus.INTERNET_OFFLINE);
        }catch (Exception ex){
            status.add(ServerStatus.INTERNET_OFFLINE);
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
        }catch (Exception ex){
        }
        return out;
    }

    public static boolean checkNickname(String name){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean sendEmailCode(String email){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean confirmEmail(String login, String password, String email, String code){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}
