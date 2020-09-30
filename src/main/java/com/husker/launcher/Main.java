package com.husker.launcher;

import com.husker.launcher.managers.NetManager;
import com.husker.launcher.managers.UpdateManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
        if(argList.contains("-disable-updates"))
            UpdateManager.enable = false;
        if(argList.contains("-disable-net"))
            NetManager.enable = false;

        if(argList.contains("-debug")){
            UpdateManager.enable = false;
            NetManager.enable = false;
        }
        if(!argList.contains("-console")){
            try {
                Files.createDirectories(Paths.get("./logs"));

                Files.deleteIfExists(Paths.get("./logs/launcher_log.txt"));
                Files.createFile(Paths.get("./logs/launcher_log.txt"));
                System.setOut(new PrintStream("./logs/launcher_log.txt"));

                Files.deleteIfExists(Paths.get("./logs/launcher_err_log.txt"));
                Files.createFile(Paths.get("./logs/launcher_err_log.txt"));
                System.setOut(new PrintStream("./logs/launcher_err_log.txt"));
            }catch (Exception ex){
                ex.getStackTrace();
            }
        }

        new Launcher();
    }
}