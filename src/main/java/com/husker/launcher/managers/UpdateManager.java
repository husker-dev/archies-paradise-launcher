package com.husker.launcher.managers;

import com.husker.launcher.Launcher;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.minecraft.LaunchManager;
import com.husker.launcher.social.Social;
import com.husker.launcher.utils.IOUtils;
import com.husker.mio.MIO;
import com.husker.mio.ProgressArguments;
import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.DownloadingProcess;
import com.husker.mio.processes.UnzippingProcess;
import com.husker.mio.processes.ZippingProcess;
import com.husker.net.Get;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UpdateManager {

    private static final Logger log = LogManager.getLogger(UpdateManager.class);
    public static boolean enable = true;

    private static final String UpdateFolder = new File(LaunchManager.clientsFolderPath, "launcher_update").getAbsolutePath();

    private static final String[] filesToDelete = new String[]{
            "lib", "resources", "launcher.exe", "shell.sh"
    };

    static{
        log.info("Current launcher version: " + Launcher.VERSION);
        new Thread(() -> {
            try {
                MIO.delete(UpdateFolder);
                MIO.delete(LaunchManager.clientsFolderPath + "/updater.jar");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getLatestVersion() throws UpdateException{
        try {
            return API.getJSON(ApiMethod.create("launcher.getVersion")).getString("version");
        }catch (Exception ex){
            ex.printStackTrace();
            throw new UpdateException(UpdateException.Stage.VERSION_GET, 1, ex);
        }
    }

    public static boolean hasUpdate() throws UpdateException{
        if(!enable)
            return false;
        return !Launcher.VERSION.equals(getLatestVersion());
    }

    public static String getDownloadLink() throws UpdateException{
        return "https://github.com/" + Social.GitHub.getRepository() + "/releases/download/" + getLatestVersion() + "/release.zip";
    }

    public static void processUpdating(UpdateProcessor processor){
        try {
            if(new File("build.gradle").exists()){
                log.info("Опять хочешь, что бы половина проекта удалилась!?");
                return;
            }

            new DeletingProcess(UpdateFolder).addProgressListener(processor::onRemoveOld).startSync();
            Files.createDirectories(Paths.get(UpdateFolder));

            processor.onConnecting();
            log.info("Update URL: " + getDownloadLink());
            new DownloadingProcess(getDownloadLink(), UpdateFolder + "/update_archive.zip").addProgressListener(processor::onDownloading).startSync();

            new UnzippingProcess(UpdateFolder + "/update_archive.zip", UpdateFolder).addProgressListener(processor::onUnzipping).startSync();
            new DeletingProcess(UpdateFolder + "/update_archive.zip").addProgressListener(processor::onZipRemoving).startSync();
            if(Files.exists(Paths.get(LaunchManager.clientsFolderPath + "/updater.jar")))
                MIO.delete(LaunchManager.clientsFolderPath + "/updater.jar");
            MIO.copy(UpdateFolder + "/updater.jar", LaunchManager.clientsFolderPath);
            MIO.delete(UpdateFolder + "/updater.jar");

            processor.onReboot();
            applyUpdate();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public interface UpdateProcessor{
        void onRemoveOld(ProgressArguments<DeletingProcess> arguments);
        void onConnecting();
        void onDownloading(ProgressArguments<DownloadingProcess> arguments);
        void onUnzipping(ProgressArguments<UnzippingProcess> arguments);
        void onZipRemoving(ProgressArguments<DeletingProcess> arguments);
        void onUnpack(double percent);
        void onReboot();
    }

    private static void applyUpdate() throws UpdateException{
        if(!Files.exists(Paths.get("./updater.jar")))
            throw new UpdateException(UpdateException.Stage.REBOOT, 7, new IOException("updater.jar doesn't exist"));
        try {
            Runtime.getRuntime().exec("java -jar \"" + new File("./updater.jar").getAbsolutePath() + "\" --delete=\"" + String.join(",", filesToDelete) + "\" --folder=\"" + new File(UpdateFolder).getAbsolutePath() + "\" --launcher=\"" + new File("./").getAbsolutePath() + "\"");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.REBOOT, 8, e);
        }
    }

    static public class UpdateException extends Exception{

        public enum Stage{
            VERSION_GET,
            DOWNLOAD,
            UNZIP,
            UNPACK,
            REBOOT,
        }

        private final Exception exception;
        private final Stage stage;
        private final int code;

        public UpdateException(Stage stage, int code, Exception exception) {
            super(exception.getMessage());
            this.exception = exception;
            this.stage = stage;
            this.code = code;
        }

        public Exception getException(){
            return exception;
        }

        public Stage getStage(){
            return stage;
        }

        public int getCode() {
            return code;
        }
    }
}
