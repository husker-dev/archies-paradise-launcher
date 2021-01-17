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
    private WinDef.HWND hwnd;

    private final MinecraftClientInfo clientInfo;
    private BufferedImage icon = null;
    private String title;

    private File scriptFile;

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

            // Setting title
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

            if(SystemUtils.isWindows()) {
                proc = Runtime.getRuntime().exec(startString, null, clientInfo.getClientFolder());
            }else{
                scriptFile = new File(clientInfo.getClientFolder(), "launch_script.sh");
                if(scriptFile.createNewFile())
                    Runtime.getRuntime().exec("chmod +x " + scriptFile.getAbsolutePath());
                MIO.writeText(startString, scriptFile);
                proc = Runtime.getRuntime().exec(scriptFile.getAbsolutePath(), null, clientInfo.getClientFolder());
            }

            AntiCheat.bindProcess(clientInfo.getClientFolder(), proc);

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
            if(SystemUtils.isWindows()) {
                MyUser32 user32 = MyUser32.INSTANCE;

                /*
                    If Windows, catch windows title using WinApi, and change it
                 */
                new Thread(() -> {
                    try {
                        int minecraftProcId = (int)SystemUtils.getProcessID(proc);

                        while (proc.isAlive()) {
                            user32.EnumWindows((User32.WNDENUMPROC) (hwnd, data) -> {
                                IntByReference procId = new IntByReference();
                                user32.GetWindowThreadProcessId(hwnd, procId);

                                if(minecraftProcId == procId.getValue()){
                                    if(MinecraftStarter.this.hwnd == null)
                                        onWindowShow(hwnd);
                                    onWindowTimer(hwnd);
                                    MinecraftStarter.this.hwnd = hwnd;
                                    return false;
                                }
                                return true;
                            }, null);
                            SystemUtils.sleep(2000);
                        }
                    }catch (Exception ignored){}
                }).start();
            }else {
                /*
                    If unix, remove .sh file after close
                 */
                new Thread(() -> {
                    try {
                        proc.waitFor();
                        clearAdditionMods();
                        if (scriptFile != null)
                            MIO.delete(scriptFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onWindowShow(WinDef.HWND hwnd){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int screenWidth = gd.getDisplayMode().getWidth();
        int screenHeight = gd.getDisplayMode().getHeight();
        int windowWidth = screenWidth / 2;
        int windowHeight = screenHeight / 2;

        if(LauncherSettings.isWindowed())
            MyUser32.INSTANCE.SetWindowPos(hwnd, hwnd, (screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2, windowWidth, windowHeight, SWP_NOZORDER| SWP_SHOWWINDOW);
        MyUser32.INSTANCE.ShowWindow(hwnd, 1);
    }

    private void onWindowTimer(WinDef.HWND hwnd){
        MyUser32.INSTANCE.SetWindowText(hwnd, title);
        MyUser32.INSTANCE.UpdateWindow(hwnd);
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

    private interface MyUser32 extends com.sun.jna.platform.win32.User32 {
        MyUser32 INSTANCE = Native.load("user32", MyUser32.class, W32APIOptions.UNICODE_OPTIONS);

        long SetWindowLongPtr(HWND hWnd, int nIndex, long ln);
        boolean ShowWindow(HWND hWnd, int  nCmdShow);
        HWND FindWindow(String className, String windowName);
        BOOL SetWindowTextA(
                HWND   hWnd,
                String lpString
        );
        boolean SetWindowText(HWND hwnd, String newText);

        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }

        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
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
