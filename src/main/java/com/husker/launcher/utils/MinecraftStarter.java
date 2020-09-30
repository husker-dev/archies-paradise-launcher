package com.husker.launcher.utils;

import com.husker.launcher.managers.UpdateManager;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MinecraftStarter {

    private final String folder;
    private final String versionFolder;
    private final JSONObject object;
    private final String version;
    private final String jarPath;

    private Process proc;

    private final ArrayList<String> servers = new ArrayList<>();
    private String nickname = "Player";
    private String uuid = "00000000-0000-0000-0000-000000000000";

    public MinecraftStarter(String folder) throws IOException {
        this.folder = new File(folder).getAbsolutePath().replace("\\", "/");
        versionFolder = new File(folder + "/versions").listFiles()[0].getAbsolutePath();
        jarPath = new File(versionFolder).listFiles(file -> file.getName().endsWith(".jar"))[0].getAbsolutePath();
        object = new JSONObject(IOUtils.readFileText(new File(versionFolder).listFiles(file -> file.getName().endsWith(".json"))[0].getAbsolutePath()));
        version = object.getString("id");
    }

    public void addServer(String server){
        servers.add(server);
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setUUID(String uuid){
        this.uuid = uuid;
    }

    public void launch(){
        try {
            String startString = getStartString();
            ConsoleUtils.printDebug(getClass(), "Parameters: " + startString);

            proc = Runtime.getRuntime().exec(startString);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            new Thread(() -> {
                try {
                    String s;
                    while ((s = stdInput.readLine()) != null)
                        ConsoleUtils.printDebug(MinecraftStarter.class, s);
                }catch (Exception ex){}
            }).start();
            new Thread(() -> {
                try {
                    String s;
                    while ((s = stdError.readLine()) != null)
                        ConsoleUtils.printDebug(MinecraftStarter.class, s);
                }catch (Exception ex){}
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareServers(){
        if(servers.size() == 0)
            return;

        try {
            NamedTag namedTag = NBTUtil.read("C:\\Users\\redfa\\AppData\\Roaming\\.minecraft\\servers.dat");
            ConsoleUtils.printDebug(getClass(), namedTag.getTag());
            CompoundTag info = new CompoundTag(){{
                ListTag<CompoundTag> list = new ListTag<>(CompoundTag.class);
                put("servers", list);

                for(String server : servers){
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putString("name", server);
                    list.add(compoundTag);
                }
            }};
            ConsoleUtils.printDebug(getClass(), info);
            NBTUtil.write(info, folder + "/servers.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getStartString(){
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("java");

        arguments.add("-Djava.library.path=\"" + versionFolder + "\\natives\"");
        arguments.add("-Dminecraft.launcher.brand=minecraft-launcher");
        arguments.add("-Dminecraft.launcher.version=" + UpdateManager.VERSION);
        if(isWindows())
            arguments.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        arguments.add("-classpath \"" + getLibraries() + "\" " + object.getString("mainClass"));

        arguments.add("--username " + nickname);
        arguments.add("--version " + version);
        arguments.add("--gameDir \"" + folder + "\"");
        arguments.add("--assetsDir \"" + folder + "/assets\"");
        arguments.add("--userType legacy");
        arguments.add("--versionType release");
        arguments.add("--accessToken null");
        arguments.add("--uuid " + uuid);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        arguments.add("--width " + gd.getDisplayMode().getWidth() / 2);
        arguments.add("--height " + gd.getDisplayMode().getHeight() / 2);

        return String.join(" ", arguments);
    }

    private String getLibraries(){
        StringBuilder out = new StringBuilder();

        JSONArray libraries = object.getJSONArray("libraries");
        for(int i = 0; i < libraries.length(); i++) {
            try {
                JSONObject mainObject = libraries.getJSONObject(i).getJSONObject("downloads");
                if(mainObject.has("artifact"))
                    out.append(folder).append("/libraries/").append(mainObject.getJSONObject("artifact").getString("path")).append(";");
                if(mainObject.has("classifiers")) {
                    if (isWindows() && mainObject.has("natives-windows"))
                        out.append(folder).append("/libraries/").append(mainObject.getJSONObject("classifiers").getJSONObject("natives-windows").getString("path")).append(";");
                    if (isUnix() && mainObject.has("atives-linux"))
                        out.append(folder).append("/libraries/").append(mainObject.getJSONObject("classifiers").getJSONObject("natives-linux").getString("path")).append(";");
                    if (isMac() && mainObject.has("natives-osx"))
                        out.append(folder).append("/libraries/").append(mainObject.getJSONObject("classifiers").getJSONObject("natives-osx").getString("path")).append(";");
                }
            }catch (Exception ex){
                ex.printStackTrace();
                ConsoleUtils.printDebug(getClass(), libraries.getJSONObject(i).getJSONObject("downloads"));
            }
        }
        out.append(jarPath);

        return out.toString();
    }

    private static boolean isWindows() {
        return (System.getProperty("os.name").contains("win"));
    }

    private static boolean isMac() {
        return (System.getProperty("os.name").contains("mac"));
    }

    private static boolean isUnix() {
        return (System.getProperty("os.name").contains("nix") || System.getProperty("os.name").contains("nux") || System.getProperty("os.name").contains("aix"));
    }

    public void joinThread()  {
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
