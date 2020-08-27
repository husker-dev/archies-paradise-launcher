package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.husker.launcher.NetManager;
import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class InfoApplying extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Редактирование");
        setText("Применение...");
    }

    public void onShow() {
        new Thread(() -> {
            int result = getLauncher().NetManager.PlayerInfo.setData(getParameters()[0], NetManager.LOGIN, getParameters()[1], NetManager.EMAIL, getParameters()[2]);

            if(result == getLauncher().NetManager.PlayerInfo.DATASET_OK)
                getLauncherUI().setScreen("main");
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_WRONG_PASSWORD)
                getLauncherUI().setScreen("message", "info_edit," + getParameters()[1] + "," + getParameters()[2], "Проблемка", "Неправильный пароль!");
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_BAD_NAME)
                getLauncherUI().setScreen("message", "info_edit," + getParameters()[1] + "," + getParameters()[2], "Проблемка", "Имя имеет неправильный формат!");
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_NAME_TAKEN)
                getLauncherUI().setScreen("message", "info_edit," + getParameters()[1] + "," + getParameters()[2], "Проблемка", "Данное имя уже занято");
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_BAD_EMAIL)
                getLauncherUI().setScreen("message", "info_edit," + getParameters()[1] + "," + getParameters()[2], "Проблемка", "Почта имеет неправильный формат!");

            if(result == getLauncher().NetManager.PlayerInfo.DATASET_ERROR)
                getLauncherUI().setScreen("message", "info_edit," + getParameters()[1] + "," + getParameters()[2], "Проблемка", "Произошла ошибка");
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_SERVER_ERROR)
                getLauncherUI().setScreen("message", "info_edit," + getParameters()[1] + "," + getParameters()[2], "Проблемка", "Произошла ошибка на сервере");

        }).start();
    }
}
