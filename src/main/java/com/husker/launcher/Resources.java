package com.husker.launcher;

import com.husker.launcher.ui.utils.ImageUtils;
import com.husker.launcher.utils.SystemUtils;
import com.husker.launcher.utils.minecraft.MinecraftClientInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class Resources {

    private static final Logger log = LogManager.getLogger(Resources.class);

    public static InputStream get(String file){
        String path = "resources/" + file;
        log.info("Reading " + path);

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
        }catch (Exception ex){
        }
        return null;
    }

    public static Image getImage(String file){
        try {
            List<Image> imgList = new ArrayList<Image>();
            BufferedImage img = ImageIO.read(get(file));
            imgList.add(img);
            double scale = SystemUtils.getWindowScaleFactor();
            imgList.add(img.getScaledInstance((int)((double)img.getWidth() * scale), (int)((double)img.getHeight() * scale), Image.SCALE_SMOOTH));
            AbstractMultiResolutionImage mrImage = new BaseMultiResolutionImage(imgList.toArray(new Image[0]));
            return mrImage;
            //return ImageIO.read(get(file));
        } catch (IOException e) {
        }
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
    public static BufferedImage Loading_Background;

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

    public static BufferedImage Social_Loading_Logo;
    public static BufferedImage Logo_Youtube;
    public static BufferedImage Logo_VK;
    public static BufferedImage Logo_Instagram;

    public static BufferedImage Skin_Steve;

    public static BufferedImage[] Background;

    public static BufferedImage Icon_Checkbox_On;
    public static BufferedImage Icon_Checkbox_Off;

    public static BufferedImage blurDefaultTexture;


    public static void loadBase(){
        if(Logo == null)
            Logo = getBufferedImage("logo.png");
        if(Icon == null)
            Icon = getBufferedImage("icon.png");
        if(Loading_Background == null)
            Loading_Background = getBufferedImage("loading_background.png");
    }

    public static void load(){
        loadBase();

        ArrayList<BufferedImage> bgs = new ArrayList<>();
        try {
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
        Icon_Fullscreen = getBufferedImage("fullscreen.png");
        Icon_Dot_Light = getBufferedImage("dot_light.png");
        Icon_VR = getBufferedImage("vr_icon.png");
        Icon_VR_Disabled = getBufferedImage("vr_icon_disabled.png");

        Social_Loading_Logo = getBufferedImage("social_loading_logo.png");
        Logo_Youtube = getBufferedImage("youtube_logo.png");
        Logo_VK = getBufferedImage("vk_logo.png");
        Logo_Instagram = getBufferedImage("instagram_logo.png");

        Skin_Steve = getBufferedImage("steve.png");

        Icon_Checkbox_On = getBufferedImage("checkbox_on.png");
        Icon_Checkbox_Off = getBufferedImage("checkbox_off.png");

        blurDefaultTexture = Resources.getBufferedImage("paper.png");
    }

}
