package com.husker.glassui.screens.main.control;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.Screen;

import java.io.*;
import java.util.ArrayList;
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
        setText("Архивация...");
        try {
            File file = new File(getParameterValue("file"));
            String id = getParameterValue("id");
            String title = getParameterValue("title");

            String zipPath = "./" + file.getName() + ".zip";

            zip(file.getAbsolutePath(), zipPath);
            System.out.println(new File(zipPath).length());

            setText("Соединение...");
            API.Client.update(new File(zipPath), id, title, stage -> {
                if(stage == 0)
                    setText("Отправка");
                if(stage == 1)
                    setText("Обработка...");
            });

            setText("Удаление временных файлов...");
            com.husker.launcher.utils.IOUtils.delete(zipPath, progress -> {
                setText("Удаление временных файлов... (" + progress + "%)");
            });

            getLauncherUI().setScreen(MainScreen.class);
        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось отправить клиент", MainScreen.class);
        }
    }

    private static void zip(String file, String zip) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(zip));
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipFile(new File(file), new File(file).getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/"))
                zipOut.putNextEntry(new ZipEntry(fileName));
            else
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : children)
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0)
            zipOut.write(bytes, 0, length);
        fis.close();
    }





}

