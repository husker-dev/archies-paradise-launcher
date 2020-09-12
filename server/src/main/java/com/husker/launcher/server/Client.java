package com.husker.launcher.server;

import com.husker.launcher.server.services.browser.VkPostParameter;
import com.husker.launcher.server.utils.ConsoleUtils;
import com.husker.launcher.server.utils.ProfileUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Client {

    private final Socket socket;

    public Client(Socket client){
        this.socket = client;
        String ip = client.getInetAddress().getHostAddress();

        try {
            GetRequest received = GetRequest.create(socket);
            GetRequest outParameters = GetRequest.createWithTitle(received.getTitle());

            try {
                textGetters.get(received.getTitle()).apply(received, outParameters);
            }catch (Exception ex){
                outParameters.put(Profile.RESULT, "-1");
                ex.printStackTrace();
            }
            if(!outParameters.containsKey(Profile.RESULT))
                outParameters.put(Profile.RESULT, "0");

            outParameters.send(socket);
        }catch (Exception ex){
            if(ex.getMessage() != null && !ex.getMessage().equals("null"))
                ConsoleUtils.printDebug(Client.class, ip + " [ERROR] " + ex.getMessage());
        }
        disconnect();
    }

    public void disconnect(){
        try {
            socket.close();
        }catch (Exception ex){}
        try {
            socket.getInputStream().close();
        }catch (Exception ex){}
        try {
            socket.getOutputStream().close();
        }catch (Exception ex){}
    }

    public interface TextGetter{
        void apply(GetRequest in, GetRequest out);
    }

    public static HashMap<String, TextGetter> textGetters = new HashMap<String, Client.TextGetter>(){{
        put("get_key", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);

            String key = "-1";
            if(profile != null)
                key = profile.createKey();
            out.put("key", key);
        });

        put("remove_key", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);

            if(profile == null || in.get("key") == null)
                out.put("return", "-1");
            else {
                profile.removeKey(in.getString("key"));
                out.put("return", "0");
            }
        });

        put("get_encrypted_password", (in, out) -> out.put(Profile.PASSWORD, ProfileUtils.getProfile(in).getDataValue(Profile.PASSWORD)));
        put("get_profile_data", (in, out) -> out.putAll(ProfileUtils.getProfile(in).getData(in.getString("get").split(","))));

        put("set_profile_data", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            if(in.containsKey(Profile.KEY)){
                in.remove(Profile.KEY);
            }else{
                in.put(Profile.CURRENT_PASSWORD, in.get(Profile.PASSWORD));
                in.remove(Profile.LOGIN);
                in.remove(Profile.PASSWORD);
            }
            out.put("result", profile.setData(in.toStringMap()) + "");
        });

        put("is_login_taken", (in, out) -> out.put("result", ProfileUtils.isNicknameExist(in.getString(Profile.LOGIN)) ? "1" : "0"));

        put("register", (in, out) -> out.put("result", Profile.create(in.getString(Profile.LOGIN), in.getString(Profile.PASSWORD)) + ""));

        put("is_email_confirmed", (in, out) -> out.put("result", ProfileUtils.getProfile(in).isEmailConfirmed() ? "1" : "0"));

        put("send_email_code", (in, out) -> out.put("result", ProfileUtils.getProfile(in).sendEmailCode(in.getString(Profile.EMAIL)) + ""));

        put("confirm_mail", (in, out) -> out.put("result", ProfileUtils.getProfile(in).setData(new HashMap<String, String>(){{
            put(Profile.EMAIL, in.getString(Profile.EMAIL));
            put(Profile.EMAIL_CODE, in.getString(Profile.EMAIL_CODE));
        }}) + ""));

        put("get_skin_folders", (in, out) -> {
            try {
                if(!Files.exists(Paths.get("./skins")))
                    Files.createDirectory(Paths.get("./skins"));
            }catch (Exception ex){}
            StringBuilder folders = new StringBuilder();
            for(File file : Objects.requireNonNull(new File("./skins").listFiles(File::isDirectory)))
                folders.append(file.getName()).append(",");
            out.put("folders", folders.substring(0, folders.lastIndexOf(",")));
        });

        put("skin", (in, out) -> {
            out.put("skin", ProfileUtils.getProfile(in).getSkin());
            out.put("result", "0");
        });

        put("get_skin_folder_preview", (in, out) -> {
            if(Files.exists(Paths.get("./skins/" + in.get("folder")))){
                File[] skins = Objects.requireNonNull(new File("./skins/" + in.get("folder")).listFiles(file -> file.getName().endsWith(".png")));
                if(skins.length > 0){
                    try {
                        out.put("skin", ImageIO.read(skins[new Random().nextInt(skins.length)]));
                        out.put("result", "0");
                        return;
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            out.put("result", "-1");
        });

        // in: folder
        // out: skins
        put("get_skin_folder_skins", (in, out) -> {
            if(Files.exists(Paths.get("./skins/" + in.get("folder")))){
                File[] skins = Objects.requireNonNull(new File("./skins/" + in.get("folder")).listFiles(file -> file.getName().endsWith(".png")));
                if(skins.length > 0){
                    try {
                        StringBuilder list = new StringBuilder();
                        for(int i = 0; i < skins.length; i++){
                            list.append(skins[i].getName().replace(".png", ""));
                            if(i != skins.length - 1)
                                list.append(",");
                        }
                        out.put("skins", list);
                        out.put("result", "0");
                        return;
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            out.put("result", "-1");
        });

        // parameters: folder, name
        put("get_folder_skin", (in, out) -> {
            if(Files.exists(Paths.get("./skins/" + in.get("folder")))){
                File[] skins = Objects.requireNonNull(new File("./skins/" + in.get("folder")).listFiles(file -> file.getName().endsWith(".png")));
                if(skins.length > 0){
                    try {
                        for(File file : skins) {
                            if (file.getName().replace(".png", "").equals(in.getString("name"))) {
                                out.put("name", in.getString("name"));
                                out.put("skin", ImageIO.read(file));
                                out.put("result", "0");
                            }
                        }
                        return;
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            out.put("result", "-1");
        });

        put("set_skin", (in, out) -> {
            Profile profile = ProfileUtils.getProfile(in);
            try {
                BufferedImage skin = null;
                String name = "null";

                if (in.containsKey("folder") && in.containsKey("name")) {
                    skin = ImageIO.read(new File("./skins/" + in.get("folder") + "/" + in.get("name") + ".png"));
                    name = in.getString("name");
                } else if(in.containsKey("skin"))
                    skin = in.getImage("skin");

                if(skin != null){
                    // Player's skin file can't be removed instantly for some reasons, so we write image directly in it
                    ImageIO.write(skin, "png", new File(profile.getFolder() + "/skin.png"));

                    profile.setData(Profile.SKIN_NAME, name);
                    out.put("result", "0");
                    return;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
            out.put("result", "-1");
        });

        put("social_get_vk_list", (in, out) -> {
            int count = in.containsKey("count") ? Integer.parseInt(in.getString("count")) : 6;
            count = Math.min(count, ServerMain.BrowserService.getVkPosts().length);

            out.put("count", count + "");

            ArrayList<JSONObject> posts = new ArrayList<>();
            for(int i = 0; i < count; i++)
                posts.add(ServerMain.BrowserService.getVkPosts()[i].getJSON());

            out.put("elements", posts);
        });

        put("social_get_youtube_list", (in, out) -> {
            int count = in.containsKey("count") ? Integer.parseInt(in.getString("count")) : 6;
            count = Math.min(count, ServerMain.BrowserService.getYoutubeVideos().length);

            out.put("count", count + "");

            ArrayList<JSONObject> posts = new ArrayList<>();
            for(int i = 0; i < count; i++)
                posts.add(ServerMain.BrowserService.getYoutubeVideos()[i].getJSON());

            out.put("elements", posts);
        });

        put("social_get_vk_info", (in, out) -> {
            out.put("title", ServerMain.BrowserService.getVkTitle());
            out.put("url", ServerMain.BrowserService.getVKUrl());
            out.put("description", ServerMain.BrowserService.getVkDescription());
            out.put("image", ServerMain.BrowserService.getVkPreviewLogo());
        });

        put("social_get_youtube_info", (in, out) -> {
            out.put("title", ServerMain.BrowserService.getYoutubeTitle());
            out.put("url", ServerMain.BrowserService.getYouTubeUrl());
            out.put("subscribers", ServerMain.BrowserService.getYoutubeSubscribers());
            out.put("preview", ServerMain.BrowserService.getYoutubePreviewLogo());
        });
    }};
}
