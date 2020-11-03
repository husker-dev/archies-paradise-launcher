package com.husker.launcher;

import com.husker.launcher.managers.NetManager;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.utils.ConsoleUtils;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        ConsoleUtils.configureLogging("launcher");
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
        if(argList.contains("-disable-updates"))
            UpdateManager.enable = false;
        if(argList.contains("-disable-net"))
            NetManager.enable = false;

        if(argList.contains("-debug")){
            UpdateManager.enable = false;
            NetManager.enable = false;
        }

        new Launcher();
    }



}