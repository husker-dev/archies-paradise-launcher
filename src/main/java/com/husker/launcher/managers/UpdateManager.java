package com.husker.launcher.managers;

import com.husker.launcher.Launcher;
import com.husker.launcher.utils.settings.SettingsFile;
import com.husker.launcher.utils.ConsoleUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

public class UpdateManager {

    public static final String VERSION = "0.0.1";

    public static boolean enable = true;

    public String updateFolder = "./launcher_update";
    public String zipName = "archive";
    private static final String[] filesToSave = new String[]{"launcher.cfg"};

    public SettingsFile config = new SettingsFile("update.cfg");

    private String html = null;
    private ZipFile file;
    private Exception lastUnzipException;

    private final Launcher launcher;

    public UpdateManager(Launcher launcher){
        this.launcher = launcher;
        ConsoleUtils.printDebug(getClass(), "Current launcher version: " + VERSION);
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

    public void downloadUpdate(Consumer<Integer> progress) throws UpdateException{
        progress.accept(0);

        String downloadLink = getDownloadLink();

        BufferedInputStream in;
        try {
            URLConnection connection = new URL(downloadLink).openConnection();
            connection.connect();

            long fileSize = connection.getContentLength();
            long currentSize = 0;

            in = new BufferedInputStream(new URL(downloadLink).openStream());

            Files.createDirectories(Paths.get(updateFolder));
            for (File fileToDelete : Objects.requireNonNull(new File(updateFolder).listFiles()))
                deleteDirectory(fileToDelete);

            FileOutputStream fileOutputStream = new FileOutputStream(updateFolder + "/" + zipName + ".zip");
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);

                currentSize += bytesRead;
                progress.accept((int) ((float) currentSize / (float) fileSize * 100f));
            }
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.DOWNLOAD, 1, e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.DOWNLOAD, 2, e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.DOWNLOAD, 3, e);
        }
    }

    public void unzipUpdate(Consumer<Integer> progress) throws UpdateException{
        progress.accept(0);

        file = new ZipFile(updateFolder + "/" + zipName + ".zip");
        new Thread(() -> {
            try {
                file.extractAll(updateFolder);
            } catch (ZipException e) {
                e.printStackTrace();
                lastUnzipException = e;
                file = null;
            }
        }).start();

        while(file != null && file.getProgressMonitor().getPercentDone() < 100) {
            progress.accept(file.getProgressMonitor().getPercentDone());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(file == null)
            throw new UpdateException(UpdateException.Stage.UNZIP, 4, lastUnzipException);
        try {
            while (Files.exists(Paths.get(updateFolder + "/" + zipName + ".zip")))
                Files.deleteIfExists(Paths.get(updateFolder + "/" + zipName + ".zip"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.UNZIP, 5, e);
        }
    }

    public void unpackUpdateFolder(Consumer<Integer> progress) throws UpdateException{
        progress.accept(0);

        try {
            String folder = Objects.requireNonNull(new File(updateFolder).list())[0];

            int files = 1 + Objects.requireNonNull(new File(updateFolder + "/" + folder).list()).length;
            int currentFile = 0;

            for(String fileToMove : Objects.requireNonNull(new File(updateFolder + "/" + folder).list())) {
                Files.move(Paths.get(updateFolder + "/" + folder + "/" + fileToMove), Paths.get(updateFolder + "/" + fileToMove));
                currentFile ++;

                progress.accept((int) ((float) currentFile / (float) files * 100f));
            }
            deleteDirectory(new File(updateFolder + "/" + folder));
        } catch (IOException e) {
            e.printStackTrace();
            throw new UpdateException(UpdateException.Stage.UNPACK, 6, e);
        }
    }

    public void rebootToApplyUpdate() throws UpdateException{
        if(!Files.exists(Paths.get("updater.jar")))
            throw new UpdateException(UpdateException.Stage.REBOOT, 7, new IOException("updater.jar not exist"));
        try {
            Runtime.getRuntime().exec("java -jar updater.jar " + String.join(" ", filesToSave));
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
