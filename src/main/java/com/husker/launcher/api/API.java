package com.husker.launcher.api;

import com.alee.utils.general.Pair;
import com.husker.launcher.Launcher;
import com.husker.launcher.managers.ProfileApiMethod;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.launcher.utils.ConsoleUtils;
import com.husker.net.Get;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;

public class API {

    private static final Logger log = LogManager.getLogger(API.class);

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

    public static class APIException extends Exception{
        public APIException(String message){
            super(message);
        }
    }

    private static InputStream getInputStream(String url) throws APIException {
        try {
            Get get = new Get(url);
            get.execute();
            if (get.getHeader("Content-Type").contains("application/json")) {
                JSONObject jsonObject = new JSONObject(get.getHtmlContent());
                if (jsonObject.has("error_code") && jsonObject.getInt("error_code") < 0)
                    throw new APIException(jsonObject.getString("error"));
                return new ByteArrayInputStream(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            }
            return get.getInputStream();
        }catch (IOException ioe){
            throw new APIException(ioe.getMessage());
        }
    }

    public static BufferedImage getImage(ApiMethod method) throws APIException {
        try {
            return ImageIO.read(getInputStream(getMethodUrl(method)));
        }catch (IOException ioe){
            throw new APIException(ioe.getMessage());
        }
    }

    public static BufferedImage getImage(String methodName) throws APIException {
        return getImage(ApiMethod.create(methodName));
    }

    public static JSONObject getJSON(ApiMethod method) throws APIException {
        String text = new BufferedReader(new InputStreamReader(getInputStream(getMethodUrl(method)), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        return new JSONObject(text);
    }

    public static JSONObject getJSON(String methodName) throws APIException {
        return getJSON(ApiMethod.create(methodName));
    }

    public static String getMethodUrl(ApiMethod apiMethod){
        String s = "http://" + LauncherConfig.getAuthIp() + ":" + LauncherConfig.getAuthPort() + "/api/method/" + apiMethod.getUrl();
        log.info(s);
        return s;
    }

    public static String[] toStringArray(JSONArray array){
        ArrayList<String> arr = new ArrayList<>();
        for(int i = 0; i < array.length(); i++)
            arr.add(array.getString(i));
        return arr.toArray(new String[0]);
    }

    public static class Minecraft {

        public static ServerInfo getServerInfo() throws APIException {
            JSONObject jsonObject = getJSON(ApiMethod.create("minecraft.getServerInfo"));
            return new ServerInfo(jsonObject.getString("ip"), jsonObject.getInt("port"));
        }

        public static class ServerInfo {
            private final String ip;
            private final int port;
            public ServerInfo(String ip, int port){
                this.ip = ip;
                this.port = port;
            }

            public String getIP(){
                return ip;
            }

            public int getPort(){
                return port;
            }
        }
    }

    public static class Skins {

        public static String[] getCategories() throws APIException {
            return toStringArray(API.getJSON("skins.getCategories").getJSONArray("categories"));
        }

        public static String[] getCategorySkins(String category) throws APIException {
            return toStringArray(API.getJSON(ApiMethod.create("skins.getCategorySkins").set("category", category)).getJSONArray("skins"));
        }

        public static BufferedImage getCategorySkin(String category, String name) throws APIException {
            return API.getImage(ApiMethod.create("skins.getCategorySkin").set("category", category).set("name", name));
        }

        public static BufferedImage getCategoryPreview(String category) throws APIException {
            return API.getImage(ApiMethod.create("skins.getCategoryPreview").set("category", category));
        }
    }

    public static class Auth {

        public static String getAccessToken(String login, String password) throws APIException, WrongAuthDataException {
            JSONObject request = getJSON(ApiMethod.create("auth.getAccessToken").set(LOGIN, login).set(PASSWORD, password));
            if(!request.has(ACCESS_TOKEN))
                throw new WrongAuthDataException();
            return request.getString(ACCESS_TOKEN);
        }

        public static void register(String login, String password) throws APIException, IncorrectLoginFormatException, LoginAlreadyExistException, IncorrectPassswordFormatException {
            switch (getJSON(ApiMethod.create("auth.create").set(LOGIN, login).set(PASSWORD, password)).getInt(RESULT)){
                case 0: return;
                case 1: throw new IncorrectLoginFormatException();
                case 2: throw new LoginAlreadyExistException();
                case 3: throw new IncorrectPassswordFormatException();
                default: throw new APIException("Unknown response");
            }
        }
    }

    public static class Profile {

        public static void bindIP(String token) throws APIException {
            getJSON(ProfileApiMethod.create("profile.bindIP", token));
        }

        public static BufferedImage getSkin(String token) throws APIException {
            return getImage(ProfileApiMethod.create("profile.getSkin", token));
        }

        public static void setSkin(String token, BufferedImage skin) throws APIException, SkinTooLargeException {
            JSONObject jsonObject = getJSON(ProfileApiMethod.create("profile.setSkin", token).set("skin", toBase64(skin)));
            if(jsonObject.has("error_code") && jsonObject.getInt("error_code") == 3)
                throw new SkinTooLargeException();
        }

        public static void setSkin(String token, String category, String name) throws APIException, CategoryNotFoundException, SkinNameNotFoundException {
            JSONObject jsonObject = getJSON(ProfileApiMethod.create("profile.setSkin", token).set("category", category).set("name", name));
            if(jsonObject.has("error_code")){
                switch (jsonObject.getInt("error_code")){
                    case 1: throw new CategoryNotFoundException(category);
                    case 2: throw new SkinNameNotFoundException(category, name);
                    default: throw new APIException("Unknown response");
                }
            }
        }

        public static boolean confirmEmail(String token, String email, String emailCode) throws APIException, EmailIsNotSpecifiedException {
            JSONObject jsonObject = getJSON(ProfileApiMethod.create("profile.confirmEmail", token).set(EMAIL, email).set(EMAIL_CODE, emailCode));
            if(jsonObject.has("error_code") && jsonObject.getInt("error_code") == 1)
                throw new EmailIsNotSpecifiedException();
            return jsonObject.getBoolean("confirmed");
        }

        public static boolean isEmailConfirmed(String token) throws APIException {
            return getJSON(ProfileApiMethod.create("profile.isEmailConfirmed", token)).getBoolean("confirmed");
        }

        public static void sendEmailCode(String token, String email) throws APIException, EmailCodeSendingException {
            int result = getJSON(ProfileApiMethod.create("profile.sendEmailCode", token).set(EMAIL, email)).getInt(RESULT);
            if(result == 1)
                throw new EmailCodeSendingException();
        }

        public static HashMap<String, String> getData(String token, String... fields) throws APIException {
            HashMap<String, String> data = new HashMap<>();

            JSONObject jsonObject = getJSON(ProfileApiMethod.create("profile.getData", token).set("fields", String.join(",", fields)));
            jsonObject.getJSONObject("data").toMap().forEach((name, value) -> data.put(name, value.toString()));

            return data;
        }

        public static void setData(String token, HashMap<String, String> data, String currentPassword, String emailCode) throws APIException, CurrentPasswordRequiredException, EmailCodeRequiredException, IncorrectCurrentPasswordException, IncorrectEmailCodeException, IncorrectLoginFormatException, LoginAlreadyExistException, IncorrectEmailFormatException, IncorrectPassswordFormatException {
            ProfileApiMethod method = ProfileApiMethod.create("profile.setSkin", token);
            if(currentPassword != null)
                method.set(CURRENT_PASSWORD, currentPassword);
            if(emailCode != null)
                method.set(EMAIL_CODE, emailCode);
            data.forEach(method::set);

            JSONObject jsonObject = getJSON(method);
            if(jsonObject.has("error_code")){
                switch (jsonObject.getInt("error_code")){
                    case 1: throw new CurrentPasswordRequiredException();
                    case 2: throw new EmailCodeRequiredException();
                    case 3: throw new IncorrectCurrentPasswordException();
                    case 4: throw new IncorrectEmailCodeException();
                    case 5: throw new IncorrectLoginFormatException();
                    case 6: throw new LoginAlreadyExistException();
                    case 7: throw new IncorrectEmailFormatException();
                    case 8: throw new IncorrectPassswordFormatException();
                    default: throw new APIException(jsonObject.getString("error"));
                }
            }
            if(!jsonObject.has("result") || jsonObject.getInt("result") != 1)
                throw new APIException("Unknown api response");
        }
    }


    public static class Client{

        public static JSONObject getSizeInfo() throws APIException {
            return getJSON(ApiMethod.create("client.getSizeInfo"));
        }

        public static Pair<Boolean, Boolean> checkSum(String mods, String client) throws APIException {
            JSONObject object = getJSON(ApiMethod.create("client.checkSum").set("mods", mods).set("client", client));
            return new Pair<>(object.getBoolean("equal_mods"), object.getBoolean("equal_client"));
        }

        public static String getClientVersion() throws APIException {
            return getJSON(ApiMethod.create("client.getFilesInfo")).getString("build");
        }

        public static String getShortClientVersion() throws APIException {
            return getJSON(ApiMethod.create("client.getFilesInfo")).getString("build_id");
        }

        public static String getJarVersion() throws APIException {
            return getJSON(ApiMethod.create("client.getFilesInfo")).getString("version");
        }

        public static ModInfo getModInfo(int index, boolean hasIcons) throws APIException{
            JSONArray info = getJSON(ApiMethod.create("client.getModInfo").set("index", index).set("require_icon", hasIcons)).getJSONArray("mods");
            if(info.length() == 1)
                return new ModInfo(info.getJSONObject(0));
            return null;
        }

        public static BufferedImage getModIcon(int index) throws APIException {
            return getImage(ApiMethod.create("client.getModIcon").set("index", index));
        }

        public static class ModInfo{

            private final String name;
            private final String description;
            private final int index;
            private final boolean hasIcon;
            private BufferedImage icon;

            public ModInfo(JSONObject jsonObject){
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
                        icon = API.getImage(ApiMethod.create("client.getModIcon"));
                    } catch (Exception e) {
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

            String out = Base64.getEncoder().encodeToString(imageBytes);
            return out != null ? out : "null";
        }catch (Exception ex){
            return "null";
        }
    }

    public static class WrongAuthDataException extends Exception{
        public WrongAuthDataException(){
            super("Wrong auth data");
        }
    }

    public static class IncorrectLoginFormatException extends Exception{
        public IncorrectLoginFormatException(){
            super("Incorrect login format");
        }
    }

    public static class IncorrectPassswordFormatException extends Exception{
        public IncorrectPassswordFormatException(){
            super("Incorrect password format");
        }
    }

    public static class LoginAlreadyExistException extends Exception{
        public LoginAlreadyExistException(){
            super("Login is already exist");
        }
    }

    public static class SkinTooLargeException extends Exception{
        public SkinTooLargeException(){
            super("Skin size is too large (Must be 64x64)");
        }
    }

    public static class CategoryNotFoundException extends Exception{
        public CategoryNotFoundException(String category){
            super("Category '" + category + "' doesn't exist");
        }
    }

    public static class SkinNameNotFoundException extends Exception{
        public SkinNameNotFoundException(String category, String name){
            super("Skin name '" + name + "' doesn't exist in '" + category + "' category");
        }
    }

    public static class CurrentPasswordRequiredException extends Exception{
        public CurrentPasswordRequiredException(){
            super("Current password required");
        }
    }

    public static class EmailCodeRequiredException extends Exception{
        public EmailCodeRequiredException(){
            super("Email code required");
        }
    }

    public static class IncorrectCurrentPasswordException extends Exception{
        public IncorrectCurrentPasswordException(){
            super("Incorrect current password");
        }
    }

    public static class IncorrectEmailCodeException extends Exception{
        public IncorrectEmailCodeException(){
            super("Incorrect email code");
        }
    }

    public static class IncorrectEmailFormatException extends Exception{
        public IncorrectEmailFormatException(){
            super("Incorrect email format");
        }
    }

    public static class EmailCodeSendingException extends Exception{
        public EmailCodeSendingException(){
            super("Can't send email code");
        }
    }

    public static class EmailIsNotSpecifiedException extends Exception{
        public EmailIsNotSpecifiedException(){
            super("Email is not specified");
        }
    }
}
