package com.husker.launcher.ui.impl.glass.screens.main.profile;

import com.husker.launcher.managers.NetManager;
import com.husker.launcher.ui.impl.glass.screens.Message;
import com.husker.launcher.ui.impl.glass.screens.SimpleLoadingScreen;

import java.util.ArrayList;

public class InfoApplying extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Редактирование");
        setText("Применение...");
    }

    public void onShow() {
        new Thread(() -> {
            String currentPassword = getParameterValue("currentPassword");

            ArrayList<String> changedParameters = new ArrayList<>();
            if(getParameters().containsKey(NetManager.LOGIN)) {
                changedParameters.add(NetManager.LOGIN);
                changedParameters.add(getParameterValue(NetManager.LOGIN));
            }
            if(getParameters().containsKey(NetManager.EMAIL)) {
                changedParameters.add(NetManager.EMAIL);
                changedParameters.add(getParameterValue(NetManager.EMAIL));
                changedParameters.add(NetManager.EMAIL_CODE);
                changedParameters.add(getParameterValue(NetManager.EMAIL_CODE));
            }

            int result = getLauncher().NetManager.PlayerInfo.setData(currentPassword, changedParameters.toArray(new String[0]));

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
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_BAD_EMAIL_CODE)
                Message.showMessage(getLauncherUI(), "Проблемка", "Неправильный код подтверждения!", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_ERROR)
                Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка", "info_edit", getParameters());
            if(result == getLauncher().NetManager.PlayerInfo.DATASET_SERVER_ERROR)
                Message.showMessage(getLauncherUI(), "Проблемка", "Произошла ошибка на сервере", "info_edit", getParameters());

        }).start();
    }
}
