package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.api.API;

public class SkinCategoriesLoading extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Загрузка");
        setText("Получение списка скинов...");
    }

    public void process() {
        try {
            String[] folders = API.Skins.getCategories();
            getLauncherUI().setScreen(SkinCategories.class, new Parameters("folders", String.join(",", folders), "notReset", getParameterValue("notReset", "0")));
        }catch (Exception ex){
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить список скинов :(", MainScreen.class);
        }
    }
}
