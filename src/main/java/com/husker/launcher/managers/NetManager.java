package com.husker.launcher.managers;

import com.husker.glassui.components.social.vk.VkPostParameter;
import com.husker.glassui.components.social.youtube.YoutubeVideoParameters;
import com.husker.launcher.Launcher;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class NetManager {

    public static boolean enable = true;

    public static final int DEFAULT_AUTH_SERVER_PORT = 15565;

    public static final String RESULT = "result";
    public static final String KEY = "key";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EMAIL_CODE = "email_code";
    public static final String ENCRYPTED = "encrypted";
    public static final String STATUS = "status";
    public static final String SKIN_NAME = "skinName";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String HAS_SKIN = "hasSkin";
    public static final String ID = "id";

    public ArrayList<NetManager.ServerStatus> statusList = new ArrayList<>();
    private final Launcher launcher;

    public NetManager(Launcher launcher){
        this.launcher = launcher;
        new Thread(() -> {
            try {
                if(enable) {
                    while(true) {
                        if (launcher != null) {
                            statusList = new ArrayList<>(getServerOnlineStatus());
                            Thread.sleep(launcher.getConfig().Net.Minecraft.getTimeout() + launcher.getConfig().Net.Internet.getTimeout() + launcher.getConfig().Net.Auth.getTimeout() + 3000);
                        } else
                            Thread.sleep(200);
                    }
                }
            } catch (InterruptedException e) {
            }
        }).start();
    }

    public void updateStatusLabel(JLabel label){
        if(label == null)
            return;

        Color red = new Color(160, 0, 0);
        Color yellow = new Color(140, 140, 0);
        Color green = new Color(0, 160, 0);

        if(statusList.contains(NetManager.ServerStatus.INTERNET_OFFLINE)){
            label.setText("Нет интернета");
            label.setForeground(yellow);
        }else if(statusList.contains(NetManager.ServerStatus.AUTH_ONLINE) && statusList.contains(NetManager.ServerStatus.MINECRAFT_SERVER_ONLINE)){
            label.setText("Онлайн");
            label.setForeground(green);
        } else if(!statusList.contains(NetManager.ServerStatus.AUTH_ONLINE) && statusList.contains(NetManager.ServerStatus.MINECRAFT_SERVER_ONLINE)){
            label.setText("Авторизация недоступна");
            label.setForeground(yellow);
        }else if(statusList.contains(NetManager.ServerStatus.AUTH_ONLINE) && !statusList.contains(NetManager.ServerStatus.MINECRAFT_SERVER_ONLINE)){
            label.setText("Доступна авторизация");
            label.setForeground(yellow);
        }else{
            label.setText("Офлайн");
            label.setForeground(red);
        }
    }

    public enum ServerStatus{
        INTERNET_OFFLINE,
        INTERNET_ONLINE,
        AUTH_OFFLINE,
        AUTH_ONLINE,
        MINECRAFT_SERVER_OFFLINE,
        MINECRAFT_SERVER_ONLINE,
    }

    public List<ServerStatus> getServerOnlineStatus(){
        ArrayList<ServerStatus> status = new ArrayList<>();
        try {
            if(ping(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort(), launcher.getConfig().Net.Auth.getTimeout()))
                status.add(ServerStatus.AUTH_ONLINE);
            else
                status.add(ServerStatus.AUTH_OFFLINE);
        }catch (Exception ex){
            status.add(ServerStatus.AUTH_OFFLINE);
        }
        try{
            if(ping(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort(), launcher.getConfig().Net.Auth.getTimeout()))
                status.add(ServerStatus.MINECRAFT_SERVER_ONLINE);
            else
                status.add(ServerStatus.MINECRAFT_SERVER_OFFLINE);
        }catch (Exception ex){
            ex.printStackTrace();
            status.add(ServerStatus.MINECRAFT_SERVER_OFFLINE);
        }
        try{
            if(InetAddress.getByName(launcher.getConfig().Net.Internet.getIp()).isReachable(launcher.getConfig().Net.Internet.getTimeout()))
                status.add(ServerStatus.INTERNET_ONLINE);
            else
                status.add(ServerStatus.INTERNET_OFFLINE);
        }catch (Exception ex){
            status.add(ServerStatus.INTERNET_OFFLINE);
        }
        return status;
    }

    public boolean ping(String ip, int port, int timeout){
        boolean out = false;
        try{
            Socket client = new Socket();
            client.connect(new InetSocketAddress(ip, port), timeout);
            out = client.isConnected();
            client.close();
        }catch (Exception ex){
        }
        return out;
    }

    public void openLink(String url){
        if(Desktop.isDesktopSupported()){
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ignored) { }
        }else{
            try {
                Runtime.getRuntime().exec("xdg-open " + url);
            } catch (Exception ignored) {
            }
        }
    }

    public String getURLContent(String url) throws IOException {
        if(url == null)
            return null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public Players Players = new Players(this);
    public Email Email = new Email(this);
    public Auth Auth = new Auth(this);
    public ProfileInfo PlayerInfo = new ProfileInfo(this);
    public Skins Skins = new Skins(this);
    public Social Social = new Social(this);

    public ArrayList<Long> threadQueue = new ArrayList<>();

    public void connect() throws IOException {
        joinThreadQueue();
        socket = new Socket();
        socket.connect(new InetSocketAddress(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort()), launcher.getConfig().Net.Auth.getTimeout());

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void disconnect(){
        try {
            socket.close();
        }catch (Exception ignored){}
        try {
            in.close();
        }catch (Exception ignored){}
        try {
            out.close();
        }catch (Exception ignored){}
        leaveThreadQueue();
    }

    public GetRequest get(GetRequest parameters) throws IOException {
        try {
            connect();
            parameters.send(socket);

            GetRequest received = GetRequest.create(socket);
            disconnect();

            return received;
        }catch (Exception exception){
            disconnect();
            throw exception;
        }
    }

    private void joinThreadQueue(){
        threadQueue.add(Thread.currentThread().getId());
        while(threadQueue.size() > 0 && !threadQueue.get(0).equals(Thread.currentThread().getId())){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void leaveThreadQueue(){
        threadQueue.remove(Thread.currentThread().getId());
    }

    public static class Skins {

        private final NetManager manager;
        public Skins(NetManager manager){
            this.manager = manager;
        }

        public String[] getFolders() throws IOException {
            return manager.get(GetRequest.createWithTitle("get_skin_folders")).getString("folders").split(",");
        }

        public String[] getFolderSkinCount(String folder) throws IOException {
            return manager.get(GetRequest.createWithTitle("get_skin_folder_skins", "folder", folder)).getString("skins").split(",");
        }

        public BufferedImage getFolderSkin(String folder, String name) throws IOException {
            return manager.get(GetRequest.createWithTitle("get_folder_skin", "folder", folder, "name", name)).getImage("skin");
        }

        public BufferedImage getFolderPreview(String folder) throws IOException {
            return manager.get(GetRequest.createWithTitle("get_skin_folder_preview", "folder", folder)).getImage("skin");
        }
    }

    public static class Players {

        private final NetManager manager;
        public Players(NetManager manager){
            this.manager = manager;
        }

        public final int ERROR = -1;
        public final int NAME_NOT_TAKEN = 0;
        public final int NAME_TAKEN = 1;

        public final int SUCCESSFUL_REGISTRATION = 0;
        public final int BAD_PASSWORD = 2;

        public int checkNickname(String name){
            try {
                return manager.get(GetRequest.createWithTitle("is_login_taken", LOGIN, name)).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int register(String login, String password){
            try {
                return manager.get(GetRequest.createWithTitle("register", LOGIN, login, PASSWORD, password)).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }
    }

    public static class Email {

        public final int ERROR = -1;
        public final int OK = 0;

        private final NetManager manager;
        public Email(NetManager manager){
            this.manager = manager;
        }

        public int sendConfirmCode(String login, String password, String email, boolean encrypted){
            try {
                return manager.get(GetRequest.createWithTitle("send_email_code", LOGIN, login, PASSWORD, password, EMAIL, email, ENCRYPTED, encrypted ? "1" : "0")).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int confirmMail(String login, String password, String email, String code, boolean encrypted){
            try {
                return manager.get(GetRequest.createWithTitle("confirm_mail", LOGIN, login, PASSWORD, password, EMAIL, email, EMAIL_CODE, code, ENCRYPTED, encrypted ? "1" : "0")).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }
    }

    public static class Auth {

        public final int OK = 0;
        public final int WRONG_DATA = 1;
        public final int CONNECTION_ERROR = -1;

        private final NetManager manager;
        public Auth(NetManager manager){
            this.manager = manager;
        }

        public int auth(String login, String password, boolean encrypted){
            try {
                String result = manager.get(GetRequest.createWithTitle("get_key", LOGIN, login, PASSWORD, password, ENCRYPTED, encrypted ? "1" : "0")).getString(KEY);
                if(result.equals("-1"))
                    return WRONG_DATA;
                else {
                    manager.PlayerInfo.applyKey(result);
                    return OK;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return CONNECTION_ERROR;
        }
    }


    public static class ProfileInfo {

        private String key = "";

        private String name = "Имя";
        private String email = "Почта";
        private String status = "Статус";
        private long id = -1;
        private boolean has_skin = false;
        private String skin_name = null;
        private BufferedImage skin;
        private boolean emailConfirmed = false;

        private String encryptedPassword;

        public final int DATASET_BAD_EMAIL_CODE = 5;
        public final int DATASET_BAD_EMAIL = 4;
        public final int DATASET_NAME_TAKEN = 3;
        public final int DATASET_BAD_NAME = 2;
        public final int DATASET_WRONG_PASSWORD = 1;
        public final int DATASET_OK = 0;
        public final int DATASET_SERVER_ERROR = -1;
        public final int DATASET_ERROR = -2;

        private final NetManager manager;
        public ProfileInfo(NetManager manager){
            this.manager = manager;
        }

        void applyKey(String key) throws IOException{
            this.key = key;

            updateData();
        }

        public void updateData() throws IOException{
            GetRequest parameters = manager.get(GetRequest.createWithTitle("get_profile_data", "get", String.join(",", new String[]{LOGIN, EMAIL, SKIN_NAME, HAS_SKIN, ID, STATUS}), KEY, key));
            name = parameters.getString(LOGIN);
            email = parameters.getString(EMAIL);
            id = parameters.getLong(ID);
            has_skin = parameters.getString(HAS_SKIN).equals("1");
            skin_name = parameters.getString(SKIN_NAME).equals("null") ? null : parameters.getString(SKIN_NAME);
            status = parameters.getString(STATUS);

            encryptedPassword = manager.get(GetRequest.createWithTitle("get_encrypted_password", KEY, key)).getString(PASSWORD);

            emailConfirmed = manager.get(GetRequest.createWithTitle("is_email_confirmed", KEY, key)).getInt(RESULT) == 1;

            if(has_skin)
                skin = manager.get(GetRequest.createWithTitle("skin", KEY, key)).getImage("skin");
            else
                skin = manager.launcher.Resources.Skin_Steve;
        }

        public String getNickname(){
            return name;
        }

        public String getEmail(){
            return email;
        }

        public String getKey(){
            return key;
        }

        public long getId(){
            return id;
        }

        public BufferedImage getSkin(){
            return skin;
        }

        public String getSkinName(){
            return skin_name;
        }

        public String getStatus() {
            return status;
        }

        public String getEncryptedPassword(){
            return encryptedPassword;
        }

        public boolean isEmailConfirmed() {
            return emailConfirmed;
        }

        public int setData(String currentPassword, String... data){
            try {
                ArrayList<String> dataList = new ArrayList<>(Arrays.asList(data));
                dataList.add(CURRENT_PASSWORD);
                dataList.add(currentPassword);
                dataList.add(KEY);
                dataList.add(key);
                int result = manager.get(GetRequest.createWithTitle("set_profile_data", dataList.toArray(new String[0]))).getInt(RESULT);
                if(result == DATASET_OK)
                    updateData();
                return result;
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return DATASET_ERROR;
        }

        public void logout(){
            key = "";
            name = "";
            email = "";
            id = -1;
            has_skin = false;
            skin_name = "";
        }

        public int setSkin(BufferedImage image){
            try {
                GetRequest request = GetRequest.createWithTitle("set_skin", KEY, key);
                request.put("skin", image);

                return manager.get(request).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
        }

        public int setSkin(String folder, String name){
            try {
                return manager.get(GetRequest.createWithTitle("set_skin", "folder", folder, "name", name, KEY, key)).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
        }
    }

    public static class Social {

        private final NetManager manager;
        public Social(NetManager manager){
            this.manager = manager;
        }

        public GetRequest vkInfo;
        public GetRequest youtubeInfo;

        public String getVkTitle(){
            checkForVKInfo();
            return vkInfo.getString("title");
        }

        public String getVkDescription(){
            checkForVKInfo();
            return vkInfo.getString("description");
        }

        public BufferedImage getVkLogo(){
            checkForVKInfo();
            return vkInfo.getImage("image");
        }

        public String getVkUrl(){
            checkForVKInfo();
            return vkInfo.getString("url");
        }

        public String getYoutubeTitle(){
            checkForYoutubeInfo();
            return youtubeInfo.getString("title");
        }

        public String getYoutubeSubscribers(){
            checkForYoutubeInfo();
            return youtubeInfo.getString("subscribers");
        }

        public BufferedImage getYoutubeLogo(){
            checkForYoutubeInfo();
            return youtubeInfo.getImage("preview");
        }

        public String getYoutubeUrl(){
            checkForYoutubeInfo();
            return youtubeInfo.getString("url");
        }

        private void checkForVKInfo(){
            try {
                if (vkInfo == null)
                    vkInfo = manager.get(GetRequest.createWithTitle("social_get_vk_info"));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        private void checkForYoutubeInfo(){
            try {
                if (youtubeInfo == null)
                    youtubeInfo = manager.get(GetRequest.createWithTitle("social_get_youtube_info"));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public VkPostParameter[] getVKPostParameters(int count){
            try {
                ArrayList<VkPostParameter> out = new ArrayList<>();

                GetRequest request = manager.get(GetRequest.createWithTitle("social_get_vk_list", "count", count + ""));
                for(int i = 0; i < request.getInt("count"); i++){
                    JSONObject object = request.getJSONArray("elements").getJSONObject(i);
                    String type = object.getString("type");
                    String text = object.getString("text");
                    String url = object.getString("url");

                    if(type.equals("default"))
                        out.add(new VkPostParameter(text, url));
                    if(type.equals("picture"))
                        out.add(new VkPostParameter.Picture(text, url, GetRequest.fromBase64(object.getString("image"))));
                    if(type.equals("video"))
                        out.add(new VkPostParameter.Video(text, url, GetRequest.fromBase64(object.getString("image"))));
                    if(type.equals("Snippet"))
                        out.add(new VkPostParameter.Snippet(text, url, GetRequest.fromBase64(object.getString("image")), object.getString("title"), object.getString("author")));
                }

                return out.toArray(new VkPostParameter[0]);
            }catch (Exception ex){
                ex.printStackTrace();
                return new VkPostParameter[0];
            }
        }

        public void getVKPostParametersAsync(int count, Consumer<VkPostParameter[]> consumer){
            new Thread(() -> consumer.accept(getVKPostParameters(count))).start();
        }

        public YoutubeVideoParameters[] getYoutubeVideoParameters(int count){
            try {
                ArrayList<YoutubeVideoParameters> out = new ArrayList<>();

                GetRequest request = manager.get(GetRequest.createWithTitle("social_get_youtube_list", "count", count + ""));
                for(int i = 0; i < request.getInt("count"); i++){
                    JSONObject object = request.getJSONArray("elements").getJSONObject(i);
                    String text = object.getString("title");
                    String url = object.getString("url");
                    String image = object.getString("image");
                    long date = object.getLong("date");

                    out.add(new YoutubeVideoParameters(GetRequest.fromBase64(image), text, url, date));

                }

                return out.toArray(new YoutubeVideoParameters[0]);
            }catch (Exception ex){
                ex.printStackTrace();
                return new YoutubeVideoParameters[0];
            }
        }

        public void getYoutubeVideoParametersAsync(int count, Consumer<YoutubeVideoParameters[]> consumer){
            new Thread(() -> consumer.accept(getYoutubeVideoParameters(count))).start();
        }
    }
}
