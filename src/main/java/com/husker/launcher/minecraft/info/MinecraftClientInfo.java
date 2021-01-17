package com.husker.launcher.minecraft.info;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class MinecraftClientInfo {

    public enum ClientType{
        VANILLA,
        FORGE,
        OPTIFINE
    }

    public static final int maxSupportedVersion;
    public static final HashMap<Integer, Class<?>> jsonReaders = new HashMap<>();
    static {
        String classPath = MinecraftClientInfo.class.getCanonicalName().substring(0, MinecraftClientInfo.class.getCanonicalName().lastIndexOf("."));

        int maxVersion = -1;
        for(int i = 1; i < 40; i++){
            try {
                Class<?> c = Class.forName(classPath + ".impl.v" + i);
                if(MinecraftClientInfo.class.isAssignableFrom(c)) {
                    maxVersion = Math.max(maxVersion, i);
                    jsonReaders.put(i, c);
                }
            }catch (Exception ignored){}
        }
        maxSupportedVersion = maxVersion;
        Class<?> currentVersion = null;
        for(int i = 0; i < maxVersion; i++){
            if(jsonReaders.get(i) == null)
                jsonReaders.put(i, currentVersion);
            else
                currentVersion = jsonReaders.get(i);
        }
    }

    public static MinecraftClientInfo getDefaultClientInfo(File clientFolder) throws Exception{
        JSONObject json = getJSON(clientFolder);

        if(json == null)
            throw new NullPointerException("Can't find json file!");

        int version = Math.min(maxSupportedVersion, json.getInt("minimumLauncherVersion"));

        if (jsonReaders.get(version) == null)
            throw new NullPointerException("Can't find client reader!");

        Class<?> clazz = jsonReaders.get(json.getInt("minimumLauncherVersion"));
        return (MinecraftClientInfo) clazz.getConstructor(File.class).newInstance(clientFolder);
    }

    public ClientType getType(){
        String id = getJSON().getString("id").toLowerCase();
        if(id.contains("forge"))
            return ClientType.FORGE;
        if(id.contains("optifine"))
            return ClientType.OPTIFINE;
        return ClientType.VANILLA;
    }

    public static File getVersionFolder(File clientFolder){
        File versions = new File(clientFolder.getAbsoluteFile() + "/versions");
        return Objects.requireNonNull(versions.listFiles(file -> {
            if(file == null || file.list() == null || file.list().length < 2)
                return false;
            boolean jar = false;
            boolean json = false;
            for (File sub : file.listFiles()) {
                if (sub.getName().endsWith(".jar"))
                    jar = true;
                if (sub.getName().endsWith(".json"))
                    json = true;
            }
            return jar && json;
        }))[0];
    }

    public static File getJar(File clientFolder){
        return Objects.requireNonNull(getVersionFolder(clientFolder).listFiles(file -> file.getName().endsWith(".jar")))[0];
    }

    public static File getJSONFile(File clientFolder){
        return Objects.requireNonNull(getVersionFolder(clientFolder).listFiles(file -> file.getName().endsWith(".json")))[0];
    }

    public static JSONObject getJSON(File clientFolder){
        try {
            return new JSONObject(String.join("\n", Files.readAllLines(Paths.get(getJSONFile(clientFolder).getAbsolutePath()), StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final File clientFolder;
    private final JSONObject json;

    private final JvmArgumentsParameters jvmParameters = new JvmArgumentsParameters();
    private final GameArgumentsParameters gameParameters = new GameArgumentsParameters();

    private final ArrayList<String> additionLibraries = new ArrayList<>();

    public MinecraftClientInfo(File clientFolder){
        this.clientFolder = clientFolder;
        json = getJSON(clientFolder);
    }

    public File getClientFolder(){
        return clientFolder;
    }

    public File getVersionFolder(){
        return getVersionFolder(clientFolder);
    }

    public JSONObject getJSON(){
        return json;
    }

    public File getJSONFile(){
        return getJSONFile(clientFolder);
    }

    public File getJar(){
        return getJar(clientFolder);
    }

    public String getAssetsPath(){
        return clientFolder.getAbsolutePath() + "/assets";
    }

    public abstract String getVersion();
    public abstract String getAssetsId();
    public abstract String getGameArguments();
    public abstract String getJVMArguments();
    public abstract String[] getLibraries();
    public String getMainClass(){
        return getJSON().getString("mainClass");
    }

    public JvmArgumentsParameters getJvmParameters(){
        return jvmParameters;
    }
    public GameArgumentsParameters getGameParameters(){
        return gameParameters;
    }

    public String formatArguments(String args){
        GameArgumentsParameters parameters = getGameParameters();
        String[] out = {args};
        HashMap<String, String> par = new HashMap<String, String>(){{
            put("auth_player_name", parameters.getUserName());
            put("version", getVersion());
            put("version_name", getVersion());
            put("game_directory", "\"" + getClientFolder().getAbsolutePath() + "\"");
            put("assets_root", "\"" + getClientFolder().getAbsolutePath() + "\\assets\"");
            put("assets_index_name", getAssetsId());
            put("auth_uuid", parameters.getUUID());
            put("auth_access_token", "null");
            put("user_type", "legacy");
            put("user_properties", "null");
            put("version_type", getJSON().getString("type"));
            put("natives_directory", "\"" + getVersionFolder().getAbsolutePath() + "\\natives\"");
            put("launcher_name", "launcher");
            put("launcher_version", "1.0");
            put("classpath", "\"" + String.join(File.pathSeparator, getLibraries()) + "\" " + getMainClass() + "");
        }};
        par.forEach((key, val) -> out[0] = out[0].replace("${" + key + "}", val));
        return out[0];
    }

    public void addLibrary(String library){
        additionLibraries.add(library);
    }

    public void removeLibrary(String library){
        additionLibraries.remove(library);
    }

    public List<String> getAdditionLibraries(){
        return additionLibraries;
    }

    public static class JvmArgumentsParameters{
        private long ram = 1024 * 4;
        private String launcherName = "launcher";
        private String launcherVersion = "1.0";

        public long getRam() {
            return ram;
        }

        public void setRam(long ram) {
            this.ram = ram;
        }

        public String getLauncherName() {
            return launcherName;
        }

        public void setLauncherName(String launcherName) {
            this.launcherName = launcherName;
        }

        public String getLauncherVersion() {
            return launcherVersion;
        }

        public void setLauncherVersion(String launcherVersion) {
            this.launcherVersion = launcherVersion;
        }
    }

    public static class GameArgumentsParameters {
        private int width = -1;
        private int height = -1;
        private String userName = "Player";
        private String uuid = "00000000-0000-0000-0000-000000000000";
        private boolean fullscreen = false;

        public boolean hasCustomResolution(){
            return width != -1 && height != -1;
        }

        public int getResolutionWidth(){
            return width;
        }

        public int getResolutionHeight(){
            return height;
        }

        public void setResolution(int width, int height){
            this.width = width;
            this.height = height;
        }

        public void setUserName(String userName){
            this.userName = userName;
        }

        public String getUserName(){
            return userName;
        }

        public String getUUID() {
            return uuid;
        }

        public void setUUID(String uuid) {
            this.uuid = uuid;
        }

        public boolean isFullscreen() {
            return fullscreen;
        }

        public void setFullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
        }
    }

}
