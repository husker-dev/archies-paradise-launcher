package com.husker.launcher;

import com.husker.launcher.ui.utils.ImageUtils;
import com.husker.launcher.utils.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Resources {

    private static final Logger log = LogManager.getLogger(Resources.class);

    public static InputStream get(String file){
        String path = "resources/" + file;
        log.info("Reading \"" + path + "\"");
        path = SystemUtils.fixPath(path);

        try {
            return new FileInputStream(new File(path));
        }catch (Exception ex){
            log.error(new NullPointerException("Can't load: " + file));
            return null;
        }
    }

    public static BufferedImage getBufferedImage(String file){
        try {
            return ImageUtils.toBufferedImage(ImageIO.read(get(file)));
        }catch (Exception ignored){
            BufferedImage image = new BufferedImage(128 , 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            int rectSize = 8;
            for(int i = 0; i < image.getWidth(); i += rectSize){
                for(int r = 0; r < image.getHeight(); r += rectSize){
                    Color color = Color.black;
                    if(i / rectSize % 2 == r / rectSize % 2)
                        color = new Color(255, 0, 255);
                    g2d.setColor(color);
                    g2d.fillRect(i, r, rectSize, rectSize);
                }
            }
            return image;
        }
    }

    public static BufferedImage loadIcon(String fileName){
        return getBufferedImage("icons/" + fileName);
    }

    public static Image getImage(String file){
        try {
            return ImageIO.read(get(file));
        } catch (IOException ignored) { }
        return null;
    }

    public static ImageIcon getIcon(String file){
        return new ImageIcon(getBufferedImage(file));
    }

    public static class Fonts {
        private static Font SFPro;
        private static Font ChronicaPro;
        private static Font ChronicaPro_Bold;
        private static Font ChronicaPro_ExtraBold;

        private static Boolean needTransform = null;

        private static AffineTransform createTransform(double fontSize){
            if(needTransform())
                return AffineTransform.getTranslateInstance(0, (int)(fontSize / 4.5));
            else
                return null;
        }

        public static boolean needTransform(){
            if(needTransform == null) {
                BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = testImage.createGraphics();
                g2d.setFont(ChronicaPro_ExtraBold.deriveFont(16f));
                g2d.setColor(Color.black);
                g2d.drawString("test", 0, 16);
                needTransform = testImage.getRGB(4, 6) != 0;
            }
            return needTransform;
        }

        public static Font getChronicaPro(double size){
            return ChronicaPro.deriveFont(new HashMap<TextAttribute, Object>(){{
                put(TextAttribute.FAMILY, "ChronicaPro-Black");
                put(TextAttribute.SIZE, size);
                put(TextAttribute.TRANSFORM, createTransform(size));
            }});
        }

        public static Font getChronicaProBold(double size){
            return ChronicaPro_Bold.deriveFont(new HashMap<TextAttribute, Object>(){{
                put(TextAttribute.FAMILY, "ChronicaPro-Bold");
                put(TextAttribute.SIZE, size);
                put(TextAttribute.TRANSFORM, createTransform(size));
            }});
        }

        public static Font getChronicaProExtraBold(double size){
            return ChronicaPro_ExtraBold.deriveFont(new HashMap<TextAttribute, Object>(){{
                put(TextAttribute.FAMILY, "ChronicaPro-ExtraBold");
                put(TextAttribute.SIZE, size);
                put(TextAttribute.TRANSFORM, createTransform(size));
            }});
        }

        public static Font getSFPro(double size){
            return SFPro.deriveFont(new HashMap<TextAttribute, Object>(){{
                put(TextAttribute.FAMILY, "SFPro");
                put(TextAttribute.SIZE, size);
                put(TextAttribute.TRANSFORM, createTransform(size));
            }});
        }

        public static Font getChronicaPro(){
            return getChronicaPro(16);
        }

        public static Font getChronicaProBold(){
            return getChronicaProBold(16);
        }

        public static Font getChronicaProExtraBold(){
            return getChronicaProExtraBold(16);
        }

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

        public static void setDefaultFontRenderer(){
            SystemUtils.setEnv("FREETYPE_PROPERTIES", "truetype:interpreter-version=35");
        }
    }

    public static BufferedImage Logo;
    public static BufferedImage Icon;
    public static BufferedImage Background_Loading;

    public static BufferedImage[] Background;

    public static BufferedImage Icon_Info;
    public static BufferedImage Icon_Play;
    public static BufferedImage Icon_Profile;
    public static BufferedImage Icon_Settings;
    public static BufferedImage Icon_Book;
    public static BufferedImage Icon_Videos;
    public static BufferedImage Icon_Image;
    public static BufferedImage Icon_Frame;
    public static BufferedImage Icon_Edit;
    public static BufferedImage Icon_Edit_Selected;
    public static BufferedImage Icon_Arrow_Left;
    public static BufferedImage Icon_Arrow_Right;
    public static BufferedImage Icon_Arrow_Left_Selected;
    public static BufferedImage Icon_Arrow_Right_Selected;
    public static BufferedImage Icon_Dot;
    public static BufferedImage Icon_Dot_Selected;
    public static BufferedImage Icon_Reply;
    public static BufferedImage Icon_Reply_Selected;
    public static BufferedImage Icon_Download;
    public static BufferedImage Icon_Reload;
    public static BufferedImage Icon_Reload_Selected;
    public static BufferedImage Icon_Code;
    public static BufferedImage Icon_People;
    public static BufferedImage Icon_Key;
    public static BufferedImage Icon_Folder;
    public static BufferedImage Icon_Fullscreen;
    public static BufferedImage Icon_Dot_Light;
    public static BufferedImage Icon_VR;
    public static BufferedImage Icon_VR_Disabled;
    public static BufferedImage Icon_Subject;
    public static BufferedImage Icon_Checkbox_On;
    public static BufferedImage Icon_Checkbox_Off;
    public static BufferedImage Icon_Back;
    public static BufferedImage Icon_Add;

    public static BufferedImage Logo_Social;
    public static BufferedImage Logo_Youtube;
    public static BufferedImage Logo_VK;
    public static BufferedImage Logo_Instagram;

    public static BufferedImage Skin_Steve;

    public static BufferedImage blurDefaultTexture;

    public static void loadBase(){
        if(Logo == null)
            Logo = getBufferedImage("logo.png");
        if(Icon == null)
            Icon = getBufferedImage("icon.png");
        if(Background_Loading == null)
            Background_Loading = getBufferedImage("loading_background.png");
    }

    public static void load(){
        loadBase();

        updateBackgrounds();
        Icon_Info = loadIcon("info.png");
        Icon_Play = loadIcon("play.png");
        Icon_Profile = loadIcon("profile.png");
        Icon_Settings = loadIcon("settings.png");
        Icon_Book = loadIcon("book.png");
        Icon_Videos = loadIcon("videos.png");
        Icon_Image = loadIcon("image.png");
        Icon_Frame = loadIcon("frame.png");
        Icon_Edit = loadIcon("edit.png");
        Icon_Edit_Selected = loadIcon("edit_selected.png");
        Icon_Arrow_Left = loadIcon("arrow_left.png");
        Icon_Arrow_Right = loadIcon("arrow_right.png");
        Icon_Arrow_Left_Selected = loadIcon("arrow_left_selected.png");
        Icon_Arrow_Right_Selected = loadIcon("arrow_right_selected.png");
        Icon_Dot = loadIcon("dot.png");
        Icon_Dot_Selected = loadIcon("dot_selected.png");
        Icon_Reply = loadIcon("reply.png");
        Icon_Reply_Selected = loadIcon("reply_selected.png");
        Icon_Download = loadIcon("download.png");
        Icon_Reload = loadIcon("reload.png");
        Icon_Reload_Selected = loadIcon("reload_selected.png");
        Icon_Code = loadIcon("code.png");
        Icon_People = loadIcon("people.png");
        Icon_Key = loadIcon("key.png");
        Icon_Folder = loadIcon("folder.png");
        Icon_Fullscreen = loadIcon("fullscreen.png");
        Icon_Dot_Light = loadIcon("dot_light.png");
        Icon_VR = loadIcon("vr.png");
        Icon_VR_Disabled = loadIcon("vr_disabled.png");
        Icon_Subject = loadIcon("subject.png");
        Icon_Checkbox_On = loadIcon("checkbox_on.png");
        Icon_Checkbox_Off = loadIcon("checkbox_off.png");
        Icon_Back = loadIcon("back.png");
        Icon_Add = loadIcon("add.png");

        Logo_Social = getBufferedImage("social_loading_logo.png");
        Logo_Youtube = getBufferedImage("youtube_logo.png");
        Logo_VK = getBufferedImage("vk_logo.png");
        Logo_Instagram = getBufferedImage("instagram_logo.png");

        Skin_Steve = getBufferedImage("steve.png");

        blurDefaultTexture = getBufferedImage("paper.png");
    }

    public static void updateBackgrounds(){
        ArrayList<BufferedImage> bgs = new ArrayList<>();
        try {
            bgs.add(getBufferedImage("background1.jpg"));
            bgs.add(getBufferedImage("background.jpg"));

            String backgroundsPath = SystemUtils.getSettingsFolder() + "/background";
            Files.createDirectories(Paths.get(backgroundsPath));
            for(File file : new File(backgroundsPath).listFiles()){
                try{
                    bgs.add(ImageIO.read(file));
                }catch (Exception ignored){}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Background = bgs.toArray(new BufferedImage[0]);
    }

}
