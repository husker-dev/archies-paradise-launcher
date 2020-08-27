package com.husker.launcher.server;

import com.husker.launcher.server.utils.ConsoleUtils;
import com.husker.launcher.server.utils.MailManager;
import com.husker.launcher.server.utils.ProfileUtils;
import com.husker.launcher.server.utils.SettingsFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerMain {

    public static ServerSocket Server;
    public static final SettingsFile Settings = new SettingsFile(new File("server_settings.cfg")){{
        setDefault("port", "15565");
        setDefault("encryption_key", ProfileUtils.generateKey());
        setDefault("email", "null");
        setDefault("email_password", "null");
        setDefault("email_title", "Archie's Paradise");
    }};

    public static MailManager MailManager = new MailManager(Settings.get("email"), Settings.get("email_password"));

    public static void main(String[] args){
        ConsoleUtils.printDebug(ServerMain.class, "Starting...");

        int port = Integer.parseInt(Settings.get("port", "15565"));

        new Thread(() -> {
            try {
                Server = new ServerSocket(port);
                ConsoleUtils.printDebug(ServerMain.class, "Server started at port: " + port);

                while(true){
                    Socket client = Server.accept();
                    new Thread(() -> new Client(client)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            if(line.equals("stop") || line.equals("exit") || line.equals("quit"))
                System.exit(0);
            if(line.startsWith("port") && line.split(" ").length == 2){
                Settings.set("port", line.split(" ")[1]);
                ConsoleUtils.printDebug(ServerMain.class, "Changes were saved! Please restart server.");
            }
            if(line.startsWith("key") && line.split(" ").length == 2){
                Settings.set("key", line.split(" ")[1]);
                ConsoleUtils.printDebug(ServerMain.class, "Changes were saved! Please restart server.");
            }
        }
    }


}
