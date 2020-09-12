package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;

public class SkinFoldersLoading extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Загрузка");
        setText("Получение списка скинов...");
    }

    public void process() {
        try {
            String[] folders = getLauncher().NetManager.Skins.getFolders();
            getLauncherUI().setScreen("skin_folders", new Parameters("folders", String.join(",", folders), "notReset", getParameterValue("notReset", "0")));
        }catch (Exception ex){
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить список скинов :(", "main");
        }
    }
}
