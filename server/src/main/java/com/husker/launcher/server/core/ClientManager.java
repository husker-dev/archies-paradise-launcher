package com.husker.launcher.server.core;

import com.husker.launcher.server.ServerMain;
import com.husker.mio.FSUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.husker.launcher.server.services.HttpService.toBase64;

public class ClientManager {

    private static final Logger log = LogManager.getLogger(ClientManager.class);

    private final String clientPath;

    public ClientManager(String clientPath){
        this.clientPath = clientPath;
    }

    public File getVersionFile(){
        if(!Files.exists(Paths.get(clientPath + "/versions")))
            return null;

        File[] files = new File(clientPath + "/versions").listFiles();
        if(files != null && files.length > 0){
            for(File versionFolder : files){
                boolean jar = false;
                boolean json = false;
                for(File file : versionFolder.listFiles()) {
                    if (file.getName().endsWith(".jar"))
                        jar = true;
                    if (file.getName().endsWith(".json"))
                        json = true;
                }
                if(jar && json)
                    return versionFolder;
            }
        }

        return null;
    }

    public VersionInfo getClientInfo(){
        return new VersionInfo(getVersionFile());
    }

    public ModInfo[] getModInfo(int count){
        ArrayList<ModInfo> mods = new ArrayList<>();

        if(Files.exists(Paths.get(clientPath + "/mods"))){
            File[] modsFiles = new File(clientPath + "/mods").listFiles(file -> file.getName().endsWith(".jar"));
            modsFiles = sortBySize(modsFiles);

            for (int i = 0; i < Math.min(count, modsFiles.length); i++) {
                File modFile = modsFiles[i];
                try {
                    mods.add(new ModInfo(modFile));
                }catch (Exception ex){
                    ex.printStackTrace();
                    log.error("Can't read: " + modFile.getName());
                }
            }
        }

        return mods.toArray(new ModInfo[0]);
    }

    public String getModsMD5(){
        ArrayList<File> mods = new ArrayList<>(FSUtils.getChildren(new File(clientPath + "/mods")));
        mods = mods.stream()
                .filter(file -> file.getName().endsWith(".jar"))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toCollection(ArrayList::new));

