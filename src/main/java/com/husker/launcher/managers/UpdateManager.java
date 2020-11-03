package com.husker.launcher.managers;

import com.husker.launcher.Launcher;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.settings.SettingsFile;
import com.husker.launcher.utils.ConsoleUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class UpdateManager {

    public static final String VERSION = "0.0.1";
    private static final String[] filesToSave = new String[]{"client"};

    public static boolean enable = true;

    public String updateFolder = "./launcher_update";
    public String zipName = "archive";

    public SettingsFile config = new SettingsFile("update.cfg");

    private String html = null;

    private final Launcher launcher;

    public UpdateManager(Launcher launcher){
        this.launcher = launcher;
        ConsoleUtils.printDebug(getClass(), "Current launcher version: " + VERSION);
        IOUtils.delete(updateFolder);
    }

    public String getLatestVersion() throws UpdateException{
        checkHTML();

        try {
            return html.split("<li class=\"d-block mb-1\">")[1].split("class=\"css-truncate-target\"")[1].split("\">")[1].split("</span>")[0];
        }catch (Exception ex){
            throw new UpdateException(UpdateException.Stage.VERSION_GET, 0, new Exception("Failed to get html from url: " + config.get("github")));
        }
    }

    private void checkHTML() throws UpdateException{
        if(html == null) {
            try {
                html = launcher.NetManager.getURLContent(config.get("github") + "/releases/latest");
            }catch (IOException e) {
                throw new UpdateException(UpdateException.Stage.VERSION_GET, 0, new Exception("Failed to get html from url: " + config.get("github")));
            }
        }
    }

    public boolean hasUpdate() throws UpdateException{
        if(!enable)
            return false;
        return !VERSION.equals(getLatestVersion());
    }

    public String getDownloadLink() throws UpdateException{
        checkHTML();

        try {
            return "https://github.com/" + html.split("d-flex flex-justify-between flex-items-center py-1 py-md-2 Box-body px-2")[1].split("href=\"")[1].split("\"")[0];
        }catch (Exception ex){
            throw new UpdateException(UpdateException.Stage.VERSION_GET, 0, new Exception("Failed to get html from url: " + config.get("github")));
        }
    }

    public void downloadUpdate(Consumer<IOUtils.FileReceivingArguments> progress) throws UpdateException{
        try {
            IOUtils.delete(updateFolder);
            Files.createDirectories(Paths.get(updateFolder));

            IOUtils.receiveFile(getDownloadLink(), updateFolder + "/" + zipName + ".zip", progress);
        }catch (IOException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.DOWNLOAD, 1, e);
        }
    }

    public void unzipUpdate(Consumer<IOUtils.ZipArguments> progress) throws UpdateException{
        try{
            IOUtils.unzip(updateFolder + "/" + zipName + ".zip", updateFolder, progress);
            IOUtils.delete(updateFolder + "/" + zipName + ".zip");
        }catch (Exception ex){
            throw new UpdateException(UpdateException.Stage.UNZIP, 2, ex);
        }
    }

    public void unpackUpdateFolder(Consumer<Double> progress){
        IOUtils.moveDirectoryContent(IOUtils.fileList(updateFolder)[0].getAbsolutePath(), updateFolder, progress);
    }

    public void rebootToApplyUpdate() throws UpdateException{
        if(!Files.exists(Paths.get(updateFolder + "/updater.jar")))
            throw new UpdateException(UpdateException.Stage.REBOOT, 7, new IOException(updateFolder + "/updater.jar doesn't exist"));
        try {
            Runtime.getRuntime().exec("java -jar " + updateFolder + "/updater.jar --path=\"" + new File(".").getAbsolutePath() + "\" --save=\"" + String.join(",", filesToSave) + "\"");
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

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null)
            for (File file : allContents)
                deleteDirectory(file);
        return directoryToBeDeleted.delete();
    }
}
