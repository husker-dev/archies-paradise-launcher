package com.husker.launcher.managers;

import com.husker.launcher.Launcher;
import com.husker.launcher.managers.social.InstPhotoInfo;
import com.husker.launcher.managers.social.VkPostInfo;
import com.husker.launcher.managers.social.YoutubeVideoInfo;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.launcher.utils.IOUtils;
import com.husker.launcher.utils.MinecraftStarter;
import com.husker.net.Get;
import com.husker.net.UrlBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Vector;
import java.util.function.Consumer;

public class API {

    public final Launcher launcher;
    public API(Launcher launcher){
        this.launcher = launcher;
    }

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

    public BufferedImage getImage(ApiMethod method) throws IOException {
        return ImageIO.read(new URL(getUrl(method)));
    }

    public BufferedImage getImage(String methodName) throws IOException {
        return getImage(ApiMethod.create(methodName));
    }

    public JSONObject getJSON(ApiMethod method) throws IOException {
        Get get = new Get(getUrl(method));
        get.execute();
        return new JSONObject(get.getHtmlContent());
    }

    public JSONObject getJSON(String methodName) throws IOException {
        return getJSON(ApiMethod.create(methodName));
    }

    public String getUrl(ApiMethod apiMethod){
        String s = "http://" + launcher.getConfig().Net.Auth.getIp() + ":" + launcher.getConfig().Net.Auth.getPort() + "/api/method/" + apiMethod.getUrl();
        System.out.println(s);
        return s;
    }

    public static String[] toStringArray(JSONArray array){
        ArrayList<String> arr = new ArrayList<>();
        for(int i = 0; i < array.length(); i++)
            arr.add(array.getString(i));
        return arr.toArray(new String[0]);
    }

    public static class Skins {

        private final API api;
        public Skins(API api){
            this.api = api;
        }

        public String[] getCategories() throws IOException {
            return toStringArray(api.getJSON("skins.getCategories").getJSONArray("categories"));
        }

        public String[] getCategorySkins(String category) throws IOException {
            return toStringArray(api.getJSON(ApiMethod.create("skins.getCategorySkins").set("category", category)).getJSONArray("skins"));
        }

        public BufferedImage getCategorySkin(String category, String name) throws IOException {
            return api.getImage(ApiMethod.create("skins.getCategorySkin").set("category", category).set("name", name));
        }

        public BufferedImage getCategoryPreview(String category) throws IOException {
            return api.getImage(ApiMethod.create("skins.getCategoryPreview").set("category", category));
        }
    }

    public static class Players {

        private final API api;
        public Players(API api){
            this.api = api;
        }

        public final int ERROR = -1;
        public final int NAME_TAKEN = 1;

        public final int SUCCESSFUL_REGISTRATION = 0;
        public final int BAD_PASSWORD = 2;


        public int register(String login, String password){
            try {
                return api.getJSON(ApiMethod.create("auth.create").set(LOGIN, login).set(PASSWORD, password)).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return ERROR;
        }
    }

    public static class Auth {

        public final int OK = 0;
        public final int WRONG_DATA = 1;
        public final int CONNECTION_ERROR = -1;

        private final API api;
        public Auth(API api){
            this.api = api;
        }

        public int auth(String login, String password){
            try {
                JSONObject request = api.getJSON(ApiMethod.create("auth.getAccessToken").set(LOGIN, login).set(PASSWORD, password));
                if(request.has(ACCESS_TOKEN)){
                    api.PlayerInfo.applyKey(request.getString(ACCESS_TOKEN));
                    return OK;
                }else
                    return WRONG_DATA;
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return CONNECTION_ERROR;
        }
    }


    public static class ProfileInfo {

        private String token = "";

        private String name = "Имя";
        private String email = "Почта";
        private String status = "Статус";
        private long id = -1;
        private boolean has_skin = false;
        private String skin_url = null;
        private BufferedImage skin;
        private boolean emailConfirmed = false;

        private final API api;
        public ProfileInfo(API api){
            this.api = api;
        }

        void applyKey(String key) throws IOException{
            this.token = key;

            updateData();
        }

