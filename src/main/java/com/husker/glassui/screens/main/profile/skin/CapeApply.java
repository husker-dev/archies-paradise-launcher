package com.husker.glassui.screens.main.profile.skin;

import com.husker.glassui.screens.Message;
import com.husker.glassui.screens.SimpleLoadingScreen;
import com.husker.glassui.screens.main.MainScreen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CapeApply extends SimpleLoadingScreen {

    public void onContentInit() {
        setTitle("Плащ");
        setText("Применение...");
    }

    public void process() {
        try {
            if(getParameters().containsKey("reset") && getParameters().get("reset").equals("true"))
                getLauncher().User.setCape(new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB));

            if (getParameters().containsKey("path")) {
                if(!getParameterValue("path").endsWith(".png"))
                    throw new RuntimeException();

                BufferedImage image = ImageIO.read(new File(getParameterValue("path")));

                if(image.getWidth() > 64 || image.getHeight() > 64)
                    throw new RuntimeException();

                getLauncher().User.setCape(image);
            }
            if(getParameters().containsKey("name"))
                getLauncher().User.setCape(getParameterValue("name"));

            getLauncherUI().setScreen(MainScreen.class);
        }catch (Exception ex){
            ex.printStackTrace();
            Message.showMessage(getLauncherUI(), "Ошибка", "Не удалось загрузить плащ", MainScreen.class);
        }
    }
}
