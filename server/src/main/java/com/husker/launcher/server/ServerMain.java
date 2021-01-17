package com.husker.launcher.server;

import com.husker.launcher.server.settings.ServerSettingsFile;
import com.husker.launcher.server.services.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;


public class ServerMain {

    private static final Logger log = LogManager.getLogger(ServerMain.class);

    public static final ServerSettingsFile Settings = new ServerSettingsFile();

    public static MailService MailService = new MailService();
    public static HttpService ClientService = new HttpService();
    public static ConsoleService ConsoleService = new ConsoleService();

    public static void main(String[] args){
        try {
            Properties props = new Properties();
            props.load(ServerMain.class.getResourceAsStream("/log4j.properties"));
            PropertyConfigurator.configure(props);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        log.info("Starting \"Mail Service\"...");
        MailService.run();

        log.info("Starting \"HTTP Service\"...");
        ClientService.run();

        log.info("Starting \"Console Service\"...");
        ConsoleService.run();

        log.info("Server is ready!");
        log.info(new String(new char[25]).replace("\0", "-"));

    }

}