        public void updateData() throws IOException{
            JSONObject parameters = api.getJSON(ProfileApiMethod.create("profile.getData", token).set("fields", String.join(",", new String[]{LOGIN, EMAIL, SKIN_URL, HAS_SKIN, ID, STATUS})));

            JSONObject data = parameters.getJSONObject("data");
            name = data.getString(LOGIN);
            email = data.getString(EMAIL);
            id = data.getLong(ID);
            has_skin = data.getString(HAS_SKIN).equals("1");
            skin_url = data.getString(SKIN_URL).equals("null") ? null : data.getString(SKIN_URL);
            status = data.getString(STATUS);

            emailConfirmed = api.getJSON(ProfileApiMethod.create("profile.isEmailConfirmed", token)).getBoolean("confirmed");

            if(has_skin)
                skin = api.getImage(ProfileApiMethod.create("profile.getSkin", token));
            else
                skin = api.launcher.Resources.Skin_Steve;

            api.launcher.getUserConfig().setLogin(name);
        }

        public int sendConfirmCode(String email){
            try {
                return api.getJSON(ProfileApiMethod.create("profile.sendEmailCode", token).set(EMAIL, email)).getInt(RESULT);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return -1;
        }

        public int confirmMail(String email, String code){
            try {
                return api.getJSON(ProfileApiMethod.create("profile.confirmEmail", token).set(EMAIL, email).set(EMAIL_CODE, code)).getBoolean("confirmed") ? 1 : 0;
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

        public String getToken(){
            return token;
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
                ApiMethod request = ProfileApiMethod.create("profile.setData", token);

                for(int i = 0; i < data.length; i += 2)
                    request.set(data[i], data[i + 1]);

                if(currentPassword != null)
                    request.set(CURRENT_PASSWORD, currentPassword);
                if(emailCode != null)
                    request.set(EMAIL_CODE, emailCode);

                int result = api.getJSON(request).getInt(RESULT);
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
                return api.getJSON(ProfileApiMethod.create("profile.bindIp", token)).getBoolean("bound") ? 1 : 0;
            }catch (Exception ex){
                return -1;
            }
        }

        public void logout(){
            token = "";
            name = "";
            email = "";
            id = -1;
            has_skin = false;
            skin_url = "";
        }

        public int setSkin(BufferedImage image){
            try {
                return api.getJSON(ProfileApiMethod.create("profile.setSkin", token).set("skin", toBase64(image))).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
        }

        public int setSkin(String category, String name){
            try {
                return api.getJSON(ProfileApiMethod.create("profile.setSkin", token).set("category", category).set("name", name)).getInt(RESULT);
            }catch (Exception ex){
                return -1;
            }
        }
    }

    public static class Social {

        private final API api;
        public Social(API api){
            this.api = api;
            checkForVK();
            checkForYouTube();
        }

        private final ArrayList<YoutubeVideoInfo> videos = new ArrayList<>();
        private final ArrayList<VkPostInfo> posts = new ArrayList<>();
        private final ArrayList<InstPhotoInfo> photos = new ArrayList<>();

        private String vk_title;
        private String vk_description;
        private BufferedImage vk_logo;
        private String vk_url;

        private String yt_title;
        private String yt_subscribers;
        private BufferedImage yt_logo;
        private String yt_url;

        public int setInstagramId(String id){
            try {
                return api.getJSON(ProfileApiMethod.create("instagram.getInfo", api.PlayerInfo.token).set("id", id)).getBoolean("changed") ? 1 : 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public int setYouTubeId(String id){
            try {
                return api.getJSON(ProfileApiMethod.create("youtube.getInfo", api.PlayerInfo.token).set("id", id)).getBoolean("changed") ? 1 : 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public int setVkId(String id){
            try {
                return api.getJSON(ProfileApiMethod.create("vk.setInfo", api.PlayerInfo.token).set("id", id)).getBoolean("changed") ? 1 : 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public int setGitHubRepo(String repo){
            try {
                return api.getJSON(ProfileApiMethod.create("github.setInfo", api.PlayerInfo.token).set("repo", repo)).getBoolean("changed") ? 1 : 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public String getInstagramId(){
            try {
                return api.getJSON(ApiMethod.create("instagram.getInfo")).getString("id");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getYouTubeId(){
            try {
                return api.getJSON(ApiMethod.create("youtube.getInfo")).getString("id");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getVkId(){
            try {
                return api.getJSON(ApiMethod.create("vk.getInfo")).getString("id");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getGitHubRepo(){
            try {
                return api.getJSON(ApiMethod.create("github.getInfo")).getString("id");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

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

        public InstPhotoInfo getInstPhoto(int index){
            checkForInst();
            return photos.get(index);
        }

        private void checkForInst(){
            try {
                if(photos.size() > 0)
                    return;

                String id = getInstagramId();

                Get inst = new Get(new UrlBuilder("instagram.com/" + id));
                inst.execute();
                String content = inst.getHtmlContent();
                JSONObject object = new JSONObject(content.split("window._sharedData =")[1].split("</script>")[0]);

                JSONArray images = object.getJSONObject("entry_data").getJSONArray("ProfilePage")
                        .getJSONObject(0).getJSONObject("graphql").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges");

                for(int i = 0; i < 4; i++){
                    JSONObject image = images.getJSONObject(i).getJSONObject("node");

                    String url = "https://instagram.com/p/" + image.getString("shortcode");
                    String text = image.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
                    String previewUrl = image.getString("display_url");

                    photos.add(new InstPhotoInfo(i, text, previewUrl, url));
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        private void checkForYouTube(){
            for(int r = 0; r < 5; r++) {
                try {
                    if (videos.size() > 0)
                        return;

                    String id = getYouTubeId();

                    Get get = new Get(new UrlBuilder("youtube.com/channel/" + id));
                    get.execute();

                    JSONObject content = new JSONObject(get.getHtmlContent().split("window\\[\"ytInitialData\"] =")[1].split("</script>")[0]);

                    yt_title = content.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getString("title");
                    yt_url = "https://youtube.com/channel/" + id;
                    yt_logo = ImageIO.read(new URL(content.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getJSONObject("avatar").getJSONArray("thumbnails").getJSONObject(0).getString("url")));
                    yt_subscribers = content.getJSONObject("header").getJSONObject("c4TabbedHeaderRenderer").getJSONObject("subscriberCountText").getString("simpleText");

                    JSONArray json_videos = content.getJSONObject("contents").getJSONObject("twoColumnBrowseResultsRenderer")
                            .getJSONArray("tabs").getJSONObject(0).getJSONObject("tabRenderer")
                            .getJSONObject("content").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(1)
                            .getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0)
                            .getJSONObject("shelfRenderer").getJSONObject("content").getJSONObject("horizontalListRenderer").getJSONArray("items");

                    for (int i = 0; i < Math.min(4, json_videos.length()); i++) {
                        String previewUrl = json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(3).getString("url");
                        String title = json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getJSONObject("title").getString("simpleText");
                        String url = "https://www.youtube.com/watch?v=" + json_videos.getJSONObject(i).getJSONObject("gridVideoRenderer").getString("videoId");
                        videos.add(new YoutubeVideoInfo(i, title, previewUrl, url));
                    }
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void checkForVK(){
            try {
                if(posts.size() > 0)
                    return;

                String id = getVkId();

                Get get = new Get(new UrlBuilder("vk.com/" + id));
                get.execute();

                String content = get.getHtmlContent();

                vk_logo = ImageIO.read(new URL(content.split("class=\"basisGroup__mainInfoRow\"")[1].split("src=\"")[1].split("\"")[0]));
                vk_title = content.split("class=\"basisGroup__groupTitle op_header\">")[1].split("</h2>")[0].trim();
                if(content.contains("class=\"pp_status\">"))
                    vk_description = content.split("class=\"pp_status\">")[1].split("</div>")[0];
                else
                    vk_description = "";
                vk_url = "https://vk.com/" + id;

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

                    if(text != null){
                        text = text.replace("<br/><br/>", "\n");
                        text = text.replace("<br/>", "\n");
                        text = text.replace("</a>", "");
                        text = text.replace("<span>", "").replace("</span>", "");
                        text = text.replace("&#33;", "");
                        text = text.replace("&quot;", "\"");
                        text = text.trim();

                        while(text.contains("<a"))
                            text = text.replace("<a" + text.split("<a")[1].split(">")[0] + ">", "");
                        while(text.contains("<img"))
                            text = text.replace("<img" + text.split("<img")[1].split(">")[0] + ">", "");
                    }

                    // Snippet
                    if(snippetPreview != null){
                        posts.add(new VkPostInfo.Snippet(posts.size(), text, snippetPreview, url, snippetTitle, snippetAuthor));
                        continue;
                    }
                    // Picture
                    if(photo != null){
                        posts.add(new VkPostInfo.Picture(posts.size(), text, photo, url));
                        continue;
                    }
                    // Video
                    if(videoPreview != null){
                        posts.add(new VkPostInfo.Video(posts.size(), text, videoPreview, url));
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

        private final API api;
        public Client(API api){
            this.api = api;
        }

        public String getClientVersion(){
            try {
                return api.getJSON(ApiMethod.create("client.getFilesInfo")).getString("build");
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
                return api.getJSON(ApiMethod.create("client.getFilesInfo")).getString("build_id");
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return "-1";
        }

        public String getJarVersion(){
            try {
                return api.getJSON(ApiMethod.create("client.getFilesInfo")).getString("version");
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
                return new ModInfo(api, api.getJSON(ApiMethod.create("client.getModInfo").set("index", index).set("require_icon", hasIcons)).getJSONArray("mods").getJSONObject(0));
            }catch (Exception ignored){
            }
            return null;
        }

        public BufferedImage getModIcon(int index){
            try {
                return api.getImage(ApiMethod.create("client.getModIcon").set("index", index));
            }catch (Exception ignored){
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
                        JSONObject downloadInfo = api.getJSON(ApiMethod.create("client.getSizeInfo"));
                        JSONObject zipInfo = downloadInfo.getJSONObject("zip");
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
                        JSONObject folderInfo = downloadInfo.getJSONObject("folders");
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

                            JSONObject checksumResult = api.getJSON(ApiMethod.create("client.checkSum").set("mods", md5_mods).set("client", md5_client));
                            if (!checksumResult.has("equal_mods") || !checksumResult.has("equal_client")) {
                                JOptionPane.showMessageDialog(null, "Файлы игры отличаются от файлов на сервере!", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);
                                boolean mods = checksumResult.has("dif_mods") && checksumResult.getBoolean("dif_mods");
                                boolean versions = checksumResult.has("dif_client") && checksumResult.getBoolean("dif_client");

                                JSONObject fileSizes = api.getJSON(ApiMethod.create("client.getSizeInfo"));
                                JSONObject folderSizes = fileSizes.getJSONObject("folders");
                                long folderVersions = folderSizes.getLong("versions");
                                long folderMods = folderSizes.getLong("mods");

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


                        if(api.PlayerInfo.applyIP() == -1)
                            JOptionPane.showMessageDialog(null, "В данный момент вход на сервер недоступен, но вы можете играть в одиночной игре", "Предупреждение", JOptionPane.INFORMATION_MESSAGE);

                        MinecraftStarter starter = new MinecraftStarter("client"){{
                            addServer(api.launcher.getConfig().Net.Minecraft.getIp());
                            setNickname(api.PlayerInfo.getNickname());
                            setFullscreen(!api.launcher.getSettings().isWindowed());
                            setRam(api.launcher.getSettings().getRAM());
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
            IOUtils.receiveFile(api.getUrl(ApiMethod.create("client.get").set("name", name)), "client/" + name + ".zip", listener);
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
            private final int index;
            private final boolean hasIcon;
            private BufferedImage icon;
            private final API api;

            public ModInfo(API api, JSONObject jsonObject){
                this.api = api;
                name = jsonObject.getString("name");
                description = jsonObject.getString("description");
                index = jsonObject.getInt("index");
                hasIcon = jsonObject.getBoolean("icon");
            }

            public String getName(){
                return name;
            }

            public String getDescription(){
                return description;
            }

            public boolean hasIcon(){
                return hasIcon;
            }

            public BufferedImage getIcon(){
                if(icon == null) {
                    try {
                        icon = api.getImage(ApiMethod.create("client.getModIcon"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return icon;
            }

            public int getIndex(){
                return index;
            }
        }
    }

    public static String toBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            stream.flush();
            byte[] imageBytes = stream.toByteArray();
            stream.close();

            return Base64.getEncoder().encodeToString(imageBytes);
        }catch (Exception ex){
            return null;
        }
    }


}
