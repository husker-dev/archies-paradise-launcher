package com.husker.launcher.plugin.impl;

import com.husker.launcher.plugin.AAuthPlugin;
import com.husker.mio.FSUtils;
import com.husker.mio.MIO;
import com.husker.mio.processes.CopyingProcess;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Backup {

    public static long timerMillis = 15 * 1000;
    public static FileConfiguration config;
    public static File file;

    public static void init(){
        file = new File(AAuthPlugin.instance.getDataFolder(), "backup.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        config.addDefault("counted_time", 0);
        config.options().copyDefaults(true);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        timerMillis = Math.min(timerMillis, AAuthConfig.getBackupTime());

        new Timer().schedule(new TimerTask() {
            public void run() {
                long currentTime = config.getLong("counted_time");
                currentTime += timerMillis;

                if(currentTime > AAuthConfig.getBackupTime())
                    doBackup(AAuthPlugin.instance.getServer().getConsoleSender());
                else
                    saveTimerTime(currentTime);
            }
        }, 0, timerMillis);
    }

    public static void saveTimerTime(long time){
        config.set("counted_time", time);
        try {
            config.save(file);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void doBackup(CommandSender sender){
        new Thread(() -> {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Сохранение копии мира...");
            saveTimerTime(0);

            try {
                File serverFolder = new File(AAuthPlugin.instance.getServer().getWorldContainer().getAbsolutePath());
                if(serverFolder.getName().equals("world") || serverFolder.getName().equals("world" + File.separator))
                    serverFolder = serverFolder.getParentFile();

                File world = new File(serverFolder, "world");
                File backups = new File(serverFolder, "backups");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
                Date currentDate = new Date();
                File backupFolder = new File(backups, dateFormat.format(currentDate).replaceAll("\\s", "_") + ".zip");

                Files.createDirectories(Paths.get(backups.getAbsolutePath()));
                AAuthPlugin.instance.getLogger().info(backupFolder.getAbsolutePath());
                MIO.zip(new File[]{world}, backupFolder);

                List<File> files = FSUtils.getChildren(backups);
                files.sort((f1, f2) -> {
                    try {
                        long t1 = dateFormat.parse(f1.getName()).getTime();
                        long t2 = dateFormat.parse(f2.getName()).getTime();

                        return Long.compare(t1, t2);
                    }catch (Exception ignored){ }
                    return Integer.compare(0, 0);
                });
                long liveTime = Math.max(1, AAuthConfig.getBackupLiveTime());
                for(File file : files){
                    try {
                        long time = dateFormat.parse(file.getName()).getTime();
                        if(currentDate.getTime() - time > liveTime)
                            MIO.delete(file);
                    }catch (Exception ignored){}
                }

                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Копия мира сохранена!");
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "Ошибка при копировании мира!");
            }

        }).start();
    }
}
