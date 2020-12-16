package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.api.API;

public class SkinListLoading extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Загрузка");
        setText("Получение списка скинов...");
    }

    public void process() {
        try {
            String[] skins = API.Skins.getCategorySkins(getParameterValue("folder"));
            getLauncherUI().setScreen(SkinList.class, new Parameters("folder", getParameterValue("folder"), "skins", String.join(",", skins), "notReset", getParameterValue("notReset", "0")));
        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить список скинов :(", MainScreen.class);
        }
    }
}
