package com.husker.launcher.server;

import com.husker.launcher.server.utils.ConsoleUtils;
import com.husker.launcher.server.utils.MailManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerMain {

    public static ServerSocket Server;
    public static final ServerSettingsFile Settings = new ServerSettingsFile();

    public static MailManager MailManager = new MailManager(Settings.getEmail(), Settings.getEmailPassword());

    public static void main(String[] args){
        ConsoleUtils.printDebug(ServerMain.class, "Starting...");

        new Thread(() -> {
            try {
                Server = new ServerSocket(Settings.getPort());
                ConsoleUtils.printDebug(ServerMain.class, "Server started at port: " + Settings.getPort());

                while(true){
                    Socket client = Server.accept();
                    new Thread(() -> new Client(client)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                ConsoleUtils.printDebug(ServerMain.class, "Server is closing... ");
                System.exit(0);
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            if(line.equals("stop") || line.equals("exit") || line.equals("quit"))
                System.exit(0);
            if(line.startsWith("port") && line.split(" ").length == 2){
                Settings.setPort(line.split(" ")[1]);
                ConsoleUtils.printDebug(ServerMain.class, "Changes were saved! Please restart server.");
            }
            if(line.startsWith("key") && line.split(" ").length == 2){
                Settings.setEncryptionKey(line.split(" ")[1]);
                ConsoleUtils.printDebug(ServerMain.class, "Changes were saved! Please restart server.");
            }
        }


    }

}
