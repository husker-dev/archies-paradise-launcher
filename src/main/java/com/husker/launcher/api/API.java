package com.husker.launcher.api;

import com.husker.launcher.managers.NetManager;
import com.husker.launcher.managers.ProfileApiMethod;
import com.husker.launcher.settings.LauncherConfig;
import com.husker.net.Get;
import com.husker.net.HttpUrlBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;

public class API {

    private static final Logger log = LogManager.getLogger(API.class);

    public static final String ACCESS_TOKEN = "access_token";
    public static final String LOGIN = "login";
    public static final String EMAIL = "email";
    public static final String EMAIL_CODE = "email_code";
    public static final String STATUS = "status";
    public static final String PASSWORD = "password";
    public static final String CURRENT_PASSWORD = "current_password";
    public static final String ID = "id";

    public static class APIException extends RuntimeException{
        private final int code;
        public APIException(String message, int code){
            super(message);
            this.code = code;
        }

        public int getCode(){
            return code;
        }
    }

    public static class InternalAPIException extends Exception{
        private final int code;
        public InternalAPIException(String message, int code){
            super(message);
            this.code = code;
        }

        public int getCode(){
            return code;
        }
    }

    private static InputStream getInputStream(String url, Integer... knownResponse) throws APIException, InternalAPIException {
        try {
            Get get = new Get(url);
            get.execute();
            if (get.getHeader("Content-Type").contains("application/json")) {
                JSONObject jsonObject = new JSONObject(get.getHtmlContent());
                int code = jsonObject.has("code") ? jsonObject.getInt("code") : 0;

                if (code < 0)
                    throw new InternalAPIException(jsonObject.getString("message"), code);
                else if(code > 0) {
                    if(knownResponse.length == 0 || Arrays.asList(knownResponse).contains(code))
                        throw new APIException(jsonObject.getString("message"), code);
                    else
                        throw new InternalAPIException("Unknown server response", -404);
                }
                return new ByteArrayInputStream(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            }
            return get.getInputStream();
        }catch (IOException ioe){
            throw new InternalAPIException(ioe.getMessage(), -404);
        }
    }

    public static BufferedImage getImage(ApiMethod method, Integer... knownResponse) throws APIException, InternalAPIException {
        try {
            return ImageIO.read(getInputStream(getMethodUrl(method), knownResponse));
        }catch (IOException ioe){
            throw new InternalAPIException(ioe.getMessage(), -404);
        }
    }

    public static BufferedImage getImage(String methodName, Integer... knownResponse) throws APIException, InternalAPIException {
        return getImage(ApiMethod.create(methodName), knownResponse);
    }

    public static JSONObject getJSON(ApiMethod method, Integer... knownResponse) throws APIException, InternalAPIException {
        String text = new BufferedReader(new InputStreamReader(getInputStream(getMethodUrl(method), knownResponse), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        return new JSONObject(text);
    }

    public static JSONObject getJSON(String methodName, Integer... knownResponse) throws APIException, InternalAPIException {
        return getJSON(ApiMethod.create(methodName), knownResponse);
    }

    public static String getMethodUrl(ApiMethod apiMethod){
        String s = "http://" + LauncherConfig.getAuthIp() + ":" + LauncherConfig.getAuthPort() + "/api/method/" + apiMethod.getUrl();
        HttpUrlBuilder builder = new HttpUrlBuilder(s);

        String oldUrl = builder.toString();
        builder.remove("password");
        String newUtr = builder.toString();
        if(!oldUrl.equals(newUtr))
            builder.set("password", "WhatDoYouWantToSeeHere");
        log.info(builder.toString());
        return s;
    }

    public static String[] toStringArray(JSONArray array){
        ArrayList<String> arr = new ArrayList<>();
        for(int i = 0; i < array.length(); i++)
            arr.add(array.getString(i));
        return arr.toArray(new String[0]);
    }

    public static class Minecraft {

        public static ServerInfo getServerInfo() throws InternalAPIException {
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

    public static class Launcher{
        public static String getCurrentVersion() throws InternalAPIException {
            return API.getJSON("launcher.getVersion").getString("version");
        }
    }

    public static class Skins {

        public static String[] getCapes() throws InternalAPIException{
            return toStringArray(API.getJSON("skins.getCapes").getJSONArray("names"));
        }

        public static BufferedImage getCapeByName(String capeName) throws InternalAPIException, WrongAccessTokenException {
            try {
                return getImage(ProfileApiMethod.create("skins.getCapeByName").set("name", capeName), 25);
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return null;
        }

        public static BufferedImage getElytraByName(String elytraName) throws InternalAPIException, WrongAccessTokenException {
            try {
                return getImage(ProfileApiMethod.create("skins.getElytraByName").set("name", elytraName), 25);
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return null;
        }

        public static BufferedImage getCape(String name) throws InternalAPIException, CategoryNotFoundException{
            try {
                return API.getImage(ApiMethod.create("skins.getCape").set("name", name), 1);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new CategoryNotFoundException(name);
            }
            return null;
        }

        public static String[] getCategories() throws InternalAPIException {
            return toStringArray(API.getJSON("skins.getCategories").getJSONArray("categories"));
        }

        public static String[] getCategorySkins(String category) throws InternalAPIException, CategoryNotFoundException {
            try {
                return toStringArray(API.getJSON(ApiMethod.create("skins.getCategorySkins").set("category", category), 1).getJSONArray("skins"));
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new CategoryNotFoundException(category);
            }
            return null;
        }

        public static BufferedImage getCategorySkin(String category, String name) throws InternalAPIException, CategoryNotFoundException {
            try {
                return API.getImage(ApiMethod.create("skins.getCategorySkin").set("category", category).set("name", name), 1);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new CategoryNotFoundException(category);
            }
            return null;
        }

        public static BufferedImage getCategoryPreview(String category) throws InternalAPIException, CategoryNotFoundException {
            try {
                return API.getImage(ApiMethod.create("skins.getCategoryPreview").set("category", category), 1);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new CategoryNotFoundException(category);
            }
            return null;
        }

        public static BufferedImage getSkin(String name) throws InternalAPIException, UnknownProfileNameException {
            try {
                return API.getImage(ApiMethod.create("skins.getSkin").set("name", name), 1);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownProfileNameException(name);
            }
            return null;
        }
    }

    public static class Auth {

        public static String getAccessToken(String login, String password) throws InternalAPIException, WrongAuthDataException {
            try{
                return getJSON(ApiMethod.create("auth.getAccessToken").set(LOGIN, login).set(PASSWORD, password), 1).getString(ACCESS_TOKEN);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new WrongAuthDataException();
            }
            return null;
        }

        public static void register(String login, String password) throws InternalAPIException, IncorrectLoginFormatException, LoginAlreadyExistException, IncorrectPasswordFormatException {
            try{
                getJSON(ApiMethod.create("auth.create").set(LOGIN, login).set(PASSWORD, password), 1, 2, 3);
            }catch (APIException e){
                switch (e.getCode()){
                    case 1: throw new IncorrectLoginFormatException();
                    case 2: throw new LoginAlreadyExistException();
                    case 3: throw new IncorrectPasswordFormatException();
                }
            }
        }
    }

    public static class Profile {

        public static void bindIP(String token) throws InternalAPIException, WrongAccessTokenException{
            try {
                getJSON(ProfileApiMethod.create("profile.bindIP", token), 25);
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
        }

        public static BufferedImage getSkin(String token) throws InternalAPIException, WrongAccessTokenException {
            try {
                return getImage(ProfileApiMethod.create("profile.getSkin", token), 25);
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return null;
        }

        public static BufferedImage getCape(String token) throws InternalAPIException, WrongAccessTokenException {
            try {
                return getImage(ProfileApiMethod.create("profile.getCape", token), 25);
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return null;
        }

        public static BufferedImage getElytra(String token) throws InternalAPIException, WrongAccessTokenException {
            try {
                return getImage(ProfileApiMethod.create("profile.getElytra", token), 25);
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return null;
        }

        public static void setCape(String token, BufferedImage skin) throws InternalAPIException, SkinTooLargeException, WrongAccessTokenException {
            try {
                getJSON(ProfileApiMethod.create("profile.setCape", token).set("base64", toBase64(skin)),  3, 25);
            }catch (APIException e){
                if(e.getCode() == 3)
                    throw new SkinTooLargeException();
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
        }

        public static void setCape(String token, String name) throws InternalAPIException, SkinNameNotFoundException, WrongAccessTokenException {
            try {
                getJSON(ProfileApiMethod.create("profile.setCape", token).set("name", name),  1, 2, 25);
            }catch (APIException e){
                switch (e.getCode()){
                    case 2: throw new SkinNameNotFoundException("", name);
                    case 25: throw new WrongAccessTokenException();
                }
            }
        }

        public static void setElytra(String token, BufferedImage skin) throws InternalAPIException, SkinTooLargeException, WrongAccessTokenException {
            try {
                getJSON(ProfileApiMethod.create("profile.setElytra", token).set("base64", toBase64(skin)),  3, 25);
            }catch (APIException e){
                if(e.getCode() == 3)
                    throw new SkinTooLargeException();
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
        }

        public static void setElytra(String token, String name) throws InternalAPIException, SkinNameNotFoundException, WrongAccessTokenException {
            try {
                getJSON(ProfileApiMethod.create("profile.setElytra", token).set("name", name),  1, 2, 25);
            }catch (APIException e){
                switch (e.getCode()){
                    case 2: throw new SkinNameNotFoundException("", name);
                    case 25: throw new WrongAccessTokenException();
                }
            }
        }

        public static void setSkin(String token, BufferedImage skin) throws InternalAPIException, SkinTooLargeException, WrongAccessTokenException {
            try {
                getJSON(ProfileApiMethod.create("profile.setSkin", token).set("base64", toBase64(skin)),  3, 25);
            }catch (APIException e){
                if(e.getCode() == 3)
                    throw new SkinTooLargeException();
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
        }

        public static void setSkin(String token, String category, String name) throws InternalAPIException, CategoryNotFoundException, SkinNameNotFoundException, WrongAccessTokenException {
            try {
                getJSON(ProfileApiMethod.create("profile.setSkin", token).set("category", category).set("name", name),  1, 2, 25);
            }catch (APIException e){
                switch (e.getCode()){
                    case 1: throw new CategoryNotFoundException(category);
                    case 2: throw new SkinNameNotFoundException(category, name);
                    case 25: throw new WrongAccessTokenException();
                }
            }
        }

        public static void confirmEmail(String token, String email, String emailCode) throws InternalAPIException, WrongAccessTokenException, EmailIsNotSpecifiedException, IncorrectEmailCodeException {
            try {
                getJSON(ProfileApiMethod.create("profile.confirmEmail", token).set(EMAIL, email).set(EMAIL_CODE, emailCode), 1, 2, 25);
            }catch (APIException e){
                switch (e.getCode()){
                    case 1: throw new EmailIsNotSpecifiedException();
                    case 2: throw new IncorrectEmailCodeException();
                    case 25: throw new WrongAccessTokenException();
                }
            }
        }

        public static boolean isEmailConfirmed(String token) throws InternalAPIException, WrongAccessTokenException {
            try {
                return getJSON(ProfileApiMethod.create("profile.isEmailConfirmed", token), 25).getBoolean("confirmed");
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return false;
        }

        public static void sendEmailCode(String token, String email) throws InternalAPIException, EmailCodeSendingException, WrongAccessTokenException, EmailAlreadyExistException {
            try {
                getJSON(ProfileApiMethod.create("profile.sendEmailCode", token).set(EMAIL, email), 1, 2, 25);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new EmailCodeSendingException();
                if(e.getCode() == 2)
                    throw new EmailAlreadyExistException();
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
        }

        public static HashMap<String, String> getData(String token, String... fields) throws InternalAPIException, WrongAccessTokenException {
            try {
                HashMap<String, String> data = new HashMap<>();

                JSONObject jsonObject = getJSON(ProfileApiMethod.create("profile.getData", token).set("fields", String.join(",", fields)), 25);
                jsonObject.getJSONObject("data").toMap().forEach((name, value) -> data.put(name, value.toString()));

                return data;
            }catch (APIException e){
                if(e.getCode() == 25)
                    throw new WrongAccessTokenException();
            }
            return null;
        }

        public static void setData(String token, HashMap<String, String> data, String currentPassword, String emailCode) throws InternalAPIException, CurrentPasswordRequiredException, EmailCodeRequiredException, IncorrectCurrentPasswordException, IncorrectEmailCodeException, IncorrectLoginFormatException, LoginAlreadyExistException, IncorrectEmailFormatException, EmailAlreadyExistException, WrongAccessTokenException {
            try {
                ProfileApiMethod method = ProfileApiMethod.create("profile.setData", token);
                if (currentPassword != null)
                    method.set(CURRENT_PASSWORD, currentPassword);
                if (emailCode != null)
                    method.set(EMAIL_CODE, emailCode);
                data.forEach(method::set);

                getJSON(method, 1, 2, 3, 4, 5, 6, 7, 8, 25);
            }catch (APIException ex){
                switch (ex.getCode()) {
                    case 1: throw new CurrentPasswordRequiredException();
                    case 2: throw new EmailCodeRequiredException();
                    case 3: throw new IncorrectCurrentPasswordException();
                    case 4: throw new IncorrectEmailCodeException();
                    case 5: throw new IncorrectLoginFormatException();
                    case 6: throw new LoginAlreadyExistException();
                    case 7: throw new IncorrectEmailFormatException();
                    case 8: throw new EmailAlreadyExistException();
                    case 25: throw new WrongAccessTokenException();
                }
            }
        }
    }


    public static class Client{

        public static void update(String token, File file, String id, String title, UpdatingActionListener listener) throws IOException {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(getMethodUrl(ProfileApiMethod.create("clients.update", token).set("id", id).set("name", title)));
                HttpEntity entity = MultipartEntityBuilder.create().addPart("file", new FileBody(file, ContentType.create("application/zip"), null)).build();
                NetManager.ProgressHttpEntityWrapper.ProgressCallback progressCallback = progress -> {
                    listener.sending((int)progress);
                    if(progress == 100)
                        listener.process();
                };

                post.setEntity(new NetManager.ProgressHttpEntityWrapper(entity, progressCallback));
                client.execute(post);
            }
        }

        public interface UpdatingActionListener{
            void sending(int progress);
            void process();
        }

        public static JSONObject getSizeInfo(String clientId) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                return getJSON(ApiMethod.create("clients.getSizeInfo").set("id", clientId), 1, 2);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return null;
        }

        public static boolean[] checksum(String clientId, String mods, String versions) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                JSONObject object = getJSON(ApiMethod.create("clients.checksum").set("id", clientId).set("mods", mods).set("versions", versions), 1, 2);
                return new boolean[]{object.getBoolean("equal_mods"), object.getBoolean("equal_versions")};
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return new boolean[]{false, false};
        }

        public static String getClientVersion(String clientId) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                return getJSON(ApiMethod.create("clients.getFilesInfo").set("id", clientId), 1, 2).getString("build");
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return null;
        }

        public static int getShortClientVersion(String clientId) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                return getJSON(ApiMethod.create("clients.getFilesInfo").set("id", clientId), 1, 2).getInt("build_id");
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return -1;
        }

        public static String getJarVersion(String clientId) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                return getJSON(ApiMethod.create("clients.getFilesInfo").set("id", clientId), 1, 2).getString("version");
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return null;
        }

        public static int getModsCount(String clientId) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                return getJSON(ApiMethod.create("clients.getModsCount").set("id", clientId), 1, 2).getInt("count");
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return 0;
        }

        public static ModInfo getModInfo(String clientId, int index) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                JSONArray info = getJSON(ApiMethod.create("clients.getModInfo").set("id", clientId).set("index", index), 1, 2).getJSONArray("mods");
                if(info.length() == 1)
                    return new ModInfo(info.getJSONObject(0));
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return null;
        }

        public static BufferedImage getModIcon(String clientId, int index) throws InternalAPIException, UnknownClientException, ClientIsUpdatingException {
            try {
                return getImage(ApiMethod.create("clients.getModIcon").set("id", clientId).set("index", index), 1, 2);
            }catch (APIException e){
                if(e.getCode() == 1)
                    throw new UnknownClientException();
                if(e.getCode() == 2)
                    throw new ClientIsUpdatingException();
            }
            return null;
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

    public static class IncorrectPasswordFormatException extends Exception{
        public IncorrectPasswordFormatException(){
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

    public static class WrongAccessTokenException extends Exception{
        public WrongAccessTokenException(){
            super("Wrong access token");
        }
    }

    public static class UnknownClientException extends Exception{
        public UnknownClientException(){
            super("Unknown client part");
        }
    }

    public static class UnknownProfileNameException extends Exception{
        public UnknownProfileNameException(String name){
            super("Unknown profile name: " + name);
        }
    }

    public static class EmailAlreadyExistException extends Exception{
        public EmailAlreadyExistException(){
            super("Email is already exist");
        }
    }

    public static class ClientIsUpdatingException extends Exception{
        public ClientIsUpdatingException(){
            super("Client is updating");
        }
    }
}
