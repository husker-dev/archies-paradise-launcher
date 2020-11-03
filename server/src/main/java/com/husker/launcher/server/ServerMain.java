package com.husker.launcher.server;

import com.husker.launcher.server.services.ClientService;
import com.husker.launcher.server.services.ConsoleService;
import com.husker.launcher.server.settings.ServerSettingsFile;
import com.husker.launcher.server.utils.ConsoleUtils;
import com.husker.launcher.server.utils.MailManager;


public class ServerMain {


    public static final ServerSettingsFile Settings = new ServerSettingsFile();

    public static MailManager MailManager = new MailManager(Settings.getEmail(), Settings.getEmailPassword());

    public static ClientService ClientService = new ClientService();
    public static ConsoleService ConsoleService = new ConsoleService();

    public static void main(String[] args){
        ConsoleUtils.printDebug(ServerMain.class, "Starting services...");

        ClientService.start();
        ConsoleService.start();

        ConsoleUtils.printDebug(ServerMain.class, "Server is ready!");


    }

}
