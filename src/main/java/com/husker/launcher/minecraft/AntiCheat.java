package com.husker.launcher.minecraft;

import com.husker.launcher.api.API;
import com.husker.launcher.minecraft.info.MinecraftClientInfo;
import com.husker.launcher.utils.SystemUtils;
import com.husker.mio.FSUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AntiCheat {

    private static final Logger log = LogManager.getLogger(AntiCheat.class);

    public static void bindProcess(File clientFolder, Process process){

        /*
            Check for client md5 when joining to the server
         */
        new Thread(() -> {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;
                while ((s = input.readLine()) != null){
                    if(s.contains("[main/INFO] [minecraft/GuiConnecting]: Connecting to ")){
                        API.Minecraft.ServerInfo serverInfo = API.Minecraft.getServerInfo();
                        if(s.contains("[main/INFO] [minecraft/GuiConnecting]: Connecting to " + serverInfo.getIP() + ", " + serverInfo.getPort())){
                            log.info("Checking client MD5...");
                            boolean[] result = checkMD5(clientFolder);
                            if(!result[0] || !result[1]){
                                closeGame(process);
                                return;
                            }
                        }
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }).start();

        /*
            Check dlls in process (Only Windows)
         */
        if(SystemUtils.isWindows()) {
            new Thread(() -> {
                while (process.isAlive()) {
                    try {
                        String[] dllsLinesOut = SystemUtils.executePowerShell("Get-Process -Id " + SystemUtils.getProcessID(process) + "| select -ExpandProperty modules|ft -Autosize").split("\n");

                        ArrayList<String> dllsLines = new ArrayList<>(Arrays.asList(dllsLinesOut));
                        dllsLines.subList(0, 4).clear();

                        ArrayList<String> dllNames = new ArrayList<>();
                        for (String line : dllsLines) {
                            try {
                                dllNames.add(line.split("\\.dll")[0].trim().split(" ")[1] + ".dll");
                            } catch (Exception ignored) {
                            }
                        }

                        MinecraftClientInfo info = MinecraftClientInfo.getDefaultClientInfo(clientFolder);
                        ArrayList<String> badWords = new ArrayList<>(Arrays.asList("injector", "inj", info.getVersion(), "Gish", "1.12.2", "cheat", "hack", "xray"));

                        for (String dll : dllNames) {
                            for (String word : badWords) {
                                word = word.toLowerCase();
                                if (dll.toLowerCase().contains(word))
                                    closeGame(process);
                            }
                        }
                    } catch (Exception ignored) { }

                    SystemUtils.sleep(7 * 1000);
                }
            }).start();
        }
    }

    private static void closeGame(Process process){
        while(process.isAlive()) {
            process.destroy();
            try {
                Runtime.getRuntime().exec("taskkill /PID " + SystemUtils.getProcessID(process));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Агааа!! Попался читер!!", "Вонючий читер", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean[] checkMD5(File clientFolder) throws API.UnknownClientException, API.InternalAPIException, API.ClientIsUpdatingException {
        return API.Client.checksum(clientFolder.getName().replace("/", File.separator).replace("\\", File.separator), getModsMD5(clientFolder), getClientMD5(clientFolder));
    }

    private static String getModsMD5(File clientFolder){
        try{
            HashMap<String, String> additionModsHash = new HashMap<String, String>(){{
                put(MinecraftStarter.skinModName, "0aeb2a97185effb9000a7fbe7f76247b");
                put(MinecraftStarter.controllerModName, "1d7419c12dd68cf5d02a7e60d35fc075");
            }};

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

    private static String getClientMD5(File clientFolder){
        try{
            File clientFile = MinecraftClientInfo.getJar(clientFolder);
            return DigestUtils.md5Hex(new FileInputStream(clientFile));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
