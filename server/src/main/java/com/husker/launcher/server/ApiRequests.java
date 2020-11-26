package com.husker.launcher.server;

import com.husker.launcher.server.services.HtmlService;
import com.husker.launcher.server.utils.*;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApiRequests {

    public static HashMap<String, Getter> getters = new HashMap<String, Getter>(){{
        // Client
        new ClientMethods(this);

        // Skins
        new SkinMethods(this);

        // Profile
        new AuthMethods(this);
        new ProfilesMethods(this);
        new ProfileMethods(this);

        // Screenshots
        new ScreenshotsMethods(this);

        // Social
        new VkMethods(this);
        new YouTubeMethods(this);
        new InstagramMethods(this);
        new GitHubMethods(this);

        // About
        new AboutMethods(this);

        // Minecraft
        new MinecraftMethods(this);
    }};

    static class AboutMethods extends MethodContainer {

        public AboutMethods(HashMap<String, Getter> container) {
            super(container, "about");

            put("getOwnerInfo", exchange -> new JSONObject(){{
                put("name", ServerMain.Settings.getOwnerName());
                put("url", ServerMain.Settings.getOwnerUrl());
            }});
            put("getSupportInfo", exchange -> new JSONObject(){{
                put("name", ServerMain.Settings.getSupportName());
                put("url", ServerMain.Settings.getSupportUrl());
            }});
        }
    }

    static class MinecraftMethods extends MethodContainer {

        public MinecraftMethods(HashMap<String, Getter> container) {
            super(container, "minecraft");

            put("getServerInfo", exchange -> new JSONObject(){{
                put("ip", ServerMain.Settings.getMinecraftServerIP());
                put("port", ServerMain.Settings.getMinecraftServerPort());
            }});
        }
    }

    static class ClientMethods extends MethodContainer{

        public ClientMethods(HashMap<String, Getter> container) {
            super(container, "client");

            put("get", exchange -> {
                String name = getAttribute(exchange, "name");

                if (name.equals("other"))
                    return new File(UpdateManager.clientFolder + "/other.zip");
                if (name.equals("mods"))
                    return new File(UpdateManager.clientFolder + "/mods.zip");
                if (name.equals("versions"))
                    return new File(UpdateManager.clientFolder + "/versions.zip");

                return new HtmlService.ErrorMessage("File not specified or not found", 1);
            });
            put("getSizeInfo", exchange -> {
                try {
                    return new JSONObject(IOUtils.readFileText(UpdateManager.clientFolder + "/files_info.json"));
                } catch (IOException e) {
                    return new HtmlService.ErrorMessage("Can't read client info file", 1);
                }
            });
            put("getFilesInfo", exchange -> {
                try {
                    JSONObject out = new JSONObject();
                    JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));

                    out.put("build", object.get("build") + "");
                    out.put("build_id", object.get("build_id") + "");
                    out.put("version", object.get("version") + "");
                    return out;
                }catch (Exception ex){
                    return new HtmlService.ErrorMessage("Can't read client info file", 1);
                }
            });
            put("checkSum", exchange -> {
                try {
                    JSONObject out = new JSONObject();
                    JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));
                    out.put("equal_mods", true);
                    out.put("equal_client", true);

                    String mods = getAttribute(exchange, "mods");
                    String client = getAttribute(exchange, "client");

                    if(mods == null || !object.getString("mods_MD5").equals(mods))
                        out.put("equal_mods", false);
                    if(client == null || !object.getString("client_MD5").equals(client))
                        out.put("equal_client", false);
                    return out;
                }catch (AttributeNotFoundException ax){
                    throw ax;
                }
                catch (Exception ex){
                    return new HtmlService.ErrorMessage("Can't read client info file", 1);
                }
            });
            put("getModInfo", exchange -> {
                JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));
                JSONArray mods = object.getJSONArray("mods");
                boolean require_icon = containsAttribute(exchange, "require_icon") && getAttribute(exchange, "require_icon").equals("true");

                ArrayList<JSONObject> objects = new ArrayList<>();
                if (containsAttribute(exchange, "count")) {
                    int count = Integer.parseInt(getAttribute(exchange, "count"));
                    if (require_icon) {
                        for (int i = 0; objects.size() < count && i < mods.length(); i++) {
                            if (!mods.getJSONObject(i).getString("icon").equals("null")) {
                                JSONObject info = mods.getJSONObject(i);
                                info.put("index", i);
                                objects.add(info);
                            }
                        }
                    } else {
                        for (int i = 0; i < count; i++) {
                            JSONObject info = mods.getJSONObject(i);
                            info.put("index", i);
                            objects.add(info);
                        }
                    }
                } else if (containsAttribute(exchange, "index")) {
                    int index = Integer.parseInt(getAttribute(exchange, "index"));
                    if (require_icon) {
                        int remain = index;
                        for (int i = 0; i < mods.length(); i++) {
                            if (!mods.getJSONObject(i).getString("icon").equals("null")) {
                                if (remain == 0) {
                                    JSONObject info = mods.getJSONObject(i);
                                    info.put("index", i);
                                    objects.add(info);
                                    break;
                                } else
                                    remain--;
                            }
                        }
                    } else {
                        JSONObject info = mods.getJSONObject(index);
                        info.put("index", index);
                        objects.add(info);
                    }
                }else{
                    for (int i = 0; i < mods.length(); i++) {
                        JSONObject info = mods.getJSONObject(i);
                        info.put("index", i);
                        objects.add(info);
                    }
                }

                for(JSONObject modInfo : objects)
                    modInfo.put("icon", modInfo.has("icon"));

                return new JSONObject(){{
                    put("mods", objects);
                }};
            });

            put("getModIcon", exchange -> {
                int index = Integer.parseInt(getAttribute(exchange, "index"));

                JSONObject object = new JSONObject(FileUtils.readFileToString(new File(UpdateManager.clientFolder + "/client_info.json"), StandardCharsets.UTF_8));
                JSONArray mods = object.getJSONArray("mods");

                return fromBase64(mods.getJSONObject(index).get("icon") + "");
            });
        }
    }

    static class AuthMethods extends MethodContainer{

        public AuthMethods(HashMap<String, Getter> container) {
            super(container, "auth");

            put("getAccessToken", exchange -> {
                Profile profile = Profile.get(getAttribute(exchange, "login"), getAttribute(exchange, "password"));

                if(profile != null)
                    return new SimpleJSON("access_token", profile.createKey());
                else
                    return new HtmlService.ErrorMessage("Wrong login or password", 1);
            });

            put("create", exchange -> new SimpleJSON("result", Profile.create(getAttribute(exchange, Profile.LOGIN), getAttribute(exchange, Profile.PASSWORD))));
        }
    }

    static class ProfileMethods extends MethodContainer{

        public ProfileMethods(HashMap<String, Getter> container) {
            super(container, "profile");

            putProfile("getData", (exchange, profile) -> new SimpleJSON("data", profile.getData(getAttribute(exchange, "fields").split(","))));

            putProfile("setData", (exchange, profile) -> {
                HashMap<String, String> fields = getAttributeMap(exchange);
                HashMap<String, String> confirms = new HashMap<String, String>(){{
                    if(fields.containsKey(Profile.EMAIL_CODE)) {
                        put(Profile.EMAIL_CODE, getAttribute(exchange, Profile.EMAIL_CODE));
                        fields.remove(Profile.EMAIL_CODE);
                    }
                    if(fields.containsKey(Profile.CURRENT_PASSWORD)) {
                        put(Profile.CURRENT_PASSWORD, getAttribute(exchange, Profile.CURRENT_PASSWORD));
                        fields.remove(Profile.CURRENT_PASSWORD);
                    }
                }};
                HtmlService.ErrorMessage message = profile.setData(fields, confirms);

                return message != null ? message : new SimpleJSON("result", 1);
            });

            putProfile("isEmailConfirmed", (exchange, profile) -> new SimpleJSON("confirmed", profile.isEmailConfirmed()));
            putProfile("sendEmailCode", (exchange, profile) -> {
                String email = containsAttribute(exchange, Profile.EMAIL) ? getAttribute(exchange, Profile.EMAIL) : profile.data.get(Profile.EMAIL);
                if(profile.sendEmailCode(email) == 1)
                    return new HtmlService.ErrorMessage("Can't send email code", 1);
                return new SimpleJSON("sent", true);
            });
            putProfile("bindIP", (exchange, profile) -> {
                profile.setIP(exchange.getRemoteAddress().getHostName());
                return new SimpleJSON("bound", true);
            });

            putProfile("confirmEmail", (exchange, profile) -> {
                if(!profile.data.get(Profile.EMAIL).equals("null"))
                    return new HtmlService.ErrorMessage("Email is empty", 1);

                if(profile.isValidEmailCode(getAttribute(exchange, Profile.EMAIL), getAttribute(exchange, Profile.EMAIL_CODE)))
                    profile.data.set(Profile.EMAIL, getAttribute(exchange, Profile.EMAIL));

                return new SimpleJSON("confirmed", profile.isEmailConfirmed());
            });

            putProfile("getSkin", (exchange, profile) -> profile.getSkin());

            putProfile("setSkin", (exchange, profile) -> {
                BufferedImage skin;

                if (containsAttribute(exchange, "category") && containsAttribute(exchange, "name")) {
                    String category = getAttribute(exchange, "category");
                    String name = getAttribute(exchange, "name");

                    if(!Files.exists(Paths.get("./skins/" + category)))
                        return new HtmlService.ErrorMessage("Category '" + category + "' doesn't exist", 1);
                    if(!Files.exists(Paths.get("./skins/" + category + "/" + name + ".png")))
                        return new HtmlService.ErrorMessage("Skin '" + name + "' doesn't exist in category '" + category + "'", 2);

                    skin = ImageIO.read(new File("./skins/" + category + "/" + name + ".png"));
                }else if(containsAttribute(exchange, "skin"))
                    skin = fromBase64(getAttribute(exchange, "skin"));
                else
                    return new HtmlService.ErrorMessage("Can't find attributes (category, name) or (skin)", -2);

                if(skin == null || skin.getWidth() != 64 || skin.getHeight() != 64)
                    return new HtmlService.ErrorMessage("Image is too large. Preferred skin size: 64x64", 3);


                ImageIO.write(skin, "png", new File(profile.getFolder() + "/skin.png"));
                profile.setData(Profile.SKIN_URL, ProfileUtils.getImageUrl(skin));

                return new SimpleJSON("result", 1);
            });
        }
    }

    static class SkinMethods extends MethodContainer {

        public SkinMethods(HashMap<String, Getter> container) {
            super(container, "skins");

            put("getCategories", exchange -> {
                ArrayList<String> list = new ArrayList<>();

                if(Files.exists(Paths.get("./skins")))
                    for (File file : new File("./skins").listFiles(File::isDirectory))
                        list.add(file.getName());
                return new JSONObject(){{
                    put("categories", list.toArray(new String[0]));
                }};
            });

            put("getCategoryPreview", exchange -> {
                String category = getAttribute(exchange, "category");
                if(Files.exists(Paths.get("./skins/" + category))){
                    File[] skins = Objects.requireNonNull(new File("./skins/" + category).listFiles(file -> file.getName().endsWith(".png")));
                    if(skins.length > 0)
                        return ImageIO.read(skins[new Random().nextInt(skins.length)]);
                }

                BufferedImage emptySkin = new BufferedImage(64, 64, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g2d = (Graphics2D) emptySkin.getGraphics();
                for(int i = 0; i < 64; i++){
                    g2d.setColor(Color.getHSBColor(1f / 64f * i, 1, 1));
                    g2d.fillRect(0, i, 64, 1);
                }
                return emptySkin;
            });

            put("getCategorySkins", exchange -> {
                String category = getAttribute(exchange, "category");
                if(Files.exists(Paths.get("./skins/" + category))){
                    File[] skins = new File("./skins/" + category).listFiles(file -> file.getName().endsWith(".png"));

                    ArrayList<String> list = new ArrayList<>();
                    for (File skin : skins)
                        list.add(skin.getName().replace(".png", ""));

                    return new JSONObject(){{
                        put("skins", list.toArray(new String[0]));
                    }};
                }else
                    return new HtmlService.ErrorMessage("Category '" + category + "' doesn't exist", 1);
            });

            put("getCategorySkin", exchange -> {
                String category = getAttribute(exchange, "category");
                String name = getAttribute(exchange, "name");

                if(Files.exists(Paths.get("./skins/" + category + "/" + name + ".png")))
                    return ImageIO.read(new File("./skins/" + category + "/" + name + ".png"));
                else
                    return new HtmlService.ErrorMessage("Skin " + category + "/" + name + " doesn't exist", 1);
            });

            put("getSkin", exchange -> {
                String name = getAttribute(exchange, "name");
                Profile profile = Profile.getByName(name);
                if(profile != null) {
                    if(profile.getSkin() == null)
                        return ImageIO.read(getClass().getResource("/steve.png"));
                    return profile.getSkin();
                }else
                    return new HtmlService.ErrorMessage("Can't find profile with name: '" + name + "'", 1);
            });
        }
    }

    static class VkMethods extends MethodContainer {

        public VkMethods(HashMap<String, Getter> container) {
            super(container, "vk");

            put("getInfo", exchange -> new SimpleJSON("id", ServerMain.Settings.getVKGroupId()));
            putProfile("setInfo", (exchange, profile) -> {
                if(profile.getDataValue(Profile.STATUS).equals("Администратор"))
                    ServerMain.Settings.setVKGroupId(getAttribute(exchange, "id"));
                else
                    return new HtmlService.ErrorMessage("The account has no rights to change the value", 1);
                return new SimpleJSON("changed", true);
            });
        }

    }

    static class YouTubeMethods extends MethodContainer {

        public YouTubeMethods(HashMap<String, Getter> container) {
            super(container, "youtube");

            put("getInfo", exchange -> new SimpleJSON("id", ServerMain.Settings.getYouTubeId()));
            putProfile("setInfo", (exchange, profile) -> {
                if(profile.getDataValue(Profile.STATUS).equals("Администратор"))
                    ServerMain.Settings.setYouTubeId(getAttribute(exchange, "id"));
                else
                    return new HtmlService.ErrorMessage("The account has no rights to change the value", 1);
                return new SimpleJSON("changed", true);
            });
        }
    }

    static class InstagramMethods extends MethodContainer {

        public InstagramMethods(HashMap<String, Getter> container) {
            super(container, "instagram");

            put("getInfo", exchange -> new SimpleJSON("id", ServerMain.Settings.getInstagramId()));
            putProfile("setInfo", (exchange, profile) -> {
                if(profile.getDataValue(Profile.STATUS).equals("Администратор"))
                    ServerMain.Settings.setInstagramId(getAttribute(exchange, "id"));
                else
                    return new HtmlService.ErrorMessage("The account has no rights to change the value", 1);
                return new SimpleJSON("changed", true);
            });
        }
    }

    static class GitHubMethods extends MethodContainer {

        public GitHubMethods(HashMap<String, Getter> container) {
            super(container, "github");

            put("getInfo", exchange -> new SimpleJSON("repo", ServerMain.Settings.getGitHubId()));
            putProfile("setInfo", (exchange, profile) -> {
                if(profile.getDataValue(Profile.STATUS).equals("Администратор"))
                    ServerMain.Settings.setGitHubId(getAttribute(exchange, "repo"));
                else
                    return new HtmlService.ErrorMessage("The account has no rights to change the value", 1);
                return new SimpleJSON("changed", true);
            });
        }
    }

    static class ProfilesMethods extends MethodContainer {

        public ProfilesMethods(HashMap<String, Getter> container) {
            super(container, "profiles");

            put("isIpBound", exchange -> new SimpleJSON("bound", ProfileUtils.isValidIp(getAttribute(exchange, "name"), getAttribute(exchange, "ip"))));
            put("isLoginTaken", exchange -> new SimpleJSON("exist", ProfileUtils.isNicknameExist(getAttribute(exchange, Profile.LOGIN))));
        }
    }

    static class ScreenshotsMethods extends MethodContainer {

        public ScreenshotsMethods(HashMap<String, Getter> container) {
            super(container, "screenshots");

            put("getNames", exchange -> {
                checkFileNames();
                ArrayList<String> hashes = new ArrayList<>();
                for(File file : new File("./screenshots").listFiles())
                    hashes.add(file.getName().split("name_")[1].split("\\.")[0]);
                return new JSONObject(){{
                    put("names", hashes.toArray(new String[0]));
                }};
            });

            put("get", exchange -> {
                //checkFileNames();
                int index = Integer.parseInt(getAttribute(exchange, "index"));
                if(index > new File("./screenshots").listFiles().length)
                    return new HtmlService.ErrorMessage("Can't find screenshot with index: '" + index + "'", 1);
                return ImageIO.read(new File("./screenshots").listFiles()[index]);
            });
        }

        private void checkFileNames() {
            for(File file : new File("./screenshots").listFiles()){
                if(!file.getName().startsWith("name_")){
                    String hash = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    while(true) {
                        if(file.renameTo(new File("./screenshots/name_" + hash + file.getName().substring(file.getName().lastIndexOf(".")))))
                            break;
                    }
                }
            }
        }
    }

    public interface Getter{
        Object apply(HttpExchange exchange) throws Exception;
    }

    public interface ProfileGetter{
        Object apply(HttpExchange exchange, Profile profile) throws Exception;
    }

    public static class MethodContainer{
        private final String name;
        private final HashMap<String, Getter> container;

        public MethodContainer(HashMap<String, Getter> container, String name){
            this.name = name;
            this.container = container;
        }

        public void put(String method, Getter getter){
            container.put(name + "." + method, getter);
        }

        public void putProfile(String method, ProfileGetter getter){
            put(method, exchange -> {
                Profile profile = Profile.get(getAttribute(exchange, Profile.ACCESS_TOKEN));
                if(profile == null)
                    return new HtmlService.ErrorMessage("Access token required!", -2);
                else
                    return getter.apply(exchange, profile);
            });
        }
    }

    public static boolean containsAttribute(HttpExchange exchange, String attribute){
        return getAttributeMap(exchange).containsKey(attribute);
    }

    public static HashMap<String, String> getAttributeMap(HttpExchange exchange){
        HashMap<String, String> result = new HashMap<>();

        if(exchange.getRequestURI().getQuery() != null) {
            for (String param : exchange.getRequestURI().getQuery().split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1)
                    result.put(entry[0], entry[1]);
                else
                    result.put(entry[0], "");
            }
        }
        return result;
    }

    public static String getAttribute(HttpExchange exchange, String attribute){
        if(!containsAttribute(exchange, attribute))
            throw new AttributeNotFoundException(attribute);
        try {
            return getAttributeMap(exchange).get(attribute);
        }catch (Exception ex){
            throw new AttributeNotFoundException(attribute);
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

    public static BufferedImage fromBase64(String text) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(text));
            BufferedImage image = ImageIO.read(stream);
            stream.close();

            return image;
        }catch (Exception ex){
            return null;
        }
    }

    public static class AttributeNotFoundException extends RuntimeException{

        public AttributeNotFoundException(String attribute){
            super("Attribute '" + attribute + "' not found");
        }

    }

    public static class SimpleJSON extends JSONObject{

        public SimpleJSON(String key, Object value){
            put(key, value);
        }
    }


}
