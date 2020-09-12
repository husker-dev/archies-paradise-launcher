package com.husker.launcher.server.services;

import com.husker.launcher.server.Client;
import com.husker.launcher.server.ServerMain;
import com.husker.launcher.server.Service;

import java.net.ServerSocket;
import java.net.Socket;

public class ClientService extends Service {

    public ServerSocket Server;

    public void onStart() {
        try {
            Server = new ServerSocket(ServerMain.Settings.getPort());
            started();

            while (true) {
                Socket client = Server.accept();
                new Thread(() -> new Client(client)).start();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
