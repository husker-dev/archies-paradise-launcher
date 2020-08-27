package com.husker.launcher.server;

import com.husker.launcher.server.utils.ConsoleUtils;
import com.husker.launcher.server.utils.GetParameters;
import com.husker.launcher.server.utils.ProfileUtils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public Client(Socket client){
        String ip = client.getInetAddress().getHostAddress();

        ConsoleUtils.printDebug(Client.class, ip + ": Connected");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String line = in.readLine();
            if(line == null)
                return;
            ConsoleUtils.printDebug(Client.class, ip + " -> " + line);

            try {
                GetParameters parameters = GetParameters.create(line);
                if(ClientGetters.textGetters.containsKey(parameters.getTitle())) {
                    GetParameters outParameters = new GetParameters(parameters.getTitle());
                    try {
                        ClientGetters.textGetters.get(parameters.getTitle()).apply(parameters, outParameters);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    ConsoleUtils.printDebug(Client.class, ip + " <- " + outParameters);
                    ProfileUtils.sendText(client, outParameters);
                }
                if(ClientGetters.imageGetters.containsKey(parameters.getTitle())) {
                    BufferedImage image = null;
                    try {
                        image = ClientGetters.imageGetters.get(parameters.getTitle()).apply(parameters);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    ConsoleUtils.printDebug(Client.class, ip + " <- [Image]");
                    ProfileUtils.sendImage(client, image);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }

            if(client.isConnected())
                client.close();

            ConsoleUtils.printDebug(Client.class, ip + ": Disconnected");
        }catch (Exception ex){
            ConsoleUtils.printDebug(Client.class, ip + " " + ex.getMessage());
        }
    }

    public interface TextGetter{
        void apply(GetParameters in, GetParameters out);
    }

    public interface ImageGetter{
        BufferedImage apply(GetParameters in);
    }
}
