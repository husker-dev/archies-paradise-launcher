package com.husker.launcher.server.services;

import com.husker.launcher.server.ServerMain;

import java.util.Scanner;

public class ConsoleService implements Runnable{

    public void run() {
        Scanner scanner = new Scanner(System.in);

        new Thread(() -> {
            while(scanner.hasNext()){
                String line = scanner.nextLine();
                if(line.equals("stop") || line.equals("exit") || line.equals("quit"))
                    System.exit(0);
                if(line.startsWith("port") && line.split(" ").length == 2){
                    ServerMain.Settings.setPort(line.split(" ")[1]);
                    //ConsoleUtils.printDebug(ServerMain.class, "Changes are saved! Please restart server.");
                }

                if(line.equals("update")){
                    //UpdateManager.applyUpdate();
                }
            }
        }).start();
    }
}
