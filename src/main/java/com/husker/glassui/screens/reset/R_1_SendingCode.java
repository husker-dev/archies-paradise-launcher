package com.husker.glassui.screens.reset;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.launcher.api.API;
import com.husker.launcher.api.ApiMethod;
import com.husker.launcher.ui.LauncherUI;
import com.husker.launcher.ui.Screen;

public class R_1_SendingCode extends SimpleLoadingScreen {

    public static void show(LauncherUI ui, long id, String name, Class<? extends Screen> finalScreen){
        ui.setScreen(R_1_SendingCode.class, new Parameters(){{
            put("id", id);
            put("name", name);
            put("finalScreen", finalScreen.getSimpleName());
            put("prevScreen", ui.getScreenName());
        }});
    }

    public void onContentInit() {
        setTitle("Загрузка");
        setText("Отправление кода подтверждения...");
    }

    public void process() {
        try {
            API.getJSON(ApiMethod.create("profiles.sendPasswordChangeCode").set("id", getParameterValue("id")));
            getLauncherUI().setScreen(R_2_EmailCode.class, getParameters());
        } catch (API.InternalAPIException e) {
            Message.showMessage(getLauncherUI(), "Проблемка", "Не удалось отправить код подтверждения", getParameterValue("prevClass"));
        }
    }
}
