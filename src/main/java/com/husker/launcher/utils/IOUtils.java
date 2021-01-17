package com.husker.launcher.utils;


import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class IOUtils {
    /*

    public static void delete(String path){
        delete(path, percent -> {});
    }

    public static void move(String from, String to) {
        move(from, to, percent -> {});
    }

    public static void moveDirectoryContent(String from, String to) {
        moveDirectoryContent(from, to, percent -> {});
    }

    public static void unzip(String zipPath, String to) throws IOException {
        unzip(zipPath, to, percent -> {});
    }

    public static void receiveFile(Socket socket, String to) throws IOException {
        receiveFile(socket, to, percent -> {});
    }

    public static void delete(String path, Consumer<Double> progress){
        progress.accept(0d);

        if(!Files.exists(Paths.get(path))) {
            progress.accept(100d);
            return;
        }

        if(new File(path).isDirectory()) {
            int files;
            int[] currentFiles = new int[]{0};
            files = countFilesInFolder(path) + 1;

            deleteFolder(path, i -> {
                currentFiles[0] += i;
                progress.accept((double)currentFiles[0] / (double)files * 100d);
            });
        }else {
            deleteAnyway(path);
            progress.accept(100d);
        }
    }

    public static void move(String element, String to, Consumer<Double> progress) {
        progress.accept(0d);

        if(!new File(element).exists()){
            progress.accept(100d);
            return;
        }

        if(new File(element).isDirectory()) {
            int files;
            int[] currentFiles = new int[]{0};
            files = countFilesInFolder(element) + 1;

            moveFolder(element, to + (to.isEmpty() ? "" : "/") + new File(element).getName(), i -> {
                currentFiles[0] += i;
                progress.accept((double)currentFiles[0] / (double)files * 100d);
            });
        }else {
            moveAnyway(element, to);
            progress.accept(100d);
        }
    }

    public static void moveDirectoryContent(String element, String to, Consumer<Double> progress) {
        progress.accept(0d);

        if(!new File(element).exists()){
            progress.accept(100d);
            return;
        }

        if(new File(element).isDirectory()) {
            int files;
            int[] currentFiles = new int[]{0};
            files = countFilesInFolder(element) + 1;

            moveFolder(element, to, i -> {
                currentFiles[0] += i;
                progress.accept((double)currentFiles[0] / (double)files * 100d);
            });
        }else {
            moveAnyway(element, to);
            progress.accept(100d);
        }
    }

    private static void deleteFolder(String folder, Consumer<Integer> listener){
        File[] files = new File(folder).listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory())
                    deleteFolder(file.getAbsolutePath(), listener);
                else {
                    deleteAnyway(file.getAbsolutePath());
                    listener.accept(1);
                }
            }
        }

        deleteAnyway(folder);
        listener.accept(1);
    }

    private static void moveFolder(String from, String to, Consumer<Integer> listener){
        File[] files = new File(from).listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory())
                    moveFolder(file.getAbsolutePath(), to + "/" + file.getName(), listener);
                else {
                    moveAnyway(file.getAbsolutePath(), to + "/" + file.getName());
                    listener.accept(1);
                }
            }
        }

        listener.accept(1);
        deleteAnyway(from);
    }

    private static int countFilesInFolder(String folder){
        int count = 0;

        File[] files = new File(folder).listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    count += countFilesInFolder(file.getAbsolutePath());
                count ++;
            }
        }
        return count;
    }

    private static void deleteAnyway(String path){
        while(Files.exists(Paths.get(path))){
            try {
                FileUtils.forceDelete(new File(path));
            }catch (Exception ignored){ }
        }
    }

    private static void moveAnyway(String from, String to){
        while(true) {
            try {
                Files.createDirectories(Paths.get(to.substring(0, to.lastIndexOf("/"))));
                Files.move(Paths.get(from), Paths.get(to));
                break;
            }catch (Exception ex){
                //ex.printStackTrace();
            }
        }
    }

    public static void receiveFile(Socket socket, String to, Consumer<FileReceivingArguments> progress) throws IOException {
        InputStream is = socket.getInputStream();

        long size = Long.parseLong(new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine());
        long currentSize = 0;
        long start = System.currentTimeMillis();

        FileOutputStream fileOutputStream = new FileOutputStream(to);
        byte[] dataBuffer = new byte[1024];
        int len;
        while ((len = is.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, len);

            currentSize += len;

            double deltaTime = System.currentTimeMillis() - start;
            double speed = 0;
            if(deltaTime > 0)
                speed = (currentSize / deltaTime) / 125d;

            progress.accept(new FileReceivingArguments(speed, size, currentSize));
        }
        fileOutputStream.close();
    }

    public static void receiveFile(String url, String to, Consumer<FileReceivingArguments> progress) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.connect();
        long size = connection.getContentLength();

        InputStream is = new URL(url).openStream();

        long currentSize = 0;
        long start = System.currentTimeMillis();

        FileOutputStream fileOutputStream = new FileOutputStream(to);
        byte[] dataBuffer = new byte[1024];
        int len;
        while ((len = is.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, len);

            currentSize += len;

            double deltaTime = System.currentTimeMillis() - start;
            double speed = 0;
            if(deltaTime > 0)
                speed = (currentSize / deltaTime) / 125d;

            progress.accept(new FileReceivingArguments(speed, size, currentSize));
        }
        fileOutputStream.close();
    }

    public static void unzip(String zipPath, String to, Consumer<ZipArguments> progress) throws IOException {
        if(!Files.exists(Paths.get(to)))
            Files.createDirectories(Paths.get(to));

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipPath));
        ZipEntry entry = zipIn.getNextEntry();

        long size = (long)(new File(zipPath).length() * 1.67d);
        long currentSize = 0;

        while (entry != null) {
            String filePath = to + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                byte[] bytesIn = new byte[4096];
                int len;
                while ((len = zipIn.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, len);
                    currentSize += len;

                    progress.accept(new ZipArguments(size, currentSize));
                }
                bos.close();

            } else
                Files.createDirectories(Paths.get(filePath));

            while(true) {
                Files.getFileAttributeView(Paths.get(filePath), BasicFileAttributeView.class).setTimes(entry.getLastModifiedTime(), entry.getLastAccessTime(), entry.getCreationTime());

                BasicFileAttributes attributes = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class);

                if( (entry.getCreationTime() == null || attributes.creationTime().toMillis() == entry.getCreationTime().toMillis()) &&
                    (entry.getLastAccessTime() == null || attributes.lastAccessTime().toMillis() == entry.getLastAccessTime().toMillis()) &&
                    (entry.getLastModifiedTime() == null || attributes.lastModifiedTime().toMillis() == entry.getLastModifiedTime().toMillis())
                )
                    break;
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    public static String readFileText(String path) throws IOException {
        return FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
    }

    public static void writeFileText(String path, String text) throws IOException {
        FileUtils.write(new File(path), text, StandardCharsets.UTF_8);
    }

    public static String[] list(String folder){
        String[] folders = new File(folder).list();
        if(folders != null)
            return folders;
        else
            return new String[0];
    }

    public static File[] fileList(String folder){
        File[] folders = new File(folder).listFiles();
        if(folders != null)
            return folders;
        else
            return new File[0];
    }

    public static long getFileLength(String path){
        if(new File(path).isFile())
            return new File(path).length();

        long length = 0;
        File[] files = new File(path).listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isFile())
                    length += file.length();
                else
                    length += getFileLength(file.getAbsolutePath());
            }
        }
        return length;
    }

    public static void zip(String path, String zipPath){
        zip(path, zipPath, e -> {});
    }

    public static void zip(String path, String zipPath, Consumer<ZipArguments> progress){
        try {
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipPath));
            FileInputStream fis = new FileInputStream(path);

            long size = (long)(new File(zipPath).length() * 1.67d);
            long currentSize = 0;

            byte[] dataBuffer = new byte[1024];
            int len;
            while ((len = fis.read(dataBuffer, 0, 1024)) != -1) {
                zout.write(dataBuffer, 0, len);

                currentSize += len;
                progress.accept(new ZipArguments(size, currentSize));
            }
            zout.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static class FileReceivingArguments {
        private final double speed;
        private final long size;
        private final long current;

        public FileReceivingArguments(double speed, long size, long current){
            this.speed = speed;
            this.size = size;
            this.current = current;
        }

        public double getPercent() {
            return (double) current / (double) size * 100d;
        }

        public double getSpeed() {
            return speed;
        }

        public long getSize() {
            return size;
        }

        public long getCurrentSize() {
            return current;
        }
    }

    public static class ZipArguments {
        private final long size;
        private final long current;

        public ZipArguments(long size, long current){
            this.size = size;
            this.current = current;
        }

        public double getPercent() {
            return (double) current / (double) size * 100d;
        }

        public long getSize() {
            return size;
        }

        public long getCurrentSize() {
            return current;
        }
    }

     */
}