        Vector<FileInputStream> streams = new Vector<>();
        for(File file : mods){
            try {
                streams.add(new FileInputStream(file));
            }catch (Exception ignored){}
        }
        try {
            return DigestUtils.md5Hex(new SequenceInputStream(streams.elements()));
        }catch (Exception ex){
            return null;
        }
    }

    public int getModsCount(){
        return Objects.requireNonNull(new File(clientPath + "/mods").listFiles(file -> file.getName().endsWith(".jar"))).length;
    }

    public ModInfo getModInfoByIndex(int index){
        if(Files.exists(Paths.get(clientPath + "/mods"))){
            File[] modsFiles = new File(clientPath + "/mods").listFiles(file -> file.getName().endsWith(".jar"));
            modsFiles = sortBySize(modsFiles);

            File modFile = modsFiles[index];
            try {
                return new ModInfo(modFile);
            }catch (Exception ex){
                log.error("Can't read: " + modFile.getName());
                ex.printStackTrace();
            }
        }

        return null;
    }

    private File[] sortBySize(File[] files){
        return Arrays.stream(files)
                .filter(File::isFile)
                .sorted((File a, File b) -> a.length() > b.length() ? -1 : 1)
                .toArray(File[]::new);
    }

    private static InputStream readZipFile(String zipFilePath, String relativeFilePath) {
        if(zipFilePath == null || relativeFilePath == null)
            return null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                if (!entry.isDirectory() && entry.getName().equals(relativeFilePath)) {
                    zipFile.close();
                    return zipFile.getInputStream(entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException ignored) {}
        }
        return null;
    }

    private static String readInputStream(InputStream inputStream){
        if(inputStream == null)
            return null;
        String out = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        try {
            inputStream.close();
        } catch (Exception ignored) {}
        return out;
    }

    public static class VersionInfo {

        private final File file;

        public VersionInfo(File file){
            file.setWritable(true);
            file.setReadable(true);
            this.file = file;
        }

        public File getFile(String suffix){
            return Objects.requireNonNull(file.listFiles(file -> file.getName().endsWith(suffix)))[0];
        }

        public String getVersion(){
            try {
                JSONObject object = new JSONObject(readInputStream(new FileInputStream(getFile(".json"))));

                if (object.has("inheritsFrom"))
                    return object.getString("inheritsFrom");
                if (object.has("id"))
                    return object.getString("id");
                if (object.has("jar"))
                    return object.getString("jar");
            }catch (Exception ex){
                ex.printStackTrace();
            }

            return file.getName();
        }

        public String getMD5(){
            try {
                return DigestUtils.md5Hex(new FileInputStream(getFile(".jar")));
            }catch (Exception ex){
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static class ModInfo{
        private static final int TYPE_FABRIC = 0;
        private static final int TYPE_FORGE = 1;
        private static final int TYPE_FORGE_MODLIST = 2;

        private int type = TYPE_FORGE;

        private final File file;
        private final JSONObject json;

        public ModInfo(File file) throws UnsupportedOperationException{
            this.file = file;

            String text = readInputStream(readZipFile(file.getAbsolutePath(), "mcmod.info"));
            if (text == null) {
                text = readInputStream(readZipFile(file.getAbsolutePath(), "fabric.mod.json"));
                type = TYPE_FABRIC;
            }
            if(text == null)
                throw new UnsupportedOperationException("Mod not supported: " + file.getName());

            text = text.trim();
            if (text.startsWith("[") && text.endsWith("]"))
                text = text.substring(1, text.length() - 1);
            json = new JSONObject(text);

            if(type == TYPE_FORGE && getJson().has("modList"))
                type = TYPE_FORGE_MODLIST;
        }

        public JSONObject getJson(){
            return json;
        }

        public JSONObject toJson(){
            return new JSONObject(){{
                put("name", getName());
                put("description", getDescription());

                BufferedImage icon = getIcon();
                if(icon != null)
                    put("icon", toBase64(icon));
                else
                    put("icon", "null");
            }};
        }

        private JSONObject getForgeJSON(){
            if(type == TYPE_FORGE_MODLIST)
                return getJson().getJSONArray("modList").getJSONObject(0);
            else
                return json;
        }

        public String getName() throws UnsupportedOperationException{
            if(type == TYPE_FABRIC || type == TYPE_FORGE || type == TYPE_FORGE_MODLIST)
                return getForgeJSON().getString("name");
            else
                throw new UnsupportedOperationException("Can't get mod name: " + file.getName());
        }

        public String getDescription(){
            if(type == TYPE_FABRIC || type == TYPE_FORGE || type == TYPE_FORGE_MODLIST)
                return getForgeJSON().getString("description");
            else
                throw new UnsupportedOperationException("Can't get mod description: " + file.getName());
        }

        public String getID(){
            if(type == TYPE_FORGE || type == TYPE_FORGE_MODLIST)
                return getForgeJSON().getString("modid");
            else if(type == TYPE_FABRIC)
                return getJson().getString("id");
            else
                throw new UnsupportedOperationException("Can't get mod id: " + file.getName());
        }

        public String getIconPath(){
            try {
                String path;
                if (type == TYPE_FABRIC)
                    path = getJson().getString("icon");
                else if(type == TYPE_FORGE || type == TYPE_FORGE_MODLIST)
                    path = getForgeJSON().getString("logoFile");
                else
                    throw new UnsupportedOperationException("Can't get mod icon path: " + file.getName());

                if(path.startsWith("/"))
                    path = path.substring(1);
                return path;
            }catch (Exception ignored){ }
            return null;
        }

        public String getFilePath(){
            return file.getAbsolutePath();
        }

        public BufferedImage getIcon(){
            try {
                InputStream is = readZipFile(file.getAbsolutePath(), getIconPath());
                if(is != null)
                    return ImageIO.read(is);
                else{
                    HashMap<String, String> icons = new HashMap<String, String>(){{
                        put("ic2", "ic2_icon.png");
                    }};

                    if(icons.containsKey(getID()))
                        return ImageIO.read(ServerMain.class.getResourceAsStream("/" + icons.get(getID())));
                }
            }catch (Exception ignored){ }
            return null;
        }
    }
}
