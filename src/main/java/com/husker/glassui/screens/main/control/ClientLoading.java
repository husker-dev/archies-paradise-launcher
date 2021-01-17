package com.husker.glassui.screens.main.control;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.Screen;
import com.husker.mio.FSUtils;
import com.husker.mio.MIO;
import com.husker.mio.processes.DeletingProcess;
import com.husker.mio.processes.ZippingProcess;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ClientLoading extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Загрузка");
        setText("Загрузка...");
    }

    public static void load(Screen screen, File directory, String id, String title){
        screen.getLauncherUI().setScreen(ClientLoading.class, new Parameters(){{
            put("file", directory.getAbsoluteFile());
            put("id", id);
            put("title", title);
        }});
    }

    public void process() {
        setText("Обработка...");
        try {
            File file = new File(getParameterValue("file"));
            String id = getParameterValue("id");
            String title = getParameterValue("title");
            String zipPath = "./" + file.getName() + ".zip";

            ArrayList<String> needs = new ArrayList<>(Arrays.asList("versions", "mods", "libraries"));
            for(File content : FSUtils.getChildren(file))
                needs.remove(content.getName());

            if(needs.size() != 0){
                JOptionPane.showMessageDialog(getLauncher(), "Данный клиент не подходит для установки, недостаёт обязательных папок: " + needs.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                getLauncherUI().setScreen(MainScreen.class);
                return;
            }

            new ZippingProcess(FSUtils.getChildren(file).toArray(new File[0]), new File(zipPath)).addProgressListener(e -> {
                setText("Добавление в архив... " + (int) e.getPercent() + "%");
            }).startSync();

            setText("Соединение...");
            API.Client.update(getLauncher().User.getToken(), new File(zipPath), id, title, new API.Client.UpdatingActionListener() {
                public void sending(int progress) {
                    setText("Отправка... " + progress + "%");
                }
                public void process() {
                    setText("Удаление временных файлов...");
                    try {
                        new DeletingProcess(zipPath).addProgressListener(e -> setText("Удаление временных файлов... " + (int) e.getPercent())).startSync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getLauncherUI().setScreen(MainScreen.class);
                }
            });


        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось отправить клиент", MainScreen.class);
        }
    }

}

