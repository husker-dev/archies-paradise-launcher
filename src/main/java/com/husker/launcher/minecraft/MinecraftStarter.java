package com.husker.launcher.minecraft;

import com.husker.launcher.Resources;
import com.husker.launcher.minecraft.info.MinecraftClientInfo;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.ui.utils.ImageUtils;
import com.husker.launcher.utils.SystemUtils;
import com.husker.mio.MIO;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static com.sun.jna.platform.win32.WinUser.SWP_NOZORDER;
import static com.sun.jna.platform.win32.WinUser.SWP_SHOWWINDOW;

public class MinecraftStarter {

    private static final Logger log = LogManager.getLogger(MinecraftStarter.class);

    public static final String controllerModName = "controllable-0.8.0-mc1.12.2.jar";
    public static final String skinModName = "CustomSkinLoader_Forge.jar";
    private static final String skinLibraryPath = "customskinloader\\CustomSkinLoader\\" + skinModName;

    private Process proc;

    private final MinecraftClientInfo clientInfo;
    private BufferedImage icon = null;

    private String customTweakClass = null;

    public MinecraftStarter(String folder) throws Exception {
        clientInfo = MinecraftClientInfo.getDefaultClientInfo(new File(folder));
    }

    public void setIcon(BufferedImage icon){
        this.icon = icon;
    }

    public void setNickname(String nickname){
        clientInfo.getGameParameters().setUserName(nickname);
    }

    public void setUUID(String uuid){
        clientInfo.getGameParameters().setUUID(uuid);
    }

    public void setFullscreen(boolean fullscreen){
        clientInfo.getGameParameters().setFullscreen(fullscreen);
    }

    public void setRam(long ram){
        clientInfo.getJvmParameters().setRam(ram);
    }

