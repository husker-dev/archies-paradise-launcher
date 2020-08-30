package com.husker.launcher.server;

import com.husker.launcher.server.utils.ProfileUtils;
import com.husker.launcher.server.utils.SettingsFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerSettingsFile extends SettingsFile {

    private static final String PORT = "port";
    private static final String ENCRYPTION_KEY = "encryption_key";
    private static final String EMAIL = "email";
    private static final String EMAIL_PASSWORD = "email_password";
    private static final String EMAIL_TITLE = "email_title";

    private static final int DEFAULT_PORT = 15565;

    public ServerSettingsFile() {
        super(new File("./server_settings.cfg"));

        setDefault(PORT, DEFAULT_PORT + "");
        setDefault(ENCRYPTION_KEY, ProfileUtils.generateKey());
        setDefault(EMAIL, "null");
        setDefault(EMAIL_PASSWORD, "null");
        setDefault(EMAIL_TITLE, "Email title");
    }

    public String getEmail(){
        return get(EMAIL);
    }

    public String getEmailPassword(){
        String debugPasswordPath = System.getProperty("user.home") + "/Desktop/email_password.txt";
        try {
            if (Files.exists(Paths.get(debugPasswordPath)))
                return Files.readAllLines(Paths.get(debugPasswordPath)).get(0);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return get(EMAIL_PASSWORD);
    }

    public String getEmailTitle(){
        return get(EMAIL_TITLE);
    }

    public int getPort(){
        try{
            Integer.parseInt(get(PORT, DEFAULT_PORT + ""));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return DEFAULT_PORT;
    }

    public String getEncryptionKey(){
        return get(ENCRYPTION_KEY);
    }

    public void setPort(int port){
        set(PORT, port + "");
    }

    public void setPort(String port){
        try {
            setPort(Integer.parseInt(port));
        }catch (Exception ex){
            setPort(DEFAULT_PORT);
        }
    }

    public void setEncryptionKey(String key){
        set(ENCRYPTION_KEY, key);
    }

}
