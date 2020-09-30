package com.husker.launcher.managers;

import com.husker.glassui.components.social.vk.VkPostParameter;
import com.husker.glassui.components.social.youtube.YoutubeVideoParameters;
import com.husker.launcher.Launcher;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.MinecraftStarter;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public static final String UUID = "uuid";

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
    public Client Client = new Client(this);

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

    public void sendText(String text) throws IOException {
        out.write(text + "\n");
        out.flush();
    }

    public String receiveText() throws IOException {
        return in.readLine();
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
        private String uuid = null;
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
            GetRequest parameters = manager.get(GetRequest.createWithTitle("get_profile_data", "get", String.join(",", new String[]{LOGIN, EMAIL, SKIN_NAME, HAS_SKIN, ID, STATUS, UUID}), KEY, key));
            name = parameters.getString(LOGIN);
            email = parameters.getString(EMAIL);
            id = parameters.getLong(ID);
            has_skin = parameters.getString(HAS_SKIN).equals("1");
            skin_name = parameters.getString(SKIN_NAME).equals("null") ? null : parameters.getString(SKIN_NAME);
            status = parameters.getString(STATUS);
            uuid = parameters.getString(UUID);

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

        public String getUUID(){
            return uuid;
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

        public int applyIP(){
            try {
                GetRequest request = GetRequest.createWithTitle("set_ip", KEY, key);
                return manager.get(request).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
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

    public static class Client{

        public final int ERROR = -1;
        public final int PLAY = 0;
        public final int UPDATE = 1;
        public final int DOWNLOAD = 2;

        private final NetManager manager;
        public Client(NetManager manager){
            this.manager = manager;
        }

        public String getClientVersion(){
            try {
                return manager.get(GetRequest.createWithTitle("client_get_version")).getString("version");
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return "-1";
        }

        public String getCurrentClientVersion(){
            try {
                if(Files.exists(Paths.get("client/client_info.json"))){
                    JSONObject object = new JSONObject(IOUtils.readFileText("client/client_info.json"));
                    return object.getString("build");
                }
            }catch (Exception ex){
            }
            return "-1";
        }

        public String getShortClientVersion(){
            try {
                return manager.get(GetRequest.createWithTitle("client_get_short_version")).getString("version");
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return "-1";
        }

        public String getJarVersion(){
            try {
                return manager.get(GetRequest.createWithTitle("client_get_minecraft_version")).getString("version");
            }catch (Exception ex){
                return "Unknown";
            }
        }

        public int hasUpdate(){
            String currentVersion = getCurrentClientVersion();
            String version = getClientVersion();

            if(version.equals("-1"))
                return ERROR;
            if(currentVersion.equals(version)){
                return PLAY;
            }else{
                if(currentVersion.equals("-1"))
                    return DOWNLOAD;
                else
                    return UPDATE;
            }
        }

        public ModInfo getModInfo(int index, boolean hasIcons){
            try {
                GetRequest request = manager.get(GetRequest.createWithTitle("client_get_mod_info", "index", index + "", "icon", hasIcons + ""));
                return new ModInfo(request.getJSONArray("mods").getJSONObject(0));
            }catch (Exception ex){
            }
            return null;
        }

        public void playOrDownload(Consumer<DownloadingProcessArguments> process){
            new Thread(() -> {
                int state = hasUpdate();
                if(state == ERROR)
                    return;
                if(state == UPDATE || state == DOWNLOAD){
                    try {
                        process.accept(new DownloadingProcessArguments(0));
                        if(Files.exists(Paths.get("client")))
                            IOUtils.delete("client", percent -> process.accept(new DownloadingProcessArguments(0, percent)));
                        Files.createDirectories(Paths.get("client"));

                        // Downloading
                        JSONObject downloadInfo = manager.get(GetRequest.createWithTitle("get_download_info"));
                        JSONObject zipInfo = downloadInfo.getJSONObject("info").getJSONObject("zip");
                        long zipVersions = zipInfo.getLong("versions");
                        long zipMods = zipInfo.getLong("mods");
                        long zipOther = zipInfo.getLong("other");
                        long zipFullSize = zipVersions + zipMods + zipOther;

                        process.accept(new DownloadingProcessArguments(1));
                        receiveClientPart("versions", args -> process.accept(new DownloadingProcessArguments(1){{
                            setCurrentSize(args.getCurrentSize());
                            setFullSize(zipFullSize);
                            setSpeed(args.getSpeed());
                        }}));
                        receiveClientPart("other", args -> process.accept(new DownloadingProcessArguments(1){{
                            setCurrentSize(zipVersions + args.getCurrentSize());
                            setFullSize(zipFullSize);
                            setSpeed(args.getSpeed());
                        }}));
                        receiveClientPart("mods", args -> process.accept(new DownloadingProcessArguments(1){{
                            setCurrentSize(zipOther + zipVersions + args.getCurrentSize());
                            setFullSize(zipFullSize);
                            setSpeed(args.getSpeed());
                        }}));

                        // Unzipping
                        JSONObject folderInfo = downloadInfo.getJSONObject("info").getJSONObject("folders");
                        long folderVersions = folderInfo.getLong("versions");
                        long folderMods = folderInfo.getLong("mods");
                        long folderOther = folderInfo.getLong("other");
                        long folderFullSize = folderVersions + folderMods + folderOther;

                        process.accept(new DownloadingProcessArguments(2));
                        simpleUnzip("client/mods.zip", args -> process.accept(new DownloadingProcessArguments(2){{
                            setCurrentSize(args.getCurrentSize());
                            setFullSize(folderFullSize);
                        }}));
                        simpleUnzip("client/versions.zip", args -> process.accept(new DownloadingProcessArguments(2){{
                            setCurrentSize(folderMods + args.getCurrentSize());
                            setFullSize(folderFullSize);
                        }}));
                        IOUtils.unzip("client/other.zip", "client/tmp_", args ->process.accept(new DownloadingProcessArguments(2){{
                            setCurrentSize(folderVersions + folderMods + args.getCurrentSize());
                            setFullSize(folderFullSize);
                        }}));

                        // Moving
                        IOUtils.moveDirectoryContent("client/tmp_/update", "client", percent -> process.accept(new DownloadingProcessArguments(3, percent)));

                        // Removing tmp files
                        IOUtils.delete("client/tmp_", percent -> process.accept(new DownloadingProcessArguments(4, percent / 2)));
                        IOUtils.delete("client/other.zip", percent -> process.accept(new DownloadingProcessArguments(4, 50 + percent / 2)));

                        // Saving client info
                        IOUtils.writeFileText("client/client_info.json", new JSONObject(){{
                            put("version", getJarVersion());
                            put("build", getClientVersion());
                            put("build_id", getShortClientVersion());
                        }}.toString());

                        process.accept(new DownloadingProcessArguments(-1));
                    }catch (Exception ex){
                        ex.printStackTrace();
                        process.accept(new DownloadingProcessArguments(-2));
                    }
                }
                if(state == PLAY){
                    try{
                        process.accept(new DownloadingProcessArguments(5));

                        if(manager.PlayerInfo.applyIP() == -1){
                            process.accept(new DownloadingProcessArguments(-2));
                            return;
                        }
                        MinecraftStarter starter = new MinecraftStarter("client"){{
                            addServer(manager.launcher.getConfig().Net.Minecraft.getIp());
                            setNickname(manager.PlayerInfo.getNickname());
                            setUUID(manager.PlayerInfo.getUUID());
                        }};
                        starter.launch();

                        process.accept(new DownloadingProcessArguments(6));
                        System.gc();
                        starter.joinThread();
                        process.accept(new DownloadingProcessArguments(-1));

                    }catch (Exception ex){
                        ex.printStackTrace();
                        process.accept(new DownloadingProcessArguments(-2));
                    }
                }
            }).start();
        }

        private void simpleUnzip(String path, Consumer<IOUtils.UnzippingArguments> process) throws IOException {
            IOUtils.unzip(path, "client/", process);
            IOUtils.delete(path);
        }

        private void receiveClientPart(String name, Consumer<IOUtils.FileReceivingArguments> listener) throws IOException {
            ConsoleUtils.printDebug(getClass(), "Receiving \"" + name + "\"...");
            manager.connect();
            manager.sendText("get_client:" + name);
            IOUtils.receiveFile(manager.socket, "client/" + name + ".zip", listener);
            manager.disconnect();
        }

        private String echoText(String text) throws IOException {
            manager.connect();
            manager.sendText(text);
            String received = manager.receiveText();
            manager.disconnect();
            return received;
        }

        private static double thirdPart(int index, double percent){
            return 100d / 3d * (double)index + percent / 3d;
        }

        public static class DownloadingProcessArguments {

            private int processId;

            private double current;
            private double full;
            private double speed;

            public DownloadingProcessArguments(int id){
                this(id, 0);
            }

            public DownloadingProcessArguments(int id, double percent){
                this.processId = id;
                full = 100;
                current = percent;
            }

            public int getProcessId() {
                return processId;
            }

            public double getPercent() {
                return current / full * 100d;
            }

            public void setProcessId(int processId) {
                this.processId = processId;
            }

            public void setCurrentSize(double current){
                this.current = current;
            }

            public void setFullSize(double full){
                this.full = full;
            }

            public double getFullSize(){
                return full;
            }

            public double getCurrentSize(){
                return current;
            }

            public void setSpeed(double speed){
                this.speed = speed;
            }

            public double getSpeed(){
                return speed;
            }
        }

        public static class ModInfo{

            private final String name;
            private final String description;
            private final BufferedImage icon;

            public ModInfo(JSONObject jsonObject){
                name = jsonObject.getString("name");
                description = jsonObject.getString("description");
                if(jsonObject.has("icon"))
                    icon = GetRequest.fromBase64(jsonObject.getString("icon"));
                else
                    icon = null;
            }

            public String getName(){
                return name;
            }

            public String getDescription(){
                return description;
            }

            public BufferedImage getIcon(){
                return icon;
            }
        }
    }

    public static class MinecraftServer{
        public static JSONObject info(String ip, int port) throws IOException {
            InetSocketAddress host = new InetSocketAddress(ip, port);
            Socket socket = new Socket();
            socket.connect(host, 3000);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            byte [] handshakeMessage = createHandshakeMessage(ip, port);

            // C->S : Handshake State=1
            // send packet length and packet
            writeVarInt(output, handshakeMessage.length);
            output.write(handshakeMessage);

            // C->S : Request
            output.writeByte(0x01); //size is only 1
            output.writeByte(0x00); //packet id for ping

            // S->C : Response
            int size = readVarInt(input);
            int packetId = readVarInt(input);

            if (packetId == -1)
                throw new IOException("Premature end of stream.");

            if (packetId != 0x00)
                throw new IOException("Invalid packetID");
            int length = readVarInt(input); //length of json string

            if (length == -1)
                throw new IOException("Premature end of stream.");

            if (length == 0)
                throw new IOException("Invalid string length.");

            byte[] in = new byte[length];
            input.readFully(in);  //read json string
            String json = new String(in);

            // C->S : Ping
            long now = System.currentTimeMillis();
            output.writeByte(0x09); //size of packet
            output.writeByte(0x01); //0x01 for ping
            output.writeLong(now); //time!?

            // S->C : Pong
            long pingTime = readVarInt(input);
            packetId = readVarInt(input);
            if (packetId == -1)
                throw new IOException("Premature end of stream.");

            if (packetId != 0x01)
                throw new IOException("Invalid packetID");

            input.readLong(); //read response

            JSONObject object = new JSONObject(json);
            object.put("ping", pingTime);

            return object;
        }

        public static byte [] createHandshakeMessage(String host, int port) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            DataOutputStream handshake = new DataOutputStream(buffer);
            handshake.writeByte(0x00); //packet id for handshake
            writeVarInt(handshake, 4); //protocol version
            writeString(handshake, host, StandardCharsets.UTF_8);
            handshake.writeShort(port); //port
            writeVarInt(handshake, 1); //state (1 for handshake)

            return buffer.toByteArray();
        }

        public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
            byte [] bytes = string.getBytes(charset);
            writeVarInt(out, bytes.length);
            out.write(bytes);
        }

        public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
            while (true) {
                if ((paramInt & 0xFFFFFF80) == 0) {
                    out.writeByte(paramInt);
                    return;
                }

                out.writeByte(paramInt & 0x7F | 0x80);
                paramInt >>>= 7;
            }
        }

        public static int readVarInt(DataInputStream in) throws IOException {
            int i = 0;
            int j = 0;
            while (true) {
                int k = in.readByte();
                i |= (k & 0x7F) << j++ * 7;
                if (j > 5) throw new RuntimeException("VarInt too big");
                if ((k & 0x80) != 128) break;
            }
            return i;
        }
    }
}
