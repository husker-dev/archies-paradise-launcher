package com.husker.launcher;


import com.husker.launcher.settings.LauncherSettings;
import com.husker.launcher.utils.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.awt.*;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;


public class Main {

    // IP: 45.67.230.69

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Java version: " + System.getProperty("java.version"));
        log.info("Launcher version: " + Launcher.VERSION);
        log.info("OS: " + SystemUtils.getOSName() + " x" + SystemUtils.getArch());
        log.info("Window scale: " + SystemUtils.getWindowScaleFactor());
        log.info("Settings folder:" + SystemUtils.getSettingsFolder());
        System.setProperty("launcher.settingsPath", SystemUtils.getSettingsFolder());

        System.setProperty("illegal-access", "permit");
        Resources.Fonts.setDefaultFontRenderer();
        log.info("Font render fix: " + ("truetype:interpreter-version=35".equals(System.getenv("FREETYPE_PROPERTIES")) ? "ENABLED" : "DISABLED"));

        try {
            Properties props = new Properties();
            props.load(Main.class.getResourceAsStream("/log4j.properties"));
            PropertyConfigurator.configure(props);

            Logger logger = LogManager.getLogger("org.apache.http");
            logger.setLevel(Level.INFO);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if(!LauncherSettings.isPotatoSettings()) {
            System.setProperty("sun.java2d.ddscale", "true");
            System.setProperty("sun.java2d.ddoffscreen", "true");
            if (!Resources.Fonts.needTransform())
                System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            System.setProperty("sun.awt.noerasebackground", "true");
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
        }

        new Timer().schedule(new TimerTask() {
            public void run() {
                System.gc();
            }
        }, 0, 1000);

        new LoadingWindow();
    }
}