    public void launch(){
        try {
            String startString = getLaunchString();
            //log.info("Executing: " + startString);

            // Setting title
            String title;
            try {
                String[] titles = MIO.readText(getClass().getResourceAsStream("/game_titles.txt")).split("\n");
                title = titles[new Random().nextInt(titles.length)];
            }catch (Exception ex){
                title = LauncherConfig.getTitle();
            }

            // Setting addition mods
            applyAdditionMods();

            // Setting icon
            if(icon != null) {
                String jsonPath = clientInfo.getClientFolder().getAbsolutePath() + "/assets/indexes/" + clientInfo.getAssetsId() + ".json";
                JSONObject assetsJson = new JSONObject(String.join("", Files.readAllLines(Paths.get(jsonPath))));
                JSONObject objects = assetsJson.getJSONObject("objects");
                if(objects.has("icons/icon_16x16.png")){
                    String hash = objects.getJSONObject("icons/icon_16x16.png").getString("hash");
                    String firstDigits = hash.substring(0, 2);
                    BufferedImage scaled = ImageUtils.getScaledInstance(icon, 16, 16, Image.SCALE_SMOOTH);
                    File file = new File(clientInfo.getClientFolder().getAbsolutePath() + "/assets/objects/" + firstDigits + "/" + hash);
                    ImageIO.write(scaled, "png", file);
                }
                if(objects.has("icons/icon_32x32.png")){
                    String hash = objects.getJSONObject("icons/icon_32x32.png").getString("hash");
                    String firstDigits = hash.substring(0, 2);
                    BufferedImage scaled = ImageUtils.getScaledInstance(icon, 32, 32, Image.SCALE_SMOOTH);
                    File file = new File(clientInfo.getClientFolder().getAbsolutePath() + "/assets/objects/" + firstDigits + "/" + hash);
                    ImageIO.write(scaled, "png", file);
                }
                if(objects.has("minecraft/icons/icon_16x16.png")){
                    String hash = objects.getJSONObject("minecraft/icons/icon_16x16.png").getString("hash");
                    String firstDigits = hash.substring(0, 2);
                    BufferedImage scaled = ImageUtils.getScaledInstance(icon, 16, 16, Image.SCALE_SMOOTH);
                    File file = new File(clientInfo.getClientFolder().getAbsolutePath() + "/assets/objects/" + firstDigits + "/" + hash);
                    ImageIO.write(scaled, "png", file);
                }
                if(objects.has("minecraft/icons/icon_32x32.png")){
                    String hash = objects.getJSONObject("minecraft/icons/icon_32x32.png").getString("hash");
                    String firstDigits = hash.substring(0, 2);
                    BufferedImage scaled = ImageUtils.getScaledInstance(icon, 32, 32, Image.SCALE_SMOOTH);
                    File file = new File(clientInfo.getClientFolder().getAbsolutePath() + "/assets/objects/" + firstDigits + "/" + hash);
                    ImageIO.write(scaled, "png", file);
                }
            }

            JSONObject settings = new JSONObject();
            settings.put("additionMods", new HashMap<String, String>(){{
                put(MinecraftStarter.skinModName, "0aeb2a97185effb9000a7fbe7f76247b");
                put(MinecraftStarter.controllerModName, "1d7419c12dd68cf5d02a7e60d35fc075");
            }});
            settings.put("folder", clientInfo.getClientFolder().getAbsolutePath());
            settings.put("launch", startString);
            settings.put("host_ip", LauncherConfig.getAuthIp());
            settings.put("host_port", LauncherConfig.getAuthPort());
            settings.put("title", title);

            String anticheatStartLine = "java -cp \"anticheat.jar;lib/*\" com.husker.launcher.anticheat.AntiCheat --settings=\"" + settings.toString().replace("\"", "[quo]") + "\"";
            anticheatStartLine = SystemUtils.fixPath(anticheatStartLine);
            log.info("Executing AntiCheat: " + anticheatStartLine);
            proc = Runtime.getRuntime().exec(anticheatStartLine);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            //procId = Integer.parseInt(stdInput.readLine().split("proc_id:")[1]);
            //log.info("Process ID: " + procId);

            new Thread(() -> {
                try {
                    String s;
                    while ((s = stdInput.readLine()) != null)
                        System.out.println(s);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    String s;
                    while ((s = stdError.readLine()) != null)
                        System.out.println(s);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    proc.waitFor();
                    clearAdditionMods();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAdditionMods(){
        try {
            MIO.delete(clientInfo.getClientFolder() + "\\mods\\" + controllerModName);
            MIO.delete(clientInfo.getClientFolder() + "\\mods\\" + skinModName);
            MIO.delete(clientInfo.getClientFolder() + "\\libraries\\customskinloader");
            MIO.delete(clientInfo.getClientFolder() + "\\CustomSkinLoader");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void applyAdditionMods(){


        try {
            if(LauncherSettings.getControllerSupport() && clientInfo.getType() == MinecraftClientInfo.ClientType.FORGE)
                Files.copy(Resources.get("mods\\" + controllerModName), Paths.get(clientInfo.getClientFolder() + "\\mods\\" + controllerModName));

            String toFolder = "";
            clientInfo.removeLibrary(skinLibraryPath);
            if(clientInfo.getType() == MinecraftClientInfo.ClientType.FORGE) {
                toFolder = clientInfo.getClientFolder() + "\\mods\\" + skinModName;
                customTweakClass = null;
            }
            if(clientInfo.getType() == MinecraftClientInfo.ClientType.OPTIFINE) {
                toFolder = clientInfo.getClientFolder() + "\\libraries\\" + skinLibraryPath;
                clientInfo.addLibrary(skinLibraryPath);
                customTweakClass = null;
            }
            if(clientInfo.getType() == MinecraftClientInfo.ClientType.VANILLA) {
                toFolder = clientInfo.getClientFolder() + "\\libraries\\" + skinLibraryPath;
                clientInfo.addLibrary(skinLibraryPath);
                customTweakClass = "customskinloader.tweaker.Tweaker";
            }
            Files.createDirectories(Paths.get(clientInfo.getClientFolder() + "\\CustomSkinLoader"));
            String configText = String.join("\n", Files.readAllLines(Paths.get("resources\\mods\\CustomSkinLoader.json")));

            configText = configText.replace("{server_ip}", LauncherConfig.getAuthIp()).replace("{server_port}", LauncherConfig.getAuthPort() + "");
            Files.write(Paths.get(clientInfo.getClientFolder() + "\\CustomSkinLoader\\CustomSkinLoader.json"), Arrays.asList(configText.split("\n")));
            //MIO.writeText(configText, clientInfo.getClientFolder() + "\\CustomSkinLoader\\CustomSkinLoader.json");

            Files.copy(Resources.get("mods\\" + skinModName), Paths.get(toFolder));


        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String getLaunchString(){
        String javaPath = getJavaPath().equals("java") ? "java" : "\"" + getJavaPath() + "\"";

        String line = javaPath + " " + clientInfo.getJVMArguments() + " " + clientInfo.getGameArguments();
        if(customTweakClass != null){
            if(line.contains("--tweakClass")) {
                String oldTweakClass = line.split("--tweakClass")[1].split(" ")[1];
                log.info("OLD TWEAK: " + oldTweakClass);
                line = line.replace(oldTweakClass, customTweakClass);
            }else
                line += " --tweakClass " + customTweakClass;
        }
        return SystemUtils.fixPath(line);
    }



    private String getJavaPath(){
        HashMap<Integer, String> paths = new HashMap<>();

        for(File drive : File.listRoots()){
            try {
                File javaDirectory = new File(drive, "Program Files\\Java");
                if (javaDirectory.exists()) {
                    for (File folder : Objects.requireNonNull(javaDirectory.listFiles())) {
                        File javaRelease = new File(folder, "release");
                        if(javaRelease.exists()){
                            String version = String.join("\n", Files.readAllLines(Paths.get(javaRelease.getAbsolutePath()))).split("JAVA_VERSION=\"")[1].split("\"")[0];

                            if(version.startsWith("1.")) {
                                version = version.substring(2, 3);
                            } else {
                                int dot = version.indexOf(".");
                                if(dot != -1)
                                    version = version.substring(0, dot);
                            }
                            paths.put(Integer.parseInt(version), new File(folder, "bin/java.exe").getAbsolutePath());
                        }
                    }
                }
            }catch (Exception ignored){}
        }

        log.info("Installed Java versions: ");
        paths.forEach((key, val) -> log.info("- " + key + " => " + val));
        for(int i = 8; i < 14; i++)
            if(paths.containsKey(i))
                return paths.get(i);
        return "java";
    }

    public void joinThread()  {
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
