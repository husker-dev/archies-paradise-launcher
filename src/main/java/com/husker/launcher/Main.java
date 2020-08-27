package com.husker.launcher;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
        if(argList.contains("-debug")){
            BrowserManager.enabled = false;
            UpdateManager.enable = false;
            NetManager.enable = false;
        }
        if(!argList.contains("-console")){
            try {
                Files.createDirectories(Paths.get("./logs"));
                Files.createFile(Paths.get("./logs/launcher_log.txt"));
                System.setOut(new PrintStream("./logs/launcher_log.txt"));
            }catch (Exception ex){
                ex.getStackTrace();
            }
        }
        new LauncherWindow();
    }

}
