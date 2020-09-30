package com.husker.launcher.server.utils;

import com.husker.launcher.server.ServerMain;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UpdateManager {

    public static final String updateFolder = "update";
    public static final String clientFolder = "server_client";

    private static long lastBuildId = 0;

    public static void applyUpdate(){
        ConsoleUtils.printDebug(ServerMain.class, "Updating...");

        JSONObject zipInfo = new JSONObject();
        JSONObject folderInfo = new JSONObject();

        try {
            if (Files.exists(Paths.get(clientFolder + "/client_info.json")))
                lastBuildId = new JSONObject(FileUtils.readFileToString(new File(clientFolder + "/client_info.json"), StandardCharsets.UTF_8)).getLong("build_id");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        lastBuildId++;

        try{
            removeIfExist(clientFolder + "/mods.zip");
            removeIfExist(clientFolder + "/versions.zip");
            removeIfExist(clientFolder + "/other.zip");
            removeIfExist(clientFolder + "/client_info.json");
            removeIfExist(clientFolder + "/files_info.json");

            Files.createDirectories(Paths.get(updateFolder + "/mods"));
            Files.createDirectories(Paths.get(updateFolder + "/versions"));

            Files.createDirectories(Paths.get(clientFolder));
        } catch (IOException e) {
            ConsoleUtils.printDebug(UpdateManager.class, "Error!");
            e.printStackTrace();
        }

        // Client info
        ClientManager clientManager = new ClientManager("update");
        ArrayList<JSONObject> mods = new ArrayList<>();
        for(ClientManager.ModInfo info : clientManager.getModInfo(clientManager.getModsCount())) {
            try {
                mods.add(info.toJson());
            }catch (Exception ex){
            }
        }
        try {
            Files.write(Paths.get(clientFolder + "/client_info.json"), Collections.singletonList(new JSONObject(){{
                put("version", clientManager.getClientInfo().getVersion());
                put("mods", mods);
                put("mods_MD5", clientManager.getModsMD5());
                put("client_MD5", clientManager.getClientInfo().getMD5());
                put("build", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                put("build_id", lastBuildId);
            }}.toString(4)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        folderInfo.put("mods", IOUtils.getFileLength(updateFolder + "/mods"));
        folderInfo.put("versions", IOUtils.getFileLength(updateFolder + "/versions"));

        addFilesToZip(updateFolder + "/mods", clientFolder + "/mods");
        addFilesToZip(updateFolder + "/versions", clientFolder + "/versions");

        removeIfExist(updateFolder + "/mods");
        removeIfExist(updateFolder + "/versions");

        folderInfo.put("other", IOUtils.getFileLength(updateFolder));
        addFilesToZip(updateFolder,  clientFolder + "/other");

        zipInfo.put("mods", new File(clientFolder + "/mods.zip").length());
        zipInfo.put("versions", new File(clientFolder + "/versions.zip").length());
        zipInfo.put("other", new File(clientFolder + "/other.zip").length());

        try {
            for (File filePath : Objects.requireNonNull(new File(updateFolder + "/").listFiles()))
                removeIfExist(filePath.getAbsolutePath());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            Files.write(Paths.get(clientFolder + "/files_info.json"), Collections.singletonList(new JSONObject(){{
                put("zip", zipInfo.toMap());
                put("folders", folderInfo.toMap());
            }}.toString(4)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConsoleUtils.printDebug(ServerMain.class, "Updated!");
    }

    private static void removeIfExist(String path){
        try {
            if (Files.exists(Paths.get(path))) {
                ConsoleUtils.printDebug(UpdateManager.class, "Removing existing \"" + path + "\" file...");
                IOUtils.delete(path);
                ConsoleUtils.printDebug(UpdateManager.class, "Removed!");
            }
        }catch (Exception ex){
            ex.printStackTrace();
            ConsoleUtils.printDebug(UpdateManager.class, "Not removed!");
        }
    }

    public static void addFilesToZip(String sourceFolder, String zipPath) {
        ConsoleUtils.printDebug(UpdateManager.class, "Archiving \"" + sourceFolder + "\" -> \"" + zipPath + ".zip\"...");
        try {
            FileOutputStream fos = new FileOutputStream(zipPath + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFolder);

            fileToZip.setReadable(true);
            fileToZip.setWritable(true);
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
        } catch(Exception e) {
            ConsoleUtils.printDebug(UpdateManager.class, "Error!");
            e.printStackTrace();
        }
        ConsoleUtils.printDebug(UpdateManager.class, "Archived!");
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/"))
                zipOut.putNextEntry(new ZipEntry(fileName));
             else
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : children)
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0)
            zipOut.write(bytes, 0, length);
        fis.close();
    }
}
