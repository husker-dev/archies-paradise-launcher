package com.husker.launcher.managers;

import com.alee.utils.general.Pair;
import com.husker.launcher.Launcher;
import com.husker.launcher.User;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        PLAY,
        UPDATE,
        DOWNLOAD
    }

    public static String getCurrentClientVersion(){
        try {
            if(Files.exists(Paths.get("client/client_info.json"))){
                JSONObject object = new JSONObject(IOUtils.readFileText("client/client_info.json"));
                return object.getString("build");
            }
        }catch (Exception ignored){
        }
        return "-1";
    }

    public static ClientState getClientState(){
        try {
            String currentVersion = getCurrentClientVersion();
            String version = API.Client.getClientVersion();

            if(version.equals("-1"))
                return ClientState.ERROR;
            if(currentVersion.equals(version))
                return ClientState.PLAY;
            else
                return currentVersion.equals("-1") ? ClientState.DOWNLOAD : ClientState.UPDATE;
        }catch (API.APIException ex){
            return ClientState.ERROR;
        }
    }

    public static void playOrDownload(User user, Consumer<DownloadingProcessArguments> process){
        new Thread(() -> {
            try {
                ClientState state = getClientState();
                log.info("Client state: " + state);
                if (state == ClientState.ERROR)
                    return;
                if (state == ClientState.UPDATE || state == ClientState.DOWNLOAD) {

                    process.accept(new DownloadingProcessArguments(0));
                    if (Files.exists(Paths.get("client")))
                        IOUtils.delete("client", percent -> process.accept(new DownloadingProcessArguments(0, percent)));
                    Files.createDirectories(Paths.get("client"));

                    // Downloading
                    JSONObject downloadInfo = API.getJSON(ApiMethod.create("client.getSizeInfo"));
                    JSONObject zipInfo = downloadInfo.getJSONObject("zip");
                    long zipVersions = zipInfo.getLong("versions");
                    long zipMods = zipInfo.getLong("mods");
                    long zipOther = zipInfo.getLong("other");
                    long zipFullSize = zipVersions + zipMods + zipOther;

                    process.accept(new DownloadingProcessArguments(1));
                    receiveClientPart("versions", args -> process.accept(new DownloadingProcessArguments(1) {{
                        setCurrentSize(args.getCurrentSize());
                        setFullSize(zipFullSize);
                        setSpeed(args.getSpeed());
                    }}));
                    receiveClientPart("other", args -> process.accept(new DownloadingProcessArguments(1) {{
                        setCurrentSize(zipVersions + args.getCurrentSize());
                        setFullSize(zipFullSize);
                        setSpeed(args.getSpeed());
                    }}));
                    receiveClientPart("mods", args -> process.accept(new DownloadingProcessArguments(1) {{
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
                    simpleUnzip("client/mods.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                        setCurrentSize(args.getCurrentSize());
                        setFullSize(folderFullSize);
                    }}));
                    simpleUnzip("client/versions.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                        setCurrentSize(folderMods + args.getCurrentSize());
                        setFullSize(folderFullSize);
                    }}));
                    IOUtils.unzip("client/other.zip", "client/tmp_", args -> process.accept(new DownloadingProcessArguments(2) {{
                        setCurrentSize(folderVersions + folderMods + args.getCurrentSize());
                        setFullSize(folderFullSize);
                    }}));

                    // Moving
                    IOUtils.moveDirectoryContent("client/tmp_/update", "client", percent -> process.accept(new DownloadingProcessArguments(3, percent)));

                    // Removing tmp files
                    IOUtils.delete("client/tmp_", percent -> process.accept(new DownloadingProcessArguments(4, percent / 2)));
                    IOUtils.delete("client/other.zip", percent -> process.accept(new DownloadingProcessArguments(4, 50 + percent / 2)));

                    // Saving client info
                    IOUtils.writeFileText("client/client_info.json", new JSONObject() {{
                        put("version", API.Client.getJarVersion());
                        put("build", API.Client.getClientVersion());
                        put("build_id", API.Client.getShortClientVersion());
                    }}.toString());

                    process.accept(new DownloadingProcessArguments(-1));
                }
                if (state == ClientState.PLAY) {

                    process.accept(new DownloadingProcessArguments(5));

                    while (true) {
                        String md5_mods = getModsMD5();
                        String md5_client = getClientMD5();

                        Pair<Boolean, Boolean> eq = API.Client.checkSum(md5_mods, md5_client);

                        if (!eq.getKey() || !eq.getValue()) {
                            JOptionPane.showMessageDialog(null, "Файлы игры отличаются от файлов на сервере!", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);

                            JSONObject fileSizes = API.Client.getSizeInfo();
                            JSONObject folderSizes = fileSizes.getJSONObject("folders");
                            long folderVersions = folderSizes.getLong("versions");
                            long folderMods = folderSizes.getLong("mods");

                            if (!eq.getKey()) {
                                if (Files.exists(Paths.get("client/mods")))
                                    IOUtils.delete("client/mods", percent -> process.accept(new DownloadingProcessArguments(0, percent)));

                                receiveClientPart("mods", args -> process.accept(new DownloadingProcessArguments(1) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(args.getSize());
                                    setSpeed(args.getSpeed());
                                }}));

                                simpleUnzip("client/mods.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(folderMods);
                                }}));
                            }

                            if (!eq.getValue()) {
                                if (Files.exists(Paths.get("client/versions")))
                                    IOUtils.delete("client/versions", percent -> process.accept(new DownloadingProcessArguments(0, percent)));

                                receiveClientPart("versions", args -> process.accept(new DownloadingProcessArguments(1) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(args.getSize());
                                    setSpeed(args.getSpeed());
                                }}));

                                simpleUnzip("client/versions.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                                    setCurrentSize(args.getCurrentSize());
                                    setFullSize(folderVersions);
                                }}));
                            }
                        } else
                            break;
                    }

                    user.bindIP();
                    //JOptionPane.showMessageDialog(null, "В данный момент вход на сервер недоступен, но вы можете играть в одиночной игре", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);

                    MinecraftStarter starter = new MinecraftStarter("client") {{
                        setNickname(user.getNickname());
                        setFullscreen(!LauncherSettings.isWindowed());
                        setRam(LauncherSettings.getRAM());
                    }};
                    starter.launch();

                    process.accept(new DownloadingProcessArguments(6));
                    System.gc();
                    starter.joinThread();
                    process.accept(new DownloadingProcessArguments(-1));

                }
            }catch (Exception ex){
                ex.printStackTrace();
                process.accept(new DownloadingProcessArguments(-2));
            }
        }).start();
    }

    private static void simpleUnzip(String path, Consumer<IOUtils.ZipArguments> process) throws IOException {
        IOUtils.unzip(path, "client/", process);
        IOUtils.delete(path);
    }

    private static void receiveClientPart(String name, Consumer<IOUtils.FileReceivingArguments> listener) throws IOException {
        log.info("Receiving \"" + name + "\"...");
        IOUtils.receiveFile(API.getMethodUrl(ApiMethod.create("client.get").set("name", name)), "client/" + name + ".zip", listener);
    }

    private static String getModsMD5(){
        try{
            Vector<FileInputStream> streams = new Vector<>();
            for(File file : new File("client/mods").listFiles(file -> file.getName().endsWith(".jar"))){
                try {
                    streams.add(new FileInputStream(file));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return DigestUtils.md5Hex(new SequenceInputStream(streams.elements()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getClientMD5(){
        try{
            File clientFolder = new File("client/versions").listFiles(File::isDirectory)[0];
            File clientFile = clientFolder.listFiles(file -> file.getName().endsWith(".jar"))[0];

            return DigestUtils.md5Hex(new FileInputStream(clientFile));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
