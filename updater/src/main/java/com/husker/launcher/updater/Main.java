package com.husker.launcher.updater;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.FileVisitResult.*;

public class Main {


    public static void main(String[] args){
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));
        if(!argList.contains("-console")){
            try {
                Files.createDirectories(Paths.get("./logs"));
                Files.deleteIfExists(Paths.get("./logs/updater_log.txt"));
                Files.createFile(Paths.get("./logs/updater_log.txt"));
                System.setOut(new PrintStream("./logs/updater_log.txt"));
            }catch (Exception ex){
                ex.getStackTrace();
            }
        }

        ArrayList<String> doNotDelete = new ArrayList<>(Arrays.asList(args));
        doNotDelete.add(getExecutorName());
        doNotDelete.add("update");
        doNotDelete.add("logs");
        doNotDelete.add("debug.bat");

        if(new File("./update").exists()){
            ConsoleUtils.printDebug(Main.class, "Update folder exist!");
            for(File file : Objects.requireNonNull(new File("./").listFiles())){
                try {
                    if (!doNotDelete.contains(file.getName())) {
                        ConsoleUtils.printDebug(Main.class, "Removing: " + file.getName());
                        while(Files.exists(Paths.get("./" + file.getName()))) {
                            try {
                                deleteFileOrFolder(Paths.get("./" + file.getName()));
                            }catch (Exception ex){}
                        }
                        ConsoleUtils.printResult("OK");
                    }
                }catch (Exception ex){
                    ConsoleUtils.printResult("ERROR");
                    ex.printStackTrace();
                }
            }

            for(File file : Objects.requireNonNull(new File("./update").listFiles())) {
                try {
                    if(!doNotDelete.contains(file.getName())) {
                        ConsoleUtils.printDebug(Main.class, "Moving from update: " + file.getName());
                        Files.move(Paths.get("./update/" + file.getName()), Paths.get("./" + file.getName()));
                        ConsoleUtils.printResult("OK");
                    }
                }catch (Exception ex){
                    ConsoleUtils.printResult("ERROR");
                    ex.printStackTrace();
                }
            }

            try {
                ConsoleUtils.printDebug(Main.class, "Removing update folder...");
                deleteFileOrFolder(Paths.get("./update"));
                ConsoleUtils.printResult("OK");
            }catch (Exception ex){
                ConsoleUtils.printResult("ERROR");
                ex.printStackTrace();
            }
        }else
            ConsoleUtils.printDebug(Main.class, "Update folder not exist");

        try {
            ConsoleUtils.printDebug(Main.class, "Starting launcher...");
            Runtime.getRuntime().exec("java -jar launcher.jar");
            ConsoleUtils.printResult("OK");
        }catch (Exception ex){
            ConsoleUtils.printResult("ERROR");
            ex.printStackTrace();
        }
    }

    public static String getExecutorName(){
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    public static void deleteFileOrFolder(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                e.printStackTrace();
                return TERMINATE;
            }

            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
                if(e!=null)
                    return handleException(e);
                Files.delete(dir);
                return CONTINUE;
            }
        });
    };
}
