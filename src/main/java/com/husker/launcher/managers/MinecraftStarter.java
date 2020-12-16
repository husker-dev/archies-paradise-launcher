package com.husker.launcher.managers;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MinecraftStarter {

    private static final Logger log = LogManager.getLogger(MinecraftStarter.class);

    private Process proc;

    private final MinecraftClientInfo clientInfo;

    private BufferedImage icon = null;

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

    private String getLaunchString(){
        return "java " + clientInfo.getJVMArguments() + " " + clientInfo.getGameArguments();
    }

    public void joinThread()  {
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
