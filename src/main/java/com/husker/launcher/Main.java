package com.husker.launcher;

import com.husker.launcher.managers.NetManager;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.utils.ConsoleUtils;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        ConsoleUtils.configureLogging("launcher");
        UpdateManager.enable = false;

        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        new LoadingWindow();
    }



}