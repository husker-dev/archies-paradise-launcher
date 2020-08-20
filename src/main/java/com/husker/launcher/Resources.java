package com.husker.launcher;

import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Resources {

    public static BufferedImage blurDefaultTexture;

    static {
        blurDefaultTexture = Resources.getBufferedImage("paper.png");
    }

    public static InputStream get(String file){
        return get(file, true);
    }

    public static InputStream get(String file, boolean launcherFolder){
        String folder = launcherFolder ? "launcher/" : "";
        String path = "/" + folder + file;

        ConsoleUtils.printDebug(Resources.class, "Reading <jar>" + path);

        InputStream out = Resources.class.getResourceAsStream(path);

        ConsoleUtils.printResult(out != null ? "OK" : "ERROR");

        return out;
    }

    public static BufferedImage getBufferedImage(String file){
        try {
            return ImageUtils.toBufferedImage(ImageIO.read(get(file)));
        }catch (Exception ex){
        }
        return null;
    }

    public static Image getImage(String file){
        try {
            return ImageIO.read(get(file));
        } catch (IOException e) {
        }
        return null;
    }

    public static ImageIcon getIcon(String file){
        return new ImageIcon(getBufferedImage(file));
    }

    public static class Fonts {
        public static Font SFPro;
        public static Font ChronicaPro;
        public static Font ChronicaPro_Bold;
        public static Font ChronicaPro_ExtraBold;

        static {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

                ge.registerFont(SFPro = Font.createFont(Font.TRUETYPE_FONT, Resources.get("fonts/SFProDisplay-Regular.ttf")).deriveFont(16f));
                ge.registerFont(ChronicaPro = Font.createFont(Font.TRUETYPE_FONT, Resources.get("fonts/ChronicaPro-Black.ttf")).deriveFont(16f));
                ge.registerFont(ChronicaPro_Bold = Font.createFont(Font.TRUETYPE_FONT, Resources.get("fonts/ChronicaPro-Bold.ttf")).deriveFont(16f));
                ge.registerFont(ChronicaPro_ExtraBold = Font.createFont(Font.TRUETYPE_FONT, Resources.get("fonts/ChronicaPro-ExtraBold.ttf")).deriveFont(16f));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public final BufferedImage Logo;
    public final BufferedImage Icon;

    public final BufferedImage Icon_Info;
    public final BufferedImage Icon_Play;
    public final BufferedImage Icon_Profile;
    public final BufferedImage Icon_Settings;
    public final BufferedImage Icon_Book;
    public final BufferedImage Icon_Videos;
    public final BufferedImage Icon_Image;
    public final BufferedImage Icon_Frame;

    public final BufferedImage Logo_Youtube;
    public final BufferedImage Logo_VK;

    public final BufferedImage[] Background = new BufferedImage[7];

    public final BufferedImage Icon_Checkbox_On;
    public final BufferedImage Icon_Checkbox_Off;

    public Resources(LauncherWindow launcher){
        Logo = getBufferedImage(launcher.getConfig().get("logo"));

        try{
            if(Files.exists(Paths.get("./background.jpg")))
                Background[0] = ImageIO.read(new File("./background.jpg"));
        }catch (Exception ex){}
        for(int i = 1; i < Background.length; i++) {
            try {
                Background[i] = getBufferedImage("background/bg_" + i + ".jpg");
            }catch (Exception ex){}
        }
        Icon = getBufferedImage(launcher.getConfig().get("icon"));

        Icon_Info = getBufferedImage("info_icon.png");
        Icon_Play = getBufferedImage("play_icon.png");
        Icon_Profile = getBufferedImage("profile_icon.png");
        Icon_Settings = getBufferedImage("settings_icon.png");
        Icon_Book = getBufferedImage("book_icon.png");
        Icon_Videos = getBufferedImage("videos_icon.png");
        Icon_Image = getBufferedImage("image_icon.png");
        Icon_Frame = getBufferedImage("frame_icon.png");

        Logo_Youtube = getBufferedImage("youtube_logo.png");
        Logo_VK = getBufferedImage("vk_logo.png");

        Icon_Checkbox_On = getBufferedImage("checkbox_on.png");
        Icon_Checkbox_Off = getBufferedImage("checkbox_off.png");
    }
}
