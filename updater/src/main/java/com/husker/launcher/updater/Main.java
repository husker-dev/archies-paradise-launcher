package com.husker.launcher.updater;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.FileVisitResult.*;

public class Main {


    public static void main(String[] args){
        ArrayList<String> doNotDelete = new ArrayList<>(Arrays.asList(args));
        doNotDelete.add(getExecutorName());
        doNotDelete.add("update");
        doNotDelete.add("debug.bat");

        if(new File("./update").exists()){
            System.out.println("Update folder exist");
            for(File file : Objects.requireNonNull(new File("./").listFiles())){
                try {
                    if (!doNotDelete.contains(file.getName())) {
                        System.out.println("Removing: " + file.getName());
                        deleteFileOrFolder(Paths.get("./" + file.getName()));
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            for(File file : Objects.requireNonNull(new File("./update").listFiles())) {
                try {
                    Files.move(Paths.get("./update/" + file.getName()), Paths.get("./" + file.getName()));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            try {
                deleteFileOrFolder(Paths.get("./update"));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        try {
            Runtime.getRuntime().exec("java -jar launcher.jar");
        }catch (Exception ex){
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
