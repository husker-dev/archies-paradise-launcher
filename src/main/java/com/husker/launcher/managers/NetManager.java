package com.husker.launcher.managers;

import com.husker.glassui.components.social.vk.VkPostInfo;
import com.husker.glassui.components.social.youtube.YoutubeVideoInfo;
import com.husker.launcher.Launcher;
import com.husker.launcher.managers.net.UrlBuilder;
import com.husker.launcher.managers.net.http.Get;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.MinecraftStarter;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
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
    public static final String ACCESS_TOKEN = "access_token";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EMAIL_CODE = "email_code";
    public static final String ENCRYPTED = "encrypted";
    public static final String STATUS = "status";
    public static final String SKIN_URL = "skin_url";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String HAS_SKIN = "has_skin";
    public static final String ID = "id";

    public static final int DATASET_EMAIL_FORMAT = 8;
    public static final int DATASET_PASSWORD_FORMAT = 7;
    public static final int DATASET_NAME_TAKEN = 6;
    public static final int DATASET_NAME_FORMAT = 5;
    public static final int DATASET_INCORRECT_EMAIL_CODE = 4;
    public static final int DATASET_INCORRECT_PASSWORD = 3;
    public static final int DATASET_EMAIL_REQUIRED = 2;
    public static final int DATASET_PASSWORD_REQUIRED = 1;

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



    public Players Players = new Players(this);
    public Auth Auth = new Auth(this);
    public ProfileInfo PlayerInfo = new ProfileInfo(this);
    public Skins Skins = new Skins(this);
    public Social Social = new Social(this);
    public Client Client = new Client(this);


    public Socket connect() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(launcher.getConfig().Net.Auth.getIp(), launcher.getConfig().Net.Auth.getPort()), launcher.getConfig().Net.Auth.getTimeout());
        return socket;
    }

    public void disconnect(Socket socket){
        try {
            socket.close();
        }catch (Exception ignored){}
    }

    public void sendText(Socket socket, String text) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.write(text + "\n");
        writer.flush();
    }

    public String receiveText(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return reader.readLine();
    }

    public GetRequest get(GetRequest parameters) throws IOException {
        Socket socket = connect();
        parameters.send(socket);

        GetRequest received = GetRequest.create(socket);
        disconnect(socket);

        return received;
    }

    public static class Skins {

        private final NetManager manager;
        public Skins(NetManager manager){
            this.manager = manager;
        }

        public String[] getCategories() throws IOException {
            return manager.get(GetRequest.createWithTitle("skins.getCategories")).getString("categories").split(",");
        }

        public String[] getCategorySkins(String category) throws IOException {
            return manager.get(GetRequest.createWithTitle("skins.getCategorySkins", "category", category)).getString("skins").split(",");
        }

        public BufferedImage getCategorySkin(String category, String name) throws IOException {
            return manager.get(GetRequest.createWithTitle("skins.getSkin", "category", category, "name", name)).getImage("skin");
        }

        public BufferedImage getCategoryPreview(String category) throws IOException {
            return manager.get(GetRequest.createWithTitle("skins.getCategoryPreview", "category", category)).getImage("skin");
        }
    }

    public static class Players {

        private final NetManager manager;
        public Players(NetManager manager){
            this.manager = manager;
        }

        public final int ERROR = -1;
        public final int NAME_TAKEN = 1;

        public final int SUCCESSFUL_REGISTRATION = 0;
        public final int BAD_PASSWORD = 2;


        public int register(String login, String password){
            try {
                return manager.get(GetRequest.createWithTitle("auth.create", LOGIN, login, PASSWORD, password)).getInt(RESULT);
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

        public int auth(String login, String password){
            try {
                GetRequest request = manager.get(GetRequest.createWithTitle("auth.getAccessToken", LOGIN, login, PASSWORD, password));
                if(request.getString(RESULT).equals("-1"))
                    return WRONG_DATA;
                else {
                    manager.PlayerInfo.applyKey(request.getString(ACCESS_TOKEN));
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
        private String skin_url = null;
        private BufferedImage skin;
        private boolean emailConfirmed = false;


        private final NetManager manager;
        public ProfileInfo(NetManager manager){
            this.manager = manager;
        }

        void applyKey(String key) throws IOException{
            this.key = key;

            updateData();
        }

        public void updateData() throws IOException{
            GetRequest parameters = manager.get(GetRequest.createWithTitle("profile.getData", "fields", String.join(",", new String[]{LOGIN, EMAIL, SKIN_URL, HAS_SKIN, ID, STATUS}), ACCESS_TOKEN, key));

            JSONObject data = parameters.getJSONObject("data");
            name = data.getString(LOGIN);
            email = data.getString(EMAIL);
            id = data.getLong(ID);
            has_skin = data.getString(HAS_SKIN).equals("1");
            skin_url = data.getString(SKIN_URL).equals("null") ? null : data.getString(SKIN_URL);
            status = data.getString(STATUS);

            emailConfirmed = manager.get(GetRequest.createWithTitle("profile.isEmailConfirmed", ACCESS_TOKEN, key)).getInt(RESULT) == 1;

            if(has_skin)
                skin = manager.get(GetRequest.createWithTitle("profile.getSkin", ACCESS_TOKEN, key)).getImage("skin");
            else
                skin = manager.launcher.Resources.Skin_Steve;

            manager.launcher.getUserConfig().setLogin(name);
        }

        public int sendConfirmCode(String email){
            try {
                return manager.get(GetRequest.createWithTitle("profile.sendEmailCode", ACCESS_TOKEN, key, EMAIL, email)).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int confirmMail(String email, String code){
            try {
                return manager.get(GetRequest.createWithTitle("profile.confirmEmail", ACCESS_TOKEN, key, EMAIL, email, EMAIL_CODE, code)).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
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
            return skin_url;
        }

        public String getStatus() {
            return status;
        }

        public boolean isEmailConfirmed() {
            return emailConfirmed;
        }

        public int setData(String currentPassword, String emailCode, String... data){
            try {
                GetRequest request = GetRequest.createWithTitle("profile.setData");

                JSONObject fields = new JSONObject();
                for(int i = 0; i < data.length; i += 2)
                    fields.put(data[i], data[i + 1]);
                request.put("fields", fields);

                JSONObject confirms = new JSONObject();
                if(currentPassword != null)
                    confirms.put(PASSWORD, currentPassword);
                if(emailCode != null)
                    confirms.put(EMAIL_CODE, emailCode);
                request.put("confirms", confirms);

                request.put(ACCESS_TOKEN, key);
                int result = manager.get(request).getInt(RESULT);
                if(result == 0)
                    updateData();
                return result;
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int applyIP(){
            try {
                GetRequest request = GetRequest.createWithTitle("profile.bindIp", ACCESS_TOKEN, key);
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
            skin_url = "";
        }

        public int setSkin(BufferedImage image){
            try {
                GetRequest request = GetRequest.createWithTitle("profile.setSkin", ACCESS_TOKEN, key);
                request.put("skin", image);

                return manager.get(request).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
        }

        public int setSkin(String category, String name){
            try {
                return manager.get(GetRequest.createWithTitle("profile.setSkin", "category", category, "name", name, ACCESS_TOKEN, key)).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
        }
    }

    public static class Social {

        private final NetManager manager;
        public Social(NetManager manager){
            this.manager = manager;
            checkForVK();
            checkForYouTube();
        }

        private final ArrayList<YoutubeVideoInfo> videos = new ArrayList<>();
        private final ArrayList<VkPostInfo> posts = new ArrayList<>();

        private String vk_title;
        private String vk_description;
        private BufferedImage vk_logo;
        private String vk_url;

        private String yt_title;
        private String yt_subscribers;
        private BufferedImage yt_logo;
        private String yt_url;

        public String getVkTitle(){
            checkForVK();
            return vk_title;
        }

        public String getVkDescription(){
            checkForVK();
            return vk_description;
        }

        public BufferedImage getVkLogo(){
            checkForVK();
            return vk_logo;
        }

        public String getVkUrl(){
            checkForVK();
            return vk_url;
        }

        public String getYoutubeTitle(){
            checkForYouTube();
            return yt_title;
        }

        public String getYoutubeSubscribers(){
            checkForYouTube();
            return yt_subscribers;
        }

        public BufferedImage getYoutubeLogo(){
            checkForYouTube();
            return yt_logo;
        }

        public String getYoutubeUrl(){
            checkForYouTube();
            return yt_url;
        }

        public VkPostInfo getPost(int index){
            checkForVK();
            return posts.get(index);
        }

        public YoutubeVideoInfo getYoutubeVideo(int index){
            checkForYouTube();
            return videos.get(index);
        }

        private void checkForYouTube(){
            try {
                if(videos.size() > 0)
                    return;

                GetRequest request = manager.get(GetRequest.createWithTitle("youtube.getInfo"));

                Get get = new Get(new UrlBuilder("youtube.com/channel/" + request.getString("id")));
                get.execute();

                JSONObject content = new JSONObject(get.getHtmlContent().split("window\\[\"ytInitialData\"] =")[1].split("</script>")[0]);

                yt_title = content.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getString("title");
                yt_url = "https://youtube.com/channel/" + request.getString("id");
                yt_logo = ImageIO.read(new URL(content.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getJSONObject("avatar").getJSONArray("thumbnails").getJSONObject(0).getString("url")));
                yt_subscribers = content.getJSONObject("header").getJSONObject("c4TabbedHeaderRenderer").getJSONObject("subscriberCountText").getString("simpleText");

                JSONArray json_videos = content.getJSONObject("contents").getJSONObject("twoColumnBrowseResultsRenderer")
                        .getJSONArray("tabs").getJSONObject(0).getJSONObject("tabRenderer")
                        .getJSONObject("content").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(1)
                        .getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0)
                        .getJSONObject("shelfRenderer").getJSONObject("content").getJSONObject("horizontalListRenderer").getJSONArray("items");

                for(int i = 0; i < Math.min(4, json_videos.length()); i++){
                    BufferedImage preview = ImageIO.read(new URL(json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(3).getString("url")));
                    String title = json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getJSONObject("title").getString("simpleText");
                    String url = "https://www.youtube.com/watch?v=" + json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getString("videoId");
                    videos.add(new YoutubeVideoInfo(i, title, preview, url));
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        private void checkForVK(){
            try {
                if(posts.size() > 0)
                    return;

                GetRequest request = manager.get(GetRequest.createWithTitle("vk.getInfo"));

                Get get = new Get(new UrlBuilder("vk.com/" + request.getString("id")));
                get.execute();

                String content = get.getHtmlContent();

                vk_logo = ImageIO.read(new URL(content.split("class=\"basisGroup__mainInfoRow\"")[1].split("src=\"")[1].split("\"")[0]));
                vk_title = content.split("class=\"basisGroup__groupTitle op_header\">")[1].split("</h2>")[0].trim();
                if(content.contains("class=\"pp_status\">"))
                    vk_description = content.split("class=\"pp_status\">")[1].split("</div>")[0];
                else
                    vk_description = "";
                vk_url = "https://vk.com/" + request.getString("id");

                String[] content_items = content.split("container=\"group_wall\">")[1].split("<div class=\"wall_item\">");
                for(String item : content_items){

                    // Если закреплённая
                    if(item.contains("wi_explain") || !item.contains("wi_info"))
                        continue;

                    String url = "https://vk.com" + item.split("wi_info")[1].split("href=\"")[1].split("\"")[0];

                    String text = item.contains("pi_text") ? item.split("pi_text\">")[1].split("</div>")[0] : null;
                    String photo = item.contains("thumb_map thumb_map_wide thumb_map_l al_photo") ? item.split("background-image: url\\(")[1].split("\\)")[0] : null;
                    String videoPreview = item.contains("thumb_map thumb_map_wide thumb_map_l") ? item.split("background-image: url\\(")[1].split("\\)")[0] : null;
                    String snippetPreview = item.contains("articleSnippet") ? item.split("background-image: url\\(")[1].split("\\)")[0] : null;
                    String snippetTitle = item.contains("articleSnippet") ? item.split("articleSnippet_title\">")[1].split("</div>")[0] : null;
                    String snippetAuthor = item.contains("articleSnippet") ? item.split("articleSnippet_author\">")[1].split("<span>")[0] : null;

                    // Snippet
                    if(snippetPreview != null){
                        posts.add(new VkPostInfo.Snippet(posts.size(), text, ImageIO.read(new URL(snippetPreview)), url, snippetTitle, snippetAuthor));
                        continue;
                    }
                    // Picture
                    if(photo != null){
                        posts.add(new VkPostInfo.Picture(posts.size(), text, ImageIO.read(new URL(photo)), url));
                        continue;
                    }
                    // Video
                    if(videoPreview != null){
                        posts.add(new VkPostInfo.Video(posts.size(), text, ImageIO.read(new URL(videoPreview)), url));
                        continue;
                    }
                    posts.add(new VkPostInfo(posts.size(), text, null, url));
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
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
                return manager.get(GetRequest.createWithTitle("client.getInfo")).getString("build");
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
                return manager.get(GetRequest.createWithTitle("client.getInfo")).getString("build_id");
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return "-1";
        }

        public String getJarVersion(){
            try {
                return manager.get(GetRequest.createWithTitle("client.getInfo")).getString("version");
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
                GetRequest request = manager.get(GetRequest.createWithTitle("client.getModsInfo", "index", index + "", "icon", hasIcons + ""));
                return new ModInfo(request.getJSONArray("mods").getJSONObject(0));
            }catch (Exception ex){
            }
            return null;
        }

        public void playOrDownload(Consumer<DownloadingProcessArguments> process){
            new Thread(() -> {
                int state = hasUpdate();
                ConsoleUtils.printDebug(getClass(), "Client state: " + state);
                if(state == ERROR)
                    return;
                if(state == UPDATE || state == DOWNLOAD){
                    try {
                        process.accept(new DownloadingProcessArguments(0));
                        if(Files.exists(Paths.get("client")))
                            IOUtils.delete("client", percent -> process.accept(new DownloadingProcessArguments(0, percent)));
                        Files.createDirectories(Paths.get("client"));

                        // Downloading
                        JSONObject downloadInfo = manager.get(GetRequest.createWithTitle("client.getFilesInfo"));
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

                        while(true) {
                            String md5_mods = getModsMD5();
                            String md5_client = getClientMD5();

                            GetRequest checksumResult = manager.get(GetRequest.createWithTitle("client.checksum", "mods", md5_mods, "client", md5_client));
                            if (checksumResult.getString("result").equals("0") && (checksumResult.has("dif_mods") || checksumResult.has("dif_versions"))) {
                                JOptionPane.showMessageDialog(null, "Файлы игры отличаются от файлов на сервере!", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);
                                boolean mods = checksumResult.has("dif_mods") && checksumResult.getBoolean("dif_mods");
                                boolean versions = checksumResult.has("dif_client") && checksumResult.getBoolean("dif_client");

                                JSONObject downloadInfo = manager.get(GetRequest.createWithTitle("get_download_info"));
                                JSONObject folderInfo = downloadInfo.getJSONObject("info").getJSONObject("folders");
                                long folderVersions = folderInfo.getLong("versions");
                                long folderMods = folderInfo.getLong("mods");

                                if (mods) {
                                    if (Files.exists(Paths.get("client/mods")))
                                        IOUtils.delete("client/mods", percent -> process.accept(new DownloadingProcessArguments(0, percent)));

                                    receiveClientPart("mods", args -> process.accept(new DownloadingProcessArguments(1) {{
                                        setCurrentSize(args.getCurrentSize());
                                        setFullSize(args.getSize());
                                        setSpeed(args.getSpeed());
                                    }}));

                                    simpleUnzip("client/mods.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                                        setCurrentSize(args.getCurrentSize());
                                        setFullSize(folderMods);
                                    }}));
                                }

                                if (versions) {
                                    if (Files.exists(Paths.get("client/versions")))
                                        IOUtils.delete("client/versions", percent -> process.accept(new DownloadingProcessArguments(0, percent)));

                                    receiveClientPart("versions", args -> process.accept(new DownloadingProcessArguments(1) {{
                                        setCurrentSize(args.getCurrentSize());
                                        setFullSize(args.getSize());
                                        setSpeed(args.getSpeed());
                                    }}));

                                    simpleUnzip("client/versions.zip", args -> process.accept(new DownloadingProcessArguments(2) {{
                                        setCurrentSize(args.getCurrentSize());
                                        setFullSize(folderVersions);
                                    }}));
                                }
                            }else
                                break;
                        }


                        if(manager.PlayerInfo.applyIP() == -1)
                            JOptionPane.showMessageDialog(null, "В данный момент вход на сервер недоступен, но вы можете играть в одиночной игре", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);

                        MinecraftStarter starter = new MinecraftStarter("client"){{
                            addServer(manager.launcher.getConfig().Net.Minecraft.getIp());
                            setNickname(manager.PlayerInfo.getNickname());
                            setFullscreen(!manager.launcher.getSettings().isWindowed());
                            setRam(manager.launcher.getSettings().getRAM());
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

        public String getModsMD5(){
            try{
                Vector<FileInputStream> streams = new Vector<>();
                for(File file : new File("client/mods").listFiles(file -> file.getName().endsWith(".jar"))){
                    try {
                        streams.add(new FileInputStream(file));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

                return DigestUtils.md5Hex(new SequenceInputStream(streams.elements()));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getClientMD5(){
            try{
                File clientFolder = new File("client/versions").listFiles(File::isDirectory)[0];
                File clientFile = clientFolder.listFiles(file -> file.getName().endsWith(".jar"))[0];

                return DigestUtils.md5Hex(new FileInputStream(clientFile));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private void simpleUnzip(String path, Consumer<IOUtils.ZipArguments> process) throws IOException {
            IOUtils.unzip(path, "client/", process);
            IOUtils.delete(path);
        }

        private void receiveClientPart(String name, Consumer<IOUtils.FileReceivingArguments> listener) throws IOException {
            ConsoleUtils.printDebug(getClass(), "Receiving \"" + name + "\"...");
            Socket socket = manager.connect();
            manager.sendText(socket, "{\"method\":\"client.get\",\"name\":\"" + name + "\"}");
            //manager.get(GetRequest.createWithTitle("client.get", "name", name));
            IOUtils.receiveFile(socket, "client/" + name + ".zip", listener);
            manager.disconnect(socket);
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
            socket.connect(host, 10000);
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
