package com.husker.launcher.server.core;

import com.husker.launcher.server.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Client {

    private static final Logger log = LogManager.getLogger(Client.class);

    public static final String folder = "./loaded_clients";

    static {
        try {
            if (Files.exists(Paths.get(folder)))
                Files.createDirectory(Paths.get(folder));
        } catch (Exception ignored) {
        }
    }

    public static String[] getList() {
        return new File(folder).list((file, name) -> {
            String path = file.getAbsolutePath() + "/" + name;
            return new File(path).isDirectory() && new File(path + "/client_info.json").exists();
        });
    }

    private final String id;

    public Client(String id) {
        this.id = id;
        if (!Files.exists(Paths.get(folder + "/" + id)))
            throw new NullPointerException("Can't find client with id: " + id);
    }

    public File getModsFile() {
        return new File(folder + "/" + id + "/mods.zip");
    }

    public File getVersionsFile() {
        return new File(folder + "/" + id + "/versions.zip");
    }

    public File getOtherFile() {
        return new File(folder + "/" + id + "/other.zip");
    }

    public JSONObject getSizeInfo() throws IOException {
        return new JSONObject(IOUtils.readFileText(folder + "/" + id + "/files_info.json"));
    }

    public JSONObject getClientInfo() throws IOException {
        return new JSONObject(IOUtils.readFileText(folder + "/" + id + "/client_info.json"));
    }

    public JSONObject getModsInfo(ModsGetterParameters parameter) throws IOException {
        JSONArray mods = getClientInfo().getJSONArray("mods");
        ModsGetterParameters.IconParameter iconParameter = parameter.getIconParameter();

        JSONArray out = new JSONArray();

        if(parameter.getIndex() == -1) {
            for (int i = 0; i < Math.min(mods.length(), parameter.getCount()); i++) {
                JSONObject modInfo = mods.getJSONObject(i);

                boolean icon = !modInfo.getString("icon").equals("null");
                modInfo.put("index", i);
                modInfo.put("icon", !modInfo.getString("icon").equals("null"));

                if (!icon && iconParameter == ModsGetterParameters.IconParameter.NOT_REQUIRE)
                    out.put(modInfo);
                else if (icon && iconParameter == ModsGetterParameters.IconParameter.REQUIRE)
                    out.put(modInfo);
                else if (iconParameter == ModsGetterParameters.IconParameter.NOT_STATED)
                    out.put(modInfo);
            }
        }else {
            JSONObject modInfo = mods.getJSONObject(parameter.getIndex());
            modInfo.put("index", parameter.getIndex());
            modInfo.put("icon", !modInfo.getString("icon").equals("null"));
            out.put(modInfo);
        }
        return new JSONObject(){{
            put("mods", out);
        }};
    }

    public String getModIcon(int index) throws IOException {
        return getClientInfo().getJSONArray("mods").getJSONObject(index).getString("icon");
    }

    public static class ModsGetterParameters{
        public enum IconParameter {
            REQUIRE,
            NOT_REQUIRE,
            NOT_STATED
        }

        private int count = Integer.MAX_VALUE;
        private int index = -1;
        private IconParameter icon = IconParameter.NOT_STATED;

        public void setCount(int count){
            this.count = count;
        }

        public void setIconParameter(IconParameter icon){
            this.icon = icon;
        }

        public void setIndex(int index){
            this.index = index;
        }

        public int getIndex(){
            return index;
        }

        public int getCount(){
            return count;
        }

        public IconParameter getIconParameter(){
            return icon;
        }
    }

    public static String createId(){
        return System.currentTimeMillis() + "";
    }

    public static void archive(String id){
        archive(null, id);
    }

    public static void archive(String name, String id){
        try {
            log.info("Updating...");

            String to = folder + "/" + id;
            String from = "received_clients_tmp/" + id;

            Files.createDirectories(Paths.get(to));

            JSONObject zipInfo = new JSONObject();
            JSONObject folderInfo = new JSONObject();

            final int[] buildId = new int[1];
            try {
                if (Files.exists(Paths.get(to + "/client_info.json")))
                    buildId[0] = new JSONObject(FileUtils.readFileToString(new File(to + "/client_info.json"), StandardCharsets.UTF_8)).getInt("build_id");
            }catch (Exception ex){
                ex.printStackTrace();
            }

            for(String file : new File(to).list())
                IOUtils.delete(file);
            Files.createDirectories(Paths.get(from + "/mods"));
            Files.createDirectories(Paths.get(from + "/versions"));
            Files.createDirectories(Paths.get(to));

            // Client info
            ClientManager clientManager = new ClientManager(from);
            ArrayList<JSONObject> mods = new ArrayList<>();
            for(ClientManager.ModInfo info : clientManager.getModInfo(clientManager.getModsCount())) {
                try {
                    mods.add(info.toJson());
                }catch (Exception ignored){}
            }

            Files.write(Paths.get(to + "/client_info.json"), Collections.singletonList(new JSONObject(){{
                put("version", clientManager.getClientInfo().getVersion());
                put("mods", mods);
                put("mods_MD5", clientManager.getModsMD5());
                put("versions_MD5", clientManager.getClientInfo().getMD5());
                put("build", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                put("build_id", buildId[0]);
                put("name", name != null ? name : id);
            }}.toString(4)));

            // Write folder size
            folderInfo.put("mods", IOUtils.getFileLength(from + "/mods"));
            folderInfo.put("versions", IOUtils.getFileLength(from + "/versions"));

            // Zipping
            addFilesToZip(from + "/mods", to + "/mods.zip");
            addFilesToZip(from + "/versions", to + "/versions.zip");

            // Removing mods and versions folders
            removeIfExist(from + "/mods");
            removeIfExist(from + "/versions");

            // Zipping "other" files
            folderInfo.put("other", IOUtils.getFileLength(from));
            addFilesToZip(from,  to + "/other.zip");

            // Write zip sizes
            zipInfo.put("mods", new File(to + "/mods.zip").length());
            zipInfo.put("versions", new File(to + "/versions.zip").length());
            zipInfo.put("other", new File(to + "/other.zip").length());

            for (File filePath : Objects.requireNonNull(new File(from + "/").listFiles()))
                removeIfExist(filePath.getAbsolutePath());

            Files.write(Paths.get(to + "/files_info.json"), Collections.singletonList(new JSONObject(){{
                put("zip", zipInfo.toMap());
                put("folders", folderInfo.toMap());
            }}.toString(4)));

            IOUtils.delete(from);
            log.info("Updated!");
        } catch (Exception e) {
            log.error("Error while updating: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void removeIfExist(String path){
        try {
            if (Files.exists(Paths.get(path))) {
                log.info("Removing \"" + path + "\"...");
                IOUtils.delete(path);
            }
        }catch (Exception ex){
            log.error("Error while removing: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void addFilesToZip(String sourceFolder, String zipPath) {
        log.info("Archiving \"" + sourceFolder + "\" -> \"" + zipPath + "\"...");
        try {
            FileOutputStream fos = new FileOutputStream(zipPath);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFolder);

            fileToZip.setReadable(true);
            fileToZip.setWritable(true);
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
        } catch(Exception e) {
            log.error("Error: " + e.getMessage());
            e.printStackTrace();
        }
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