package com.husker.launcher.server.core;

import com.husker.mio.FSUtils;
import com.husker.mio.MIO;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.ZippingProcess;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class Client {

    private static final Logger log = LogManager.getLogger(Client.class);
    public static final String folder = "./loaded_clients";

    public static ArrayList<String> updatingClients = new ArrayList<>();

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

    public Client(String id) throws IllegalAccessException {
        this.id = id;
        if (!Files.exists(Paths.get(folder + "/" + id)))
            throw new NullPointerException("Can't find client with id: " + id);
        if(updatingClients.contains(id))
            throw new IllegalAccessException("Client is updating");
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

    public JSONObject getSizeInfo() throws Exception {
        return new JSONObject(MIO.readFileText(folder + "/" + id + "/files_info.json"));
    }

    public JSONObject getClientInfo() throws Exception {
        return new JSONObject(MIO.readFileText(folder + "/" + id + "/client_info.json"));
    }

    public List<SimpleModInfo> getModsInfo() throws Exception {
        JSONArray jsonMods = getClientInfo().getJSONArray("mods");

        // Sorting
        List<SimpleModInfo> mods = new ArrayList<>();
        for(int i = 0; i < jsonMods.length(); i++)
            mods.add(new SimpleModInfo(jsonMods.getJSONObject(i), i));
        Collections.sort(mods);

        return mods;
    }

    public String getModIcon(int index) throws Exception {
        return getModsInfo().get(index).iconBase64;
    }

    public JSONObject getModsInfo(int index) throws Exception {
        List<SimpleModInfo> mods = getModsInfo();
        JSONArray out = new JSONArray();

        if(index == -1) {
            for(SimpleModInfo mod : mods)
                out.put(mod.getJSON());
        }else
            out.put(mods.get(index).getJSON());

        return new JSONObject(){{
            put("mods", out);
        }};
    }

    static class SimpleModInfo implements Comparable<SimpleModInfo>{
        private final String name;
        private final Boolean icon;
        private final String iconBase64;
        private final JSONObject jsonObject;

        public SimpleModInfo(JSONObject info, int index){
            name = info.getString("name");
            iconBase64 = info.getString("icon");
            icon = !iconBase64.equals("null");

            jsonObject = info;
            jsonObject.put("icon", icon);
            jsonObject.put("index", index);
        }

        public int compareTo(SimpleModInfo o) {
            if(icon.compareTo(o.icon) != 0)
                return -icon.compareTo(o.icon);
            else
                return name.compareTo(o.name);
        }

        public String getIconBase64(){
            return iconBase64;
        }

        public JSONObject getJSON(){
            return jsonObject;
        }
    }

    public static String createId(){
        return System.currentTimeMillis() + "";
    }

    public static void archive(String id){
        archive(null, id);
    }

    public static void archive(String name, String id){
        updatingClients.add(id);
        try {
            log.info("Updating...");

            String to = folder + "/" + id;
            String from = "./received_clients_tmp/" + id;

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

            for(File file : FSUtils.getChildren(new File(to)))
                MIO.delete(file);

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
            folderInfo.put("mods", FSUtils.getFileSize(new File(from + "/mods")));
            folderInfo.put("versions", FSUtils.getFileSize(new File(from + "/versions")));

            // Zipping
            addFilesToZip(from + "/mods", to + "/mods.zip");
            addFilesToZip(from + "/versions", to + "/versions.zip");

            // Removing mods and versions folders
            removeIfExist(from + "/mods");
            removeIfExist(from + "/versions");

            // Zipping "other" files
            folderInfo.put("other", FSUtils.getFileSize(new File(from)));
            addFilesToZip(FSUtils.getChildren(new File(from)).toArray(new File[0]),  to + "/other.zip");

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

            MIO.delete(from);
            log.info("Updated!");
        } catch (Exception e) {
            log.error("Error while updating: " + e.getMessage());
            e.printStackTrace();
        }
        updatingClients.remove(id);
    }

    private static void removeIfExist(String path){
        try {
            if (Files.exists(Paths.get(path))) {
                log.info("Removing \"" + path + "\"...");
                MIO.delete(path);
            }
        }catch (Exception ex){
            log.error("Error while removing: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void addFilesToZip(String sourceFile, String zipPath) throws Exception {
        addFilesToZip(new File[]{new File(sourceFile)}, zipPath);
    }

    public static void addFilesToZip(File[] sourceFiles, String zipPath) throws Exception {
        log.info("Archiving \"" + Arrays.toString(sourceFiles) + "\" -> \"" + zipPath + "\"...");

        new ZippingProcess(sourceFiles, new File(zipPath)).addProgressListener(new Consumer<ProgressArguments<ZippingProcess>>() {
            int old_percent;
            public void accept(ProgressArguments<ZippingProcess> e) {
                if((int)e.getPercent() != old_percent) {
                    System.out.println("Zipping " + zipPath + ": " + (int)e.getPercent() + "%");
                    old_percent = (int)e.getPercent();
                }
            }
        }).startSync();
    }

}