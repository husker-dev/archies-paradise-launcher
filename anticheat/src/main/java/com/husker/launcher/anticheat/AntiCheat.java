package com.husker.launcher.anticheat;

import com.husker.launcher.anticheat.info.MinecraftClientInfo;
import com.husker.launcher.anticheat.modules.DllChecker;
import com.husker.launcher.anticheat.modules.OnServerConnectionChecker;
import com.husker.launcher.anticheat.modules.WindowDecorator;
import com.husker.launcher.anticheat.system.DllInstance;
import com.husker.launcher.anticheat.system.SystemUtils;
import com.husker.mio.FSUtils;
import com.husker.mio.MIO;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.ptr.IntByReference;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AntiCheat {

    private final HashMap<String, String> additionModsHash = new HashMap<>();
    private final File clientFolder;
    private final String launchString;
    private final String ip;
    private final String clientId;
    private final String title;
    private final int port;

    private Process process;
    private File scriptFile;

    private final ArrayList<Consumer<String>> consoleListeners = new ArrayList<>();

    private final String javaPath;

    public static void main(String[] args) throws Exception {
        String argument = String.join("", Arrays.asList(args)).replace("[quo]", "\"");

        if(argument.startsWith("--settings=")) {
            new AntiCheat(new JSONObject(argument.split("--settings=")[1]));
        }else
            throw new NullPointerException("Settings argument is null");
    }

    public AntiCheat(JSONObject settings){
        settings.getJSONObject("additionMods").toMap().forEach((name, val) -> additionModsHash.put(name, val.toString()));
        clientFolder = new File(settings.getString("folder"));
        launchString = settings.getString("launch");
        ip = settings.getString("host_ip");
        port = settings.getInt("host_port");
        title = settings.getString("title");
        clientId = clientFolder.getName().replace("\\", "").replace("/", "");

        if(launchString.startsWith("java"))
            javaPath = System.getProperty("java.home");
        else
            javaPath = new File(launchString.split("\"")[1]).getParentFile().getParentFile().getAbsolutePath();

        try {
            start();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void start() throws Exception {
        /*
            Start minecraft process
         */
        if(SystemUtils.isWindows()) {
            process = Runtime.getRuntime().exec(launchString, null, clientFolder);
        }else{
            scriptFile = new File(clientFolder, "launch_script.sh");
            if(scriptFile.createNewFile())
                Runtime.getRuntime().exec("chmod +x " + scriptFile.getAbsolutePath());
            MIO.writeText(launchString, scriptFile);
            process = Runtime.getRuntime().exec(scriptFile.getAbsolutePath(), null, clientFolder);
        }

        /*
            Read I/O
         */
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        new Thread(() -> {
            try {
                String s;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                    String finalS = s;
                    consoleListeners.forEach(t -> t.accept(finalS));
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                String s;
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                    String finalS = s;
                    consoleListeners.forEach(t -> t.accept(finalS));
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }).start();

        /*
            Bind current process to game
         */
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeGame));

        new OnServerConnectionChecker(this);
        if(SystemUtils.isWindows()) {
            new DllChecker(this);
            new WindowDecorator(this);
        }


        /*
            If unix, remove .sh file after close
        */
        new Thread(() -> {
            try {
                process.waitFor();
                if (scriptFile != null)
                    MIO.delete(scriptFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String getWindowTitle(){
        return title;
    }

    public String getJavaPath(){
        return javaPath;
    }

    public File getClientFolder(){
        return clientFolder;
    }

    public Process getProcess(){
        return process;
    }

    public void addConsoleListener(Consumer<String> listener){
        consoleListeners.add(listener);
    }

    public void closeGame(){
        while(process != null && process.isAlive()) {
            process.destroy();
            try {
                Runtime.getRuntime().exec("taskkill /PID " + SystemUtils.getProcessID(process));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Подтвердите начало майнинга биткоинов", "Подтврждение", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean[] checkMD5() {
        try{
            String mods = getModsMD5();
            String versions = getVersionsMD5();

            JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader(new URL(ip + ":" + port + "/api/method/clients.checksum?id=" + clientId + "&mods=" + mods + "&versions=" + versions).openStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")));
            return new boolean[]{json.getBoolean("equal_mods"), json.getBoolean("equal_versions")};
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new boolean[]{false, false};
    }

    private String getModsMD5(){
        try{
            for(Map.Entry<String, String> entry : additionModsHash.entrySet()) {
                File file = new File(clientFolder, "mods\\" + entry.getKey());
                if (file.exists()) {
                    if (!DigestUtils.md5Hex(new FileInputStream(file)).equals(entry.getValue()))
                        return "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
                }
            }

            ArrayList<File> mods = new ArrayList<>(FSUtils.getChildren(new File(clientFolder, "mods")));
            mods = mods.stream()
                    .filter(file -> file.getName().endsWith(".jar") && !additionModsHash.containsKey(file.getName()))
                    .sorted(Comparator.comparing(File::getName))
                    .collect(Collectors.toCollection(ArrayList::new));

            Vector<FileInputStream> streams = new Vector<>();
            for(File file : mods){
                try {
                    streams.add(new FileInputStream(file));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return DigestUtils.md5Hex(new SequenceInputStream(streams.elements()));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getVersionsMD5(){
        try{
            File clientFile = MinecraftClientInfo.getJar(clientFolder);
            return DigestUtils.md5Hex(new FileInputStream(clientFile));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private interface MyUser32 extends com.sun.jna.platform.win32.User32 {

    }
}
