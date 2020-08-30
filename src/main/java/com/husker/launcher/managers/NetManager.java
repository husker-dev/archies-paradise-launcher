package com.husker.launcher.managers;

import com.alee.laf.label.WebLabel;
import com.husker.launcher.Launcher;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.utils.ConsoleUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

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

    public void updateStatusLabel(WebLabel label){
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

    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort()), launcher.getConfig().Net.Auth.getTimeout());

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public GetParameters getString(GetParameters parameters) throws IOException {
        connect();

        String text = parameters.toString();

        ConsoleUtils.printDebug(getClass(), "Send: " + text.replace("\n", ""));
        out.write(text + "\n");
        out.flush();
        String inLine = in.readLine();

        socket.close();
        in.close();
        out.close();

        ConsoleUtils.printDebug(getClass(), "Received: " + inLine);
        return GetParameters.create(inLine);
    }

    public BufferedImage getImage(GetParameters parameters) throws IOException {
        connect();

        String text = parameters.toString();

        ConsoleUtils.printDebug(getClass(), "Send: " + text.replace("\n", ""));
        out.write(text + "\n");
        out.flush();

        ConsoleUtils.printDebug(getClass(), "Receiving image...");
        byte[] sizeAr = new byte[4];
        socket.getInputStream().read(sizeAr);
        int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

        byte[] imageAr = new byte[size];
        socket.getInputStream().read(imageAr);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));

        ConsoleUtils.printResult("OK");

        socket.close();
        in.close();
        out.close();

        return image;
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
                return Integer.parseInt(manager.getString(new GetParameters("is_login_taken", LOGIN, name)).get(RESULT));
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int register(String login, String password){
            try {
                return Integer.parseInt(manager.getString(new GetParameters("register", LOGIN, login, PASSWORD, password)).get(RESULT));
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
                return Integer.parseInt(manager.getString(new GetParameters("send_email_code", LOGIN, login, PASSWORD, password, EMAIL, email, ENCRYPTED, encrypted ? "1" : "0")).get(RESULT));
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int confirmMail(String login, String password, String email, String code, boolean encrypted){
            try {
                return Integer.parseInt(manager.getString(new GetParameters("confirm_mail", LOGIN, login, PASSWORD, password, EMAIL, email, EMAIL_CODE, code, ENCRYPTED, encrypted ? "1" : "0")).get(RESULT));
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
                String result = manager.getString(new GetParameters("get_key", LOGIN, login, PASSWORD, password, ENCRYPTED, encrypted ? "1" : "0")).get(KEY);
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

        private String name = "";
        private String email = "";
        private String status = "";
        private long id = -1;
        private boolean has_skin = false;
        private String skin_name = "";
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
            GetParameters parameters = manager.getString(new GetParameters("get_profile_data", "get", String.join(",", new String[]{LOGIN, EMAIL, SKIN_NAME, HAS_SKIN, ID, STATUS}), KEY, key));
            name = parameters.get(LOGIN);
            email = parameters.get(EMAIL);
            id = Long.parseLong(parameters.get(ID));
            has_skin = parameters.get(HAS_SKIN).equals("1");
            skin_name = parameters.get(SKIN_NAME).equals("null") ? null : parameters.get(SKIN_NAME);
            status = parameters.get(STATUS);

            encryptedPassword = manager.getString(new GetParameters("get_encrypted_password", KEY, key)).get(PASSWORD);

            emailConfirmed = manager.getString(new GetParameters("is_email_confirmed", KEY, key)).get(RESULT).equals("1");

            if(has_skin)
                skin = manager.getImage(new GetParameters("skin", KEY, key));
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
                int result = Integer.parseInt(manager.getString(new GetParameters("set_profile_data", dataList.toArray(new String[0]))).get(RESULT));
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


    }

    private static class GetParameters extends HashMap<String, String>{
        private final String title;

        public GetParameters(String title, String... parameters){
            this.title = title;

            if(parameters.length % 2 == 1)
                throw new RuntimeException("Bad parameters");

            for(int i = 0; i < parameters.length; i += 2)
                put(parameters[i], parameters[i + 1]);
        }

        public String toString(){
            StringBuilder values = new StringBuilder();

            int index = 0;
            for(Map.Entry<String, String> entry : entrySet()) {
                values.append(entry.getKey()).append("=").append("\"").append(entry.getValue()).append("\"");

                if(index != size() - 1)
                    values.append(";");
                index ++;
            }

            return title + "{" + values + "}";
        }

        public String getTitle(){
            return title;
        }

        public static GetParameters create(String text){
            String title = text.split("\\{")[0];
            ArrayList<String> parameters = new ArrayList<>();

            for(String par : text.split("\\{")[1].split("}")[0].split(";")) {
                parameters.add(par.split("=")[0]);
                parameters.add(par.split("\"")[1].split("\"")[0]);
            }

            return new GetParameters(title, parameters.toArray(new String[0]));
        }
    }
}
