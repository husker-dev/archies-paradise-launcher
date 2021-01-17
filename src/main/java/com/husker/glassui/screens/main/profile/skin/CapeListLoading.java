package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;
import com.husker.launcher.api.API;
import com.husker.launcher.ui.Screen;

public class CapeListLoading extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Загрузка");
        setText("Получение списка плащей...");
    }

    public void process() {
        try {
            String[] capes = API.Skins.getCapes();
            getLauncherUI().setScreen(CapeList.class, new Screen.Parameters("capes", String.join(",", capes), "notReset", getParameterValue("notReset", "0")));
        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить список плащей :(", MainScreen.class);
        }
    }
}
