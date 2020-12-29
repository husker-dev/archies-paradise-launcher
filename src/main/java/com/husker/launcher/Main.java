package com.husker.launcher;


import com.husker.launcher.discord.Discord;
import com.husker.launcher.managers.UpdateManager;
import com.husker.launcher.settings.LauncherSettings;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;


public class Main {

    public static void main(String[] args) {
        System.setProperty("illegal-access", "permit");
        System.out.println("Java version: " + System.getProperty("java.version"));
        Resources.Fonts.setDefaultFontRenderer();
        System.out.println("truetype:interpreter-version=35".equals(System.getenv("FREETYPE_PROPERTIES")) ? "With font renderer fix": "Without font renderer fix");

        try {
            Properties props = new Properties();
            props.load(Main.class.getResourceAsStream("/log4j.properties"));
            PropertyConfigurator.configure(props);

            Logger logger = LogManager.getLogger("org.apache.http");
            logger.setLevel(Level.INFO);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        //System.setProperty("sun.java2d.opengl", "true");
        if(!LauncherSettings.isPotatoSettings()) {
            System.setProperty("sun.java2d.ddscale", "true");
            System.setProperty("sun.java2d.ddoffscreen", "true");
            if (!Resources.Fonts.needTransform())
                System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            System.setProperty("sun.awt.noerasebackground", "true");
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }

        Discord.init();

        new LoadingWindow();
    }



}