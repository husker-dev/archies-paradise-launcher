package com.husker.launcher.server.settings;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerSettingsFile extends SettingsFile {

    private static final String PORT = "port";

    private static final String EMAIL = "mail.login";
    private static final String EMAIL_PASSWORD = "mail.password";

    private static final String EMAIL_HOST = "mail.host";
    private static final String EMAIL_CODE_TITLE = "mail.code.title";
    private static final String EMAIL_CODE_TEXT = "mail.code.text";
    private static final String EMAIL_PASSWORD_RESET_TITLE = "mail.password_reset.title";
    private static final String EMAIL_PASSWORD_RESET_TEXT = "mail.password_reset.text";
    private static final String EMAIL_PASSWORD_CHANGE_TITLE = "mail.password_code.title";
    private static final String EMAIL_PASSWORD_CHANGE_TEXT = "mail.password_code.text";

    private static final String VK_GROUP = "social.vk";
    private static final String YOUTUBE_ID = "social.youtube";
    private static final String INSTAGRAM_NAME = "social.instagram";
    private static final String GITHUB_REPO = "social.github";

    private static final String OWNER_NAME = "owner.name";
    private static final String OWNER_URL = "owner.url";

    private static final String SUPPORT_NAME = "support.name";
    private static final String SUPPORT_URL = "support.url";

    private static final String MINECRAFT_IP = "minecraft.ip";
    private static final String MINECRAFT_PORT = "minecraft.port";

    private static final String LAUNCHER_VERSION = "launcher.version";

    public ServerSettingsFile() {
        super("./server_settings.yaml");

        setDefault(PORT, 15565);
        setDefault(LAUNCHER_VERSION, "0.1");

        setDefault(EMAIL, "example@mail.com");
        setDefault(EMAIL_PASSWORD, "password");
        setDefault(EMAIL_HOST, "smtp.gmail.com");

        setDefault(EMAIL_CODE_TITLE, "Email title");
        setDefault(EMAIL_CODE_TEXT, "Dear {name}, here is your code: {code}");

        setDefault(EMAIL_PASSWORD_RESET_TITLE, "Email title");
        setDefault(EMAIL_PASSWORD_RESET_TEXT, "Dear {name}, here is your new password: {password}");

        setDefault(EMAIL_PASSWORD_CHANGE_TITLE, "Email title");
        setDefault(EMAIL_PASSWORD_CHANGE_TEXT, "Dear {name}, here is the code to change your password {code}");

        setDefault(VK_GROUP, "id");
        setDefault(YOUTUBE_ID, "id");
        setDefault(INSTAGRAM_NAME, "name");
        setDefault(GITHUB_REPO, "repo");

        setDefault(OWNER_NAME, "owner name");
        setDefault(OWNER_URL, "owner url");

        setDefault(SUPPORT_NAME, "support name");
        setDefault(SUPPORT_URL, "support url");

        setDefault(MINECRAFT_IP, "127.0.0.1");
        setDefault(MINECRAFT_PORT, 25565);
    }

    // Email sending
    public String getEmailHost(){
        return get(EMAIL_HOST);
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

    // Minecraft server
    public String getMinecraftServerIP(){
        return get(MINECRAFT_IP);
    }
    public int getMinecraftServerPort(){
        return getInt(MINECRAFT_PORT);
    }

    public int getPort(){
        try{
            getInt(PORT);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 15565;
    }

    public void setPort(int port){
        set(PORT, port);
    }

    public void setPort(String port){
        try {
            setPort(Integer.parseInt(port));
        }catch (Exception ex){
            setPort(15565);
        }
    }

    // Info
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

    // Social
    public String getVKGroupId(){
        return get(VK_GROUP);
    }
    public void setVKGroupId(String key){
        set(VK_GROUP, key);
    }

    public String getYouTubeId(){
        return get(YOUTUBE_ID);
    }
    public void setYouTubeId(String key){
        set(YOUTUBE_ID, key);
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

    public void setLauncherVersion(String version){
        set(LAUNCHER_VERSION, version);
    }
    public String getLauncherVersion(){
        return get(LAUNCHER_VERSION);
    }

    // Mail

    public String getEmailPasswordTitle(){
        return get(EMAIL_PASSWORD_RESET_TITLE);
    }
    public String getEmailPasswordText(){
        return get(EMAIL_PASSWORD_RESET_TEXT);
    }

    public String getEmailCodeTitle(){
        return get(EMAIL_CODE_TITLE);
    }
    public String getEmailCodeText(){
        return get(EMAIL_CODE_TEXT);
    }

    public String getEmailPasswordChangeTitle(){
        return get(EMAIL_PASSWORD_CHANGE_TITLE);
    }
    public String getEmailPasswordChangeText(){
        return get(EMAIL_PASSWORD_CHANGE_TEXT);
    }
}
