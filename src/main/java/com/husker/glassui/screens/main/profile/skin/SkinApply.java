package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SkinApply extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Скин");
        setText("Применение...");
    }

    public void process() {
        try {
            if (getParameters().containsKey("path")) {
                if(!getParameterValue("path").endsWith(".png"))
                    throw new RuntimeException();

                BufferedImage image = ImageIO.read(new File(getParameterValue("path")));

                if(image.getWidth() > 64 || image.getHeight() > 64)
                    throw new RuntimeException();

                getLauncher().User.setSkin(image);
            }
            if(getParameters().containsKey("folder") && getParameters().containsKey("name"))
                getLauncher().User.setSkin(getParameterValue("folder"), getParameterValue("name"));

            getLauncherUI().setScreen(MainScreen.class);
        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить скин", MainScreen.class);
        }
    }
}
