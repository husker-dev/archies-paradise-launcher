package com.husker.launcher.updater;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    public static boolean safeMode = false;

    public static void main(String[] args){
        ConsoleUtils.configureLogging("updater");
        safeMode = Arrays.asList(args).contains("safe");
        if(safeMode)
            ConsoleUtils.printDebug(Main.class, "Running in safe mode!");

        String path = "";
        ArrayList<String> clientFolder = new ArrayList<>();

        for(String arg : args) {
            if (arg.startsWith("--path="))
                path = arg.replace("--path=", "");
            if (arg.startsWith("--save="))
                clientFolder.addAll(Arrays.asList(arg.replace("--save=", "").split(",")));
        }
        if(path.equals("")){
            ConsoleUtils.printDebug(Main.class, "Argument '--path' is empty!");
        }
        doEvilThings(path, clientFolder);
    }

    public static void doEvilThings(String path, List<String> save){
        ConsoleUtils.printDebug(Main.class, "Path to update: " + path);

        for(File file : IOUtils.fileList(path)){
            if(save.contains(file.getName()) && file.isDirectory())
                continue;
            delete(file.getAbsolutePath());
        }

        for(File file : IOUtils.fileList("."))
            if(!file.getName().equals(getExecutorName()))
                move(file.getAbsolutePath(), path);
    }

    public static String getExecutorName(){
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    public static void delete(String path){
        if(!safeMode)
            IOUtils.delete(path);
        else
            ConsoleUtils.printDebug(Main.class, "[SAVE] Delete: " + path);
    }

    public static void move(String from, String to){
        if(!safeMode) {
            IOUtils.move(from, to);
        }else
            ConsoleUtils.printDebug(Main.class, "[SAVE] Move: " + from + " -> " + to);
    }
}
