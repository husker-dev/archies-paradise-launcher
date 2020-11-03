package com.husker.glassui.screens.main.profile.edit;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.managers.NetManager;

import java.util.ArrayList;
import java.util.HashMap;

import static com.husker.launcher.managers.NetManager.*;

public class InfoApplying extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Редактирование");
        setText("Применение...");
    }

    public void process() {
        String currentPassword = getParameterValue(NetManager.PASSWORD);
        String emailCode = null;

        ArrayList<String> changedParameters = new ArrayList<>();
        if(getParameters().containsKey(NetManager.LOGIN)) {
            changedParameters.add(NetManager.LOGIN);
            changedParameters.add(getParameterValue(NetManager.LOGIN));
        }
        if(getParameters().containsKey(NetManager.EMAIL)) {
            changedParameters.add(NetManager.EMAIL);
            changedParameters.add(getParameterValue(NetManager.EMAIL));

            emailCode = getParameterValue(NetManager.EMAIL_CODE);
        }

        int result = getLauncher().NetManager.PlayerInfo.setData(currentPassword, emailCode, changedParameters.toArray(new String[0]));

        if(result == 0) {
            getLauncherUI().setScreen(MainScreen.class);
            return;
        }

        HashMap<Integer, String> messages = new HashMap<>();

        messages.put(DATASET_INCORRECT_PASSWORD, "Неправильный пароль!");
        messages.put(DATASET_PASSWORD_FORMAT, messages.get(DATASET_INCORRECT_PASSWORD));
        messages.put(DATASET_NAME_FORMAT, "Имя имеет неправильный формат!");
        messages.put(DATASET_NAME_TAKEN, "Данное имя уже занято");
        messages.put(DATASET_EMAIL_FORMAT, "Почта имеет неправильный формат!");
        messages.put(DATASET_INCORRECT_EMAIL_CODE, "Неправильный код подтверждения!");
        messages.put(-1, "Произошла ошибка");
        messages.put(-2, "Произошла ошибка на сервере");

        Message.showMessage(getLauncherUI(), "Проблемка", messages.get(result), InfoEdit.class, getParameters());
    }
}
