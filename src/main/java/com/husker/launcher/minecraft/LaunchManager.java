package com.husker.launcher.minecraft;

import com.husker.launcher.Resources;
import com.husker.launcher.User;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.utils.SystemUtils;
import com.husker.mio.FSUtils;
import com.husker.mio.MIO;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.CopyingProcess;
import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.DownloadingProcess;
import com.husker.mio.processes.UnzippingProcess;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static java.io.File.separator;

public class LaunchManager {

    private static final Logger log = LogManager.getLogger(LaunchManager.class);
    public static final File clientsFolderFile;
    public static final String clientsFolderPath;

    private static final String[] toSaveFiles = {"custom-models", "options.txt", "optionsof.txt", "custom-models.json", "screenshots"};

    static {
        if(SystemUtils.isWindows())
            clientsFolderFile = new File(System.getenv("APPDATA") + separator + "." + LauncherConfig.getFolderName().toLowerCase().replaceAll("\\s", "").replace("'", ""));
        else
            clientsFolderFile = new File(SystemUtils.getSettingsFolder(), "clients");
        clientsFolderPath = clientsFolderFile.getAbsolutePath();
    }

    public static String getClientPath(String clientId){
        return clientsFolderPath + "/" + clientId;
    }

    public enum ClientState{
        ERROR,
        UPDATING,
        PLAY,
        UPDATE,
        DOWNLOAD
    }

