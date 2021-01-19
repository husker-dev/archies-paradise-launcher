package com.husker.launcher.anticheat.modules;

import com.husker.launcher.anticheat.AntiCheat;

public class OnServerConnectionChecker {

    /*
        Check for client md5 when joining to the server
    */
    public OnServerConnectionChecker(AntiCheat antiCheat){
        antiCheat.addConsoleListener(line -> {
            if(line.contains("[main/INFO] [minecraft/GuiConnecting]: Connecting to ")){
                boolean[] result = antiCheat.checkMD5();
                if(!result[0] || !result[1])
                    antiCheat.closeGame();
            }
        });
    }
}
