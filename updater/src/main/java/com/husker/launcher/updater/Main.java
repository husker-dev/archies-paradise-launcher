package com.husker.launcher.updater;

import com.vnetpublishing.java.suapp.SU;
import com.vnetpublishing.java.suapp.SuperUserApplication;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main extends SuperUserApplication {

    public static boolean safeMode = false;

    public static void main(String[] args){
        SU.run(new Main(), args);
    }

    public int run(String[] args) {
        //ConsoleUtils.configureLogging("updater");
        safeMode = Arrays.asList(args).contains("safe");
        if(safeMode)
            ConsoleUtils.printDebug(Main.class, "Running in safe mode!");

        String path = "";

        for(String arg : args)
            if (arg.startsWith("--folder="))
                path = arg.replace("--folder=", "");

        if(path.equals("")) {
            ConsoleUtils.printDebug(Main.class, "Argument '--path' is empty!");
            path = new File("update_launcher").getAbsolutePath();
        }

        doEvilThings(path);
        return 0;
    }

    public static void doEvilThings(String path){
        ConsoleUtils.printDebug(Main.class, "Launcher path: " + path);

        for(File file : Objects.requireNonNull(new File("./").listFiles(file ->
                    !file.getName().equals("updater.jar") &&
                    !file.getName().equals(new File(path).getName()) &&
                    !file.getName().equals("clients")
        )))
            delete(file.getAbsolutePath());

        for(File file : new File(path).listFiles()) {
            move(file.getAbsolutePath(), new File("./", file.getName()).getAbsolutePath());
        }
        try {
            Runtime.getRuntime().exec(new File("launcher.exe").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            try {
                Files.move(Paths.get(from), Paths.get(to));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "ОШИБКА " + e.getMessage());
                e.printStackTrace();
            }
        }else
            ConsoleUtils.printDebug(Main.class, "[SAVE] Move: " + from + " -> " + to);
    }


}