    public static String getCurrentClientVersion(String clientId){
        try {
            if(Files.exists(Paths.get(getClientPath(clientId) + "/client_info.json"))){
                JSONObject object = new JSONObject(MIO.readFileText(getClientPath(clientId) + "/client_info.json"));
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
                if(!Files.exists(Paths.get(getClientPath(clientId) + "/versions")) || !Files.exists(Paths.get(getClientPath(clientId) + "/mods")))
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

    public static void playOrDownload(String clientId, User user, LaunchListener listener){
        new Thread(() -> {
            try {
                // todo: когда-нибудь надо убрать
                if(SystemUtils.isWindows() && Files.exists(Paths.get("clients"))) {
                    new CopyingProcess("clients", clientsFolderPath).setCopyOnlyContent(true).addProgressListener(listener::onOldCopying).startSync();
                    MIO.delete("clients");
                }

                ClientState state = getClientState(clientId);
                log.info("Client state: " + state);
                if (state == ClientState.ERROR) {
                    listener.onError();
                    return;
                }
                if (state == ClientState.UPDATE || state == ClientState.DOWNLOAD) {
                    listener.onConfigSave();
                    File configsDirectory = new File(clientsFolderPath + "/" + clientId + "_to_save");
                    for(String file : toSaveFiles){
                        Files.createDirectories(Paths.get(configsDirectory.getAbsolutePath()));
                        if(Files.exists(Paths.get(getClientPath(clientId) + separator + file)))
                            MIO.copy(getClientPath(clientId) + separator + file, configsDirectory.getAbsolutePath());
                    }

                    new DeletingProcess(getClientPath(clientId)).addProgressListener(listener::onRemoveOld).startSync();
                    Files.createDirectories(Paths.get(getClientPath(clientId)));

                    receiveClientPart(clientId,"versions", listener::onVersionsDownloading);
                    receiveClientPart(clientId,"mods", listener::onModsDownloading);
                    receiveClientPart(clientId,"other", listener::onOtherDownloading);

                    unzipAndDelete(clientId, "versions", listener::onVersionsUnzipping, listener::onVersionsZipRemoving);
                    unzipAndDelete(clientId, "mods", listener::onModsUnzipping, listener::onModsZipRemoving);
                    unzipAndDelete(clientId, "other", listener::onOtherUnzipping, listener::onOtherZipRemoving);

                    MIO.writeText(new JSONObject() {{
                        put("version", API.Client.getJarVersion(clientId));
                        put("build", API.Client.getClientVersion(clientId));
                        put("build_id", API.Client.getShortClientVersion(clientId));
                    }}.toString(), getClientPath(clientId) + separator + "client_info.json");

                    listener.onConfigApply();

                    if(configsDirectory.exists()){
                        for(File file : FSUtils.getChildren(configsDirectory))
                            MIO.delete(new File(getClientPath(clientId), file.getName()));
                        new CopyingProcess(configsDirectory, new File(getClientPath(clientId))).setCopyOnlyContent(true).startSync();
                        MIO.delete(configsDirectory);
                    }

                    listener.onClientUpdated();
                }
                if (state == ClientState.PLAY) {
                    listener.onClientChecking();

                    MinecraftStarter starter = new MinecraftStarter(getClientPath(clientId)) {{
                        setNickname(user.getId() + "");
                        setFullscreen(!LauncherSettings.isWindowed());
                        setRam(LauncherSettings.getRAM());
                        setIcon(Resources.Icon);
                    }};
                    starter.clearAdditionMods();

                    while (true) {
                        boolean[] eq = AntiCheat.checkMD5(new File(getClientPath(clientId)));
                        boolean modsDiff = !eq[0];
                        boolean versionsDiff = !eq[1];

                        if (modsDiff || versionsDiff) {
                            JOptionPane.showMessageDialog(null, "Файлы игры отличаются от файлов на сервере!", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);

                            // Removing old
                            if(modsDiff)
                                new DeletingProcess(getClientPath(clientId) + separator + "mods").addProgressListener(listener::onModsRemoving).startSync();
                            if(versionsDiff)
                                new DeletingProcess(getClientPath(clientId) + separator + "versions").addProgressListener(listener::onVersionsRemoving).startSync();

                            // Downloading
                            if(modsDiff)
                                receiveClientPart(clientId,"mods", listener::onModsDownloading);
                            if(versionsDiff)
                                receiveClientPart(clientId,"versions", listener::onVersionsDownloading);

                            // Unzip and delete
                            if(modsDiff)
                                unzipAndDelete(clientId, "mods", listener::onModsUnzipping, listener::onModsZipRemoving);
                            if(versionsDiff)
                                unzipAndDelete(clientId, "versions", listener::onVersionsUnzipping, listener::onVersionsZipRemoving);
                        } else
                            break;
                    }

                    listener.onClientStarting();
                    user.bindIP();
                    starter.launch();

                    listener.onClientStarted();
                    System.gc();

                    starter.joinThread();

                    listener.onClientClosed();
                }
            }catch (Exception ex){
                ex.printStackTrace();
                listener.onError();
            }
        }).start();
    }

    private static void unzipAndDelete(String clientId, String name, Consumer<ProgressArguments<UnzippingProcess>> unzip, Consumer<ProgressArguments<DeletingProcess>> remove) throws Exception {
        new UnzippingProcess(getClientPath(clientId) + separator + name + ".zip", getClientPath(clientId)).addProgressListener(unzip).startSync();
        new DeletingProcess(getClientPath(clientId) + separator + name + ".zip").addProgressListener(remove).startSync();
    }

    private static void receiveClientPart(String clientId, String name, Consumer<ProgressArguments<DownloadingProcess>> listener) throws Exception {
        log.info("Receiving \"" + name + "\"...");
        String url = API.getMethodUrl(ApiMethod.create("clients.get").set("id", clientId).set("name", name));
        String filePath = getClientPath(clientId) + separator + name + ".zip";
        new DownloadingProcess(url, filePath).addProgressListener(listener).startSync();
    }

    public interface LaunchListener {

        void onConfigSave();
        void onConfigApply();

        void onError();
        void onRemoveOld(ProgressArguments<DeletingProcess> event);
        void onVersionsDownloading(ProgressArguments<DownloadingProcess> event);
        void onModsDownloading(ProgressArguments<DownloadingProcess> event);
        void onOtherDownloading(ProgressArguments<DownloadingProcess> event);

        void onModsUnzipping(ProgressArguments<UnzippingProcess> event);
        void onModsZipRemoving(ProgressArguments<DeletingProcess> event);
        void onVersionsUnzipping(ProgressArguments<UnzippingProcess> event);
        void onVersionsZipRemoving(ProgressArguments<DeletingProcess> event);
        void onOtherUnzipping(ProgressArguments<UnzippingProcess> event);
        void onOtherZipRemoving(ProgressArguments<DeletingProcess> event);

        void onOldCopying(ProgressArguments<CopyingProcess> event);

        void onClientUpdated();

        void onClientChecking();
        void onClientStarting();
        void onClientStarted();
        void onClientClosed();

        void onModsRemoving(ProgressArguments<DeletingProcess> event);
        void onVersionsRemoving(ProgressArguments<DeletingProcess> event);
    }
}
