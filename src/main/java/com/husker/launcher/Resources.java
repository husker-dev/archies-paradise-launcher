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
import java.util.ArrayList;

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
    public final BufferedImage Icon_Edit;
    public final BufferedImage Icon_Edit_Selected;
    public final BufferedImage Icon_Arrow_Left;
    public final BufferedImage Icon_Arrow_Right;
    public final BufferedImage Icon_Arrow_Left_Selected;
    public final BufferedImage Icon_Arrow_Right_Selected;
    public final BufferedImage Icon_Dot;
    public final BufferedImage Icon_Dot_Selected;
    public final BufferedImage Icon_Reply;
    public final BufferedImage Icon_Reply_Selected;
    public final BufferedImage Icon_Download;
    public final BufferedImage Icon_Reload;
    public final BufferedImage Icon_Reload_Selected;
    public final BufferedImage Icon_Code;
    public final BufferedImage Icon_People;
    public final BufferedImage Icon_Key;
    public final BufferedImage Icon_Folder;

    public final BufferedImage Social_Loading_Logo;
    public final BufferedImage Logo_Youtube;
    public final BufferedImage Logo_VK;

    public final BufferedImage Loading_Background;

    public final BufferedImage Skin_Steve;

    public final BufferedImage[] Background;

    public final BufferedImage Icon_Checkbox_On;
    public final BufferedImage Icon_Checkbox_Off;

    public Resources(Launcher launcher){
        Logo = getBufferedImage(launcher.getConfig().Launcher.getLogo());

        BufferedImage custom = null;
        try{
            if(Files.exists(Paths.get("./background.jpg")))
                custom = ImageIO.read(new File("./background.jpg"));
        }catch (Exception ex){}

        ArrayList<BufferedImage> bgs = new ArrayList<>();
        for(int i = 1;;i++){
            BufferedImage bg = getBufferedImage("background/bg_" + i + ".jpg");
            if(bg == null)
                break;
            bgs.add(bg);
        }
        Background = new BufferedImage[1 + bgs.size()];
        Background[0] = custom;
        for(int i = 0; i < bgs.size(); i++)
            Background[i + 1] = bgs.get(i);

        Icon = getBufferedImage(launcher.getConfig().Launcher.getIcon());

        Icon_Info = getBufferedImage("info_icon.png");
        Icon_Play = getBufferedImage("play_icon.png");
        Icon_Profile = getBufferedImage("profile_icon.png");
        Icon_Settings = getBufferedImage("settings_icon.png");
        Icon_Book = getBufferedImage("book_icon.png");
        Icon_Videos = getBufferedImage("videos_icon.png");
        Icon_Image = getBufferedImage("image_icon.png");
        Icon_Frame = getBufferedImage("frame_icon.png");
        Icon_Edit = getBufferedImage("edit_icon.png");
        Icon_Edit_Selected = getBufferedImage("edit_icon_selected.png");
        Icon_Arrow_Left = getBufferedImage("arrow_left.png");
        Icon_Arrow_Right = getBufferedImage("arrow_right.png");
        Icon_Arrow_Left_Selected = getBufferedImage("arrow_left_selected.png");
        Icon_Arrow_Right_Selected = getBufferedImage("arrow_right_selected.png");
        Icon_Dot = getBufferedImage("dot.png");
        Icon_Dot_Selected = getBufferedImage("dot_selected.png");
        Icon_Reply = getBufferedImage("reply.png");
        Icon_Reply_Selected = getBufferedImage("reply_selected.png");
        Icon_Download = getBufferedImage("download_icon.png");
        Icon_Reload = getBufferedImage("reload.png");
        Icon_Reload_Selected = getBufferedImage("reload_selected.png");
        Icon_Code = getBufferedImage("code_icon.png");
        Icon_People = getBufferedImage("people_icon.png");
        Icon_Key = getBufferedImage("key_icon.png");
        Icon_Folder = getBufferedImage("folder_icon.png");

        Social_Loading_Logo = getBufferedImage("social_loading_logo.png");
        Logo_Youtube = getBufferedImage("youtube_logo.png");
        Logo_VK = getBufferedImage("vk_logo.png");

        Loading_Background = getBufferedImage("loading_background.png");

        Skin_Steve = getBufferedImage("steve.png");

        Icon_Checkbox_On = getBufferedImage("checkbox_on.png");
        Icon_Checkbox_Off = getBufferedImage("checkbox_off.png");
    }
}
