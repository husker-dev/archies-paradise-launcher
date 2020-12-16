package com.husker.launcher;


import com.husker.launcher.discord.Discord;
import com.husker.launcher.managers.UpdateManager;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;


public class Main {

    public static void main(String[] args) {

        UpdateManager.enable = false;

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
        System.setProperty("sun.java2d.ddscale", "true");
        System.setProperty("sun.java2d.ddoffscreen", "true");
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.awt.noerasebackground", "true");

        Discord.init();

        new LoadingWindow();
    }



}