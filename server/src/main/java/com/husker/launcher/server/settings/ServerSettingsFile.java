package com.husker.launcher.server.settings;

import com.husker.launcher.server.utils.settings.SettingsFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerSettingsFile extends SettingsFile {

    private static final String PORT = "port";

    private static final String EMAIL = "mail.login";
    private static final String EMAIL_PASSWORD = "mail.password";
    private static final String EMAIL_TITLE = "mail.title";
    private static final String EMAIL_TEXT = "mail.text";

    private static final String DELAY = "social.delay";

    private static final String VK_GROUP = "social.vk.group_id";
    private static final String YOUTUBE = "social.youtube.channel_id";
    private static final String INSTAGRAM_NAME = "social.instagram.name";
    private static final String GITHUB_REPO = "social.github.repo";

    private static final String OWNER_NAME = "owner.name";
    private static final String OWNER_URL = "owner.url";

    private static final String SUPPORT_NAME = "support.name";
    private static final String SUPPORT_URL = "support.url";

    private static final String MINECRAFT_IP = "minecraft.ip";
    private static final String MINECRAFT_PORT = "minecraft.port";

    public ServerSettingsFile() {
        super(new File("./server_settings.cfg"));

        setDefault(PORT, 15565 + "");
        setDefault(EMAIL, "[email]");
        setDefault(EMAIL_PASSWORD, "[password]");
        setDefault(EMAIL_TITLE, "Email title");
        setDefault(EMAIL_TEXT, "Here is your code [code]");

        setDefault(VK_GROUP, "[id]");
        setDefault(YOUTUBE, "[id]");
        setDefault(INSTAGRAM_NAME, "[name]");
        setDefault(GITHUB_REPO, "[repo]");

        setDefault(OWNER_NAME, "[owner name]");
        setDefault(OWNER_URL, "[owner url]");

        setDefault(SUPPORT_NAME, "[support name]");
        setDefault(SUPPORT_URL, "[support url]");

        setDefault(MINECRAFT_IP, "[server ip]");
        setDefault(MINECRAFT_PORT, "[server port]");
    }

    public String getMinecraftServerIP(){
        return get(MINECRAFT_IP);
    }

    public int getMinecraftServerPort(){
        return Integer.parseInt(get(MINECRAFT_PORT));
    }

    public String getOwnerName(){
        return get(OWNER_NAME);
    }

    public String getOwnerUrl(){
        return get(OWNER_URL);
    }

    public String getSupportName(){
        return get(SUPPORT_NAME);
    }

    public String getSupportUrl(){
        return get(SUPPORT_URL);
    }

    public String getEmail(){
        return get(EMAIL);
    }

    public String getEmailPassword(){
        try {
            if (Files.exists(Paths.get(get(EMAIL_PASSWORD))))
                return Files.readAllLines(Paths.get(get(EMAIL_PASSWORD))).get(0);
            else
                return get(EMAIL_PASSWORD);
        }catch (Exception ex){
            return null;
        }
    }

    public String getEmailTitle(){
        return get(EMAIL_TITLE);
    }

    public int getPort(){
        try{
            Integer.parseInt(get(PORT));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 15565;
    }

    public void setPort(int port){
        set(PORT, port + "");
    }

    public void setPort(String port){
        try {
            setPort(Integer.parseInt(port));
        }catch (Exception ex){
            setPort(15565);
        }
    }

    public String getVKGroupId(){
        return get(VK_GROUP);
    }

    public void setVKGroupId(String key){
        set(VK_GROUP, key);
    }

    public String getYouTubeId(){
        return get(YOUTUBE);
    }

    public void setYouTubeId(String key){
        set(YOUTUBE, key);
    }

    public String getInstagramId(){
        return get(INSTAGRAM_NAME);
    }

    public void setInstagramId(String key){
        set(INSTAGRAM_NAME, key);
    }

    public String getGitHubId(){
        return get(GITHUB_REPO);
    }

    public void setGitHubId(String key){
        set(GITHUB_REPO, key);
    }

    public String getEmailText(){
        return get(EMAIL_TEXT);
    }

    public int getSocialDelay(){
        try {
            return Integer.parseInt(get(DELAY, "10"));
        }catch (Exception ex){
            return 10;
        }
    }
}
