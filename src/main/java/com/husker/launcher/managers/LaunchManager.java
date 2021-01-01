package com.husker.launcher.managers;

import com.alee.utils.general.Pair;
import com.husker.launcher.Resources;
import com.husker.launcher.User;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.minecraft.MinecraftClientInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.function.Consumer;

public class LaunchManager {

    private static final Logger log = LogManager.getLogger(LaunchManager.class);

    public enum ClientState{
        ERROR,
        UPDATING,
        PLAY,
        UPDATE,
        DOWNLOAD
    }

    public static String getCurrentClientVersion(String clientId){
        try {
            if(Files.exists(Paths.get("clients/" + clientId + "/client_info.json"))){
                JSONObject object = new JSONObject(IOUtils.readFileText("clients/" + clientId + "/client_info.json"));
                return object.getString("build");
            }
        }catch (Exception ignored){
        }
        return "-1";
    }

    public static ClientState getClientState(String clientId){
        try {
            String currentVersion = getCurrentClientVersion(clientId);
            String version = API.Client.getClientVersion(clientId);

            if(version == null || version.equals("-1"))
                return ClientState.ERROR;
            if(currentVersion.equals(version)) {
                if(!Files.exists(Paths.get("./clients/" + clientId + "/versions")) || !Files.exists(Paths.get("./clients/" + clientId + "/mods")))
                    return ClientState.DOWNLOAD;
                return ClientState.PLAY;
            }else
                return currentVersion.equals("-1") ? ClientState.DOWNLOAD : ClientState.UPDATE;
        }catch (API.ClientIsUpdatingException ex){
            return ClientState.UPDATING;
        }catch (Exception ex){
            return ClientState.ERROR;
        }
    }

