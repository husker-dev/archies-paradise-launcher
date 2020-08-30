package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.husker.launcher.NetManager;
import com.husker.launcher.ui.impl.glass.Message;
import com.husker.launcher.ui.impl.glass.SimpleLoadingScreen;

public class InfoApplying extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Редактирование");
        setText("Применение...");
    }

    public void onShow() {
        new Thread(() -> {
            String currentPassword = getParameterValue("currentPassword");
            String login = getParameterValue("login");
            String email = getParameterValue("email");

            int result = getLauncher().NetManager.PlayerInfo.setData(currentPassword, NetManager.LOGIN, login, NetManager.EMAIL, email);

            if(result == getLauncher().NetManager.PlayerInfo.DATASET_OK)
                getLauncherUI().setScreen("main");
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_WRONG_PASSWORD)
                Message.showMessage(getLauncherUI(), "Проблемка", "Неправильный пароль!", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_BAD_NAME)
                Message.showMessage(getLauncherUI(), "Проблемка", "Имя имеет неправильный формат!", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_NAME_TAKEN)
                Message.showMessage(getLauncherUI(), "Проблемка", "Данное имя уже занято", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_BAD_EMAIL)
                Message.showMessage(getLauncherUI(), "Проблемка", "Почта имеет неправильный формат!", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_ERROR)
                Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_SERVER_ERROR)
                Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка на сервере", "info_edit", getParameters());

        }).start();
    }
}
