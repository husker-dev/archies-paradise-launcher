package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;

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

                int result = getLauncher().NetManager.PlayerInfo.setSkin(image);
                if(result == -1)
                    throw new RuntimeException();
            }
            if(getParameters().containsKey("folder") && getParameters().containsKey("name")) {
                int result = getLauncher().NetManager.PlayerInfo.setSkin(getParameterValue("folder"), getParameterValue("name"));
                if(result == -1)
                    throw new RuntimeException();
            }

            getLauncher().NetManager.PlayerInfo.updateData();
            getLauncherUI().setScreen("main");
        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить скин", "main");
        }
    }
}