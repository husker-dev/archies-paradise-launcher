package com.husker.launcher.server.services;

import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.Service;
import com.husker.launcher.server.utils.ConsoleUtils;

import java.util.Scanner;

public class ConsoleService extends Service {


    public void onStart() {
        Scanner scanner = new Scanner(System.in);
        started();
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            if(line.equals("stop") || line.equals("exit") || line.equals("quit"))
                System.exit(0);
            if(line.startsWith("port") && line.split(" ").length == 2){
                ServerMain.Settings.setPort(line.split(" ")[1]);
                ConsoleUtils.printDebug(ServerMain.class, "Changes were saved! Please restart server.");
            }
            if(line.startsWith("key") && line.split(" ").length == 2){
                ServerMain.Settings.setEncryptionKey(line.split(" ")[1]);
                ConsoleUtils.printDebug(ServerMain.class, "Changes were saved! Please restart server.");
            }
        }
    }
}
