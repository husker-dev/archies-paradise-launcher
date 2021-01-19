package com.husker.launcher.anticheat.system;

import com.husker.launcher.anticheat.info.MinecraftClientInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DllInstance {



    public static List<DllInstance> getProcessDlls(Process process){
        return getProcessDlls(SystemUtils.getProcessID(process));
    }

    public static List<DllInstance> getProcessDlls(long processId){
        try {
            String[] consoleOut = SystemUtils.executePowerShell("Get-Process -Id " + processId + "| select -ExpandProperty modules|ft -Autosize").split("\n");

            ArrayList<String> consoleOutList = new ArrayList<>(Arrays.asList(consoleOut));
            consoleOutList.subList(0, 4).clear();

            ArrayList<DllInstance> dlls = new ArrayList<>();
            for (String line : consoleOutList) {
                line = line.replaceAll("\\s+", " ").trim();

                String[] parts = line.split(" ");
                String id = parts[0];
                String name = parts[1];
                String path = line.replaceAll(id + " " + name + " ", "");

                dlls.add(new DllInstance(Long.parseLong(id), name, path));
            }
            dlls.sort(Comparator.comparingLong(t -> t.id));
            return dlls;

            /*
            if(dllNames.equals(old))
                dllNames.forEach(System.out::println);
            old = dllNames;

            MinecraftClientInfo info = MinecraftClientInfo.getDefaultClientInfo(clientFolder);
            ArrayList<String> badWords = new ArrayList<>(Arrays.asList("injector", "inj", info.getVersion(), "Gish", "1.12.2", "cheat", "hack", "xray"));

            for (String dll : dllNames) {
                for (String word : badWords) {
                    word = word.toLowerCase();
                    if (dll.toLowerCase().contains(word))
                        closeGame();
                }
            }

             */
        } catch (Exception ignored) { }

        return null;
    }

    public final long id;
    public final String name, path;

    public DllInstance(long id, String name, String path){
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public String toString() {
        return "DllInstance{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
