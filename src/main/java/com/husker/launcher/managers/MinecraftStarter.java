package com.husker.launcher.managers;

import com.husker.launcher.Resources;
import com.husker.launcher.ui.utils.ImageUtils;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.minecraft.MinecraftClientInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MinecraftStarter {

    private static final Logger log = LogManager.getLogger(MinecraftStarter.class);
    private static final String skinModName = "CustomSkinLoader_Forge.jar";
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
            log.info("Executing: " + startString);

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

            proc = Runtime.getRuntime().exec(startString);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            stdInput.readLine();

            new Thread(() -> {
                try {
                    String s;
                    while ((s = stdInput.readLine()) != null)
                        log.info(s);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    String s;
                    while ((s = stdError.readLine()) != null)
                        log.error(s);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearSkinMod(){
        IOUtils.delete(clientInfo.getClientFolder() + "\\mods\\" + skinModName);
        IOUtils.delete(clientInfo.getClientFolder() + "\\libraries\\customskinloader");
        IOUtils.delete(clientInfo.getClientFolder() + "\\CustomSkinLoader");
    }

    public void applySkinMod(){
        try {
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
            Files.copy(Resources.get("mods\\CustomSkinLoader.json"), Paths.get(clientInfo.getClientFolder() + "\\CustomSkinLoader\\CustomSkinLoader.json"));
            Files.copy(Resources.get("mods\\CustomSkinLoader_Forge.jar"), Paths.get(toFolder));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String getLaunchString(){
        String line = "\"" + getJavaPath() + "\" " + clientInfo.getJVMArguments() + " " + clientInfo.getGameArguments();
        if(customTweakClass != null){
            if(line.contains("--tweakClass")) {
                String oldTweakClass = line.split("--tweakClass")[1].split(" ")[1];
                log.info("OLD TWEAK: " + oldTweakClass);
                line = line.replace(oldTweakClass, customTweakClass);
            }else
                line += " --tweakClass " + customTweakClass;
        }
        return line;
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