    public static void playOrDownload(String clientId, User user, Consumer<DownloadingProcessArguments> process){
        new Thread(() -> {
            try {
                ClientState state = getClientState(clientId);
                log.info("Client state: " + state);
                if (state == ClientState.ERROR) {
                    process.accept(new DownloadingProcessArguments(-1, 0));
                    return;
                }
                if (state == ClientState.UPDATE || state == ClientState.DOWNLOAD) {
                    process.accept(new DownloadingProcessArguments(0));

                    IOUtils.delete("clients/" + clientId, percent -> process.accept(new DownloadingProcessArguments(0, percent)));
                    Files.createDirectories(Paths.get("clients/" + clientId));

                    // Downloading
                    JSONObject downloadInfo = API.Client.getSizeInfo(clientId);
                    JSONObject zipInfo = downloadInfo.getJSONObject("zip");
                    long zipVersions = zipInfo.getLong("versions");
                    long zipMods = zipInfo.getLong("mods");
                    long zipOther = zipInfo.getLong("other");
                    long zipFullSize = zipVersions + zipMods + zipOther;

                    process.accept(new DownloadingProcessArguments(1));
                    receiveClientPart(clientId,"versions", args -> process.accept(new DownloadingProcessArguments(1) {{
                        setCurrentSize(args.getCurrentSize());
                        setFullSize(zipFullSize);
                        setSpeed(args.getSpeed());
                    }}));
                    receiveClientPart(clientId,"other", args -> process.accept(new DownloadingProcessArguments(1) {{
                        setCurrentSize(zipVersions + args.getCurrentSize());
                        setFullSize(zipFullSize);
                        setSpeed(args.getSpeed());
                    }}));
                    receiveClientPart(clientId,"mods", args -> process.accept(new DownloadingProcessArguments(1) {{
                        setCurrentSize(zipOther + zipVersions + args.getCurrentSize());
                        setFullSize(zipFullSize);
                        setSpeed(args.getSpeed());
                    }}));

                    // Unzipping
                    JSONObject folderInfo = downloadInfo.getJSONObject("folders");
                    long folderVersions = folderInfo.getLong("versions");
                    long folderMods = folderInfo.getLong("mods");
                    long folderOther = folderInfo.getLong("other");
                    long folderFullSize = folderVersions + folderMods + folderOther;

                    process.accept(new DownloadingProcessArguments(2));
                    simpleUnzip(clientId,"clients/" + clientId + "/mods.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                        setCurrentSize(args.getCurrentSize());
                        setFullSize(folderFullSize);
                    }}));
                    simpleUnzip(clientId,"clients/" + clientId + "/versions.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                        setCurrentSize(folderMods + args.getCurrentSize());
                        setFullSize(folderFullSize);
                    }}));
                    IOUtils.unzip("clients/" + clientId + "/other.zip", "clients/" + clientId + "/tmp_", args -> process.accept(new DownloadingProcessArguments(2) {{
                        setCurrentSize(folderVersions + folderMods + args.getCurrentSize());
                        setFullSize(folderFullSize);
                    }}));

                    // Moving
                    IOUtils.moveDirectoryContent("clients/" + clientId + "/tmp_/" + clientId, "clients/" + clientId, percent -> process.accept(new DownloadingProcessArguments(3, percent)));

                    // Removing tmp files
                    IOUtils.delete("clients/" + clientId + "/tmp_", percent -> process.accept(new DownloadingProcessArguments(4, percent / 2)));
                    IOUtils.delete("clients/" + clientId + "/other.zip", percent -> process.accept(new DownloadingProcessArguments(4, 50 + percent / 2)));

                    // Saving client info
                    IOUtils.writeFileText("clients/" + clientId + "/client_info.json", new JSONObject() {{
                        put("version", API.Client.getJarVersion(clientId));
                        put("build", API.Client.getClientVersion(clientId));
                        put("build_id", API.Client.getShortClientVersion(clientId));
                    }}.toString());

                    process.accept(new DownloadingProcessArguments(-1));
                }
                if (state == ClientState.PLAY) {
                    process.accept(new DownloadingProcessArguments(5));

                    MinecraftStarter starter = new MinecraftStarter("clients/" + clientId) {{
                        setNickname(user.getId() + "");
                        setFullscreen(!LauncherSettings.isWindowed());
                        setRam(LauncherSettings.getRAM());
                        setIcon(Resources.Icon);
                    }};
                    starter.clearSkinMod();

                    while (true) {
                        String md5_mods = getModsMD5(clientId);
                        String md5_client = getClientMD5(clientId);

                        Pair<Boolean, Boolean> eq = API.Client.checksum(clientId, md5_mods, md5_client);

                        if (!eq.getKey() || !eq.getValue()) {
                            JOptionPane.showMessageDialog(null, "Файлы игры отличаются от файлов на сервере!", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);

                            JSONObject fileSizes = API.Client.getSizeInfo(clientId);
                            JSONObject folderSizes = fileSizes.getJSONObject("folders");
                            long folderVersions = folderSizes.getLong("versions");
                            long folderMods = folderSizes.getLong("mods");

                            if (!eq.getKey()) {
                                if (Files.exists(Paths.get("clients/" + clientId + "/mods")))
                                    IOUtils.delete("clients/" + clientId + "/mods", percent -> process.accept(new DownloadingProcessArguments(0, percent)));

                                receiveClientPart(clientId,"mods", args -> process.accept(new DownloadingProcessArguments(1) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(args.getSize());
                                    setSpeed(args.getSpeed());
                                }}));

                                simpleUnzip(clientId,"clients/" + clientId + "/mods.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(folderMods);
                                }}));
                            }

                            if (!eq.getValue()) {
                                if (Files.exists(Paths.get("clients/" + clientId + "/versions")))
                                    IOUtils.delete("clients/" + clientId + "/versions", percent -> process.accept(new DownloadingProcessArguments(0, percent)));

                                receiveClientPart(clientId,"versions", args -> process.accept(new DownloadingProcessArguments(1) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(args.getSize());
                                    setSpeed(args.getSpeed());
                                }}));

                                simpleUnzip(clientId, "clients/" + clientId + "/versions.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(folderVersions);
                                }}));
                            }
                        } else
                            break;
                    }

                    user.bindIP();
                    starter.applySkinMod();
                    starter.launch();

                    process.accept(new DownloadingProcessArguments(6));
                    System.gc();
                    starter.joinThread();
                    starter.clearSkinMod();
                    process.accept(new DownloadingProcessArguments(-1));

                }
            }catch (Exception ex){
                ex.printStackTrace();
                process.accept(new DownloadingProcessArguments(-2));
            }
        }).start();
    }

    private static void simpleUnzip(String clientId, String path, Consumer<IOUtils.ZipArguments> process) throws IOException {
        IOUtils.unzip(path, "clients/" + clientId, process);
        IOUtils.delete(path);
    }

    private static void receiveClientPart(String clientId, String name, Consumer<IOUtils.FileReceivingArguments> listener) throws IOException {
        log.info("Receiving \"" + name + "\"...");
        IOUtils.receiveFile(API.getMethodUrl(ApiMethod.create("clients.get").set("id", clientId).set("name", name)), "clients/" + clientId + "/" + name + ".zip", listener);
    }

    private static String getModsMD5(String clientId){
        try{
            Vector<FileInputStream> streams = new Vector<>();
            for(File file : new File("clients/" + clientId + "/mods").listFiles(file -> file.getName().endsWith(".jar"))){
                try {
                    streams.add(new FileInputStream(file));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return DigestUtils.md5Hex(new SequenceInputStream(streams.elements()));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getClientMD5(String clientId){
        try{
            File clientFile = MinecraftClientInfo.getJar(new File("clients/" + clientId));
            return DigestUtils.md5Hex(new FileInputStream(clientFile));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static class DownloadingProcessArguments {

        private int processId;

        private double current;
        private double full;
        private double speed;

        public DownloadingProcessArguments(int id){
            this(id, 0);
        }

        public DownloadingProcessArguments(int id, double percent){
            this.processId = id;
            full = 100;
            current = percent;
        }

        public int getProcessId() {
            return processId;
        }

        public double getPercent() {
            return current / full * 100d;
        }

        public void setProcessId(int processId) {
            this.processId = processId;
        }

        public void setCurrentSize(double current){
            this.current = current;
        }

        public void setFullSize(double full){
            this.full = full;
        }

        public double getFullSize(){
            return full;
        }

        public double getCurrentSize(){
            return current;
        }

        public void setSpeed(double speed){
            this.speed = speed;
        }

        public double getSpeed(){
            return speed;
        }
    }
}